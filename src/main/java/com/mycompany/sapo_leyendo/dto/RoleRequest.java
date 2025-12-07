package com.mycompany.sapo_leyendo.dto;

import lombok.Data;
import java.util.Set;

@Data
public class RoleRequest {
    private String roleName;
    private String description;
    private Set<Integer> permissionIds;
}
