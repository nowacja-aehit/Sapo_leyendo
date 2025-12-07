package com.mycompany.sapo_leyendo.dto;

import lombok.Data;
import java.util.Set;

@Data
public class UserRequest {
    private String login;
    private String password;
    private String firstName;
    private String lastName;
    private boolean isActive;
    private Set<Integer> roleIds;
}
