package com.thirdeye30.resumehelper.tokenmanager.services;

import java.util.UUID;
import com.thirdeye30.resumehelper.tokenmanager.dtos.UserDto;

public interface UserService {
    UserDto createUser(UserDto userDto);
    UserDto getUser(UUID id);
    Long getToken(UUID id);
    void deleteUsersOlderThan();
	void addToken(UUID id, Long amount);
	void subtractToken(UUID id, Long amount);
	UserDto updateNameAndEmail(UUID id, String name, String email);
}
