package com.mycompany.sapo_leyendo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Set;

@Data
@AllArgsConstructor
public class UserInfo {
    private Integer id;
    private String username;
    private String firstName;
    private String lastName;
    private Set<String> roles;
    private Set<String> permissions;
}
