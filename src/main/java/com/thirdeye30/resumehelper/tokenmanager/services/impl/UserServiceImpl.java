package com.thirdeye30.resumehelper.tokenmanager.services.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thirdeye30.resumehelper.tokenmanager.dtos.UserDto;
import com.thirdeye30.resumehelper.tokenmanager.entities.User;
import com.thirdeye30.resumehelper.tokenmanager.repos.UserRepository;
import com.thirdeye30.resumehelper.tokenmanager.services.ConfigService;
import com.thirdeye30.resumehelper.tokenmanager.services.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ConfigService configService;
    private final StringRedisTemplate redisTemplate;

    @Value("${thirdeye.redis.balance-prefix}")
    private String redisBalancePrefix;

    private static final String SUBTRACT_LUA = 
        "local current = redis.call('get', KEYS[1]) " +
        "if current and tonumber(current) >= tonumber(ARGV[1]) then " +
        "  return redis.call('decrby', KEYS[1], ARGV[1]) " +
        "else " +
        "  return -1 " +
        "end";

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        Long initialTokens = configService.getConfig().getMaximumTokenAllocated();
        User user = User.builder()
                .name(userDto.getName() == null ? "User" : userDto.getName())
                .token(initialTokens)
                .build();
        
        User savedUser = userRepository.save(user);
        
        redisTemplate.opsForValue().set(
            redisBalancePrefix + savedUser.getId(), 
            initialTokens.toString(), 
            Duration.ofMinutes(30)
        );
        
        return mapToDto(savedUser);
    }

    @Override
    @Transactional
    public void addToken(UUID id, Long amount) {
        if (userRepository.addToken(id, amount) == 0) throw new RuntimeException("User not found");
        
        String key = redisBalancePrefix + id;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForValue().increment(key, amount);
        } else {
            getToken(id);
        }
    }

    @Override
    @Transactional
    public void subtractToken(UUID id, Long amount) {
        String key = redisBalancePrefix + id;
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(SUBTRACT_LUA, Long.class);
        
        Long result = redisTemplate.execute(script, Collections.singletonList(key), amount.toString());

        if (result != null && result >= 0) {
            try {
                if (userRepository.subtractToken(id, amount) == 0) throw new RuntimeException("DB Sync Failed");
            } catch (Exception e) {
                redisTemplate.opsForValue().increment(key, amount);
                throw new RuntimeException("Database update failed, Redis rolled back");
            }
        } else {
            throw new RuntimeException("Insufficient tokens or user session expired");
        }
    }

    @Override
    @Transactional
    public void deleteUsersOlderThan() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(configService.getConfig().getMaximumTimeForUserInDays());
        userRepository.deleteByCreateTimeBefore(threshold);
    }

    @Override
    public Long getToken(UUID id) {
        String key = redisBalancePrefix + id;
        String cached = redisTemplate.opsForValue().get(key);
        
        if (cached != null) return Long.parseLong(cached);

        Long dbToken = userRepository.findTokenById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        redisTemplate.opsForValue().set(key, dbToken.toString(), Duration.ofMinutes(30));
        return dbToken;
    }

    @Override
    public UserDto getUser(UUID id) {
        return mapToDto(userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found")));
    }

    @Override
    @Transactional
    public UserDto updateNameAndEmail(UUID id, String name, String email) {
        if (userRepository.updateNameAndEmail(id, name, email) == 0) throw new RuntimeException("User not found");
        return mapToDto(userRepository.findById(id).orElseThrow());
    }

    private UserDto mapToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setToken(user.getToken());
        dto.setCreateTime(user.getCreateTime());
        return dto;
    }
}