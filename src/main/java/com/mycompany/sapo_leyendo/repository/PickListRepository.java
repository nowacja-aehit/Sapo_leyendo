package com.mycompany.sapo_leyendo.repository;

import com.mycompany.sapo_leyendo.model.PickList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PickListRepository extends JpaRepository<PickList, UUID> {
}
