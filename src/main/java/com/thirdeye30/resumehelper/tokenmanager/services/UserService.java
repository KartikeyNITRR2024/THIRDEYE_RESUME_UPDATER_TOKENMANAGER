package com.thirdeye30.resumehelper.tokenmanager.services;

import java.util.UUID;
import com.thirdeye30.resumehelper.tokenmanager.dtos.UserDto;

public interface UserService {
    UserDto createUser(UserDto userDto);
    UserDto getUser(UUID id);
    Long getToken(UUID id);
    UserDto updateName(UUID id, String name);
    void deleteUsersOlderThan();
	void addToken(UUID id, Long amount);
	void subtractToken(UUID id, Long amount);
}
