package com.mergency.authDemo.dto;

import lombok.*;

@Getter
@Setter
public class VerifyUserDto {
    private String email;
    private String verificationCode;
}
