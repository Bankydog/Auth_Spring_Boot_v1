package com.mergency.authDemo.dto;

import lombok.*;

@Getter
@Setter
public class RegisterUserDto {
    private String username;
    private String password;
    private String email;
}
