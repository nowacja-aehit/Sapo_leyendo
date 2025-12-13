package com.mycompany.sapo_leyendo.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
