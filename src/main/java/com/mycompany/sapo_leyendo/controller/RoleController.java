package com.mycompany.sapo_leyendo.controller;

import com.mycompany.sapo_leyendo.dto.RoleRequest;
import com.mycompany.sapo_leyendo.model.Permission;
import com.mycompany.sapo_leyendo.model.Role;
import com.mycompany.sapo_leyendo.repository.PermissionRepository;
import com.mycompany.sapo_leyendo.repository.RoleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleController(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @GetMapping
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable Integer id) {
        return roleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Role createRole(@RequestBody RoleRequest roleRequest) {
        Role role = new Role();
        role.setRoleName(roleRequest.getRoleName());
        role.setDescription(roleRequest.getDescription());
        
        if (roleRequest.getPermissionIds() != null) {
            List<Permission> permissions = permissionRepository.findAllById(roleRequest.getPermissionIds());
            role.setPermissions(new HashSet<>(permissions));
        }

        return roleRepository.save(role);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(@PathVariable Integer id, @RequestBody RoleRequest roleRequest) {
        return roleRepository.findById(id)
                .map(role -> {
                    role.setRoleName(roleRequest.getRoleName());
                    role.setDescription(roleRequest.getDescription());
                    
                    if (roleRequest.getPermissionIds() != null) {
                        List<Permission> permissions = permissionRepository.findAllById(roleRequest.getPermissionIds());
                        role.setPermissions(new HashSet<>(permissions));
                    }
                    
                    return ResponseEntity.ok(roleRepository.save(role));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Integer id) {
        if (roleRepository.existsById(id)) {
            roleRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/permissions")
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }
}
