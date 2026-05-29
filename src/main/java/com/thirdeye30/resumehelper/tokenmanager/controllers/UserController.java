package com.thirdeye30.resumehelper.tokenmanager.controllers;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.thirdeye30.resumehelper.tokenmanager.dtos.UserDto;
import com.thirdeye30.resumehelper.tokenmanager.services.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tokenmanager/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        UserDto createdUser = userService.createUser(userDto);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @GetMapping("/{id}/token")
    public ResponseEntity<Long> getToken(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getToken(id));
    }

    @PutMapping("/{id}/name")
    public ResponseEntity<UserDto> updateName(@PathVariable UUID id, @RequestParam String name) {
        return new ResponseEntity<>(userService.updateName(id, name), HttpStatus.OK);
    }
    
    @PutMapping("/{id}/token/add/{amount}")
    public ResponseEntity<Void> addToken(@PathVariable UUID id, @PathVariable Long amount) {
        userService.addToken(id, amount);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/token/subtract/{amount}")
    public ResponseEntity<Void> subtractToken(@PathVariable UUID id, @PathVariable Long amount) {
        userService.subtractToken(id, amount);
        return ResponseEntity.ok().build();
    }
}
