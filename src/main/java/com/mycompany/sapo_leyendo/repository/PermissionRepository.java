package com.mycompany.sapo_leyendo.repository;

import com.mycompany.sapo_leyendo.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
}
