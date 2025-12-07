package com.mycompany.sapo_leyendo.repository;

import com.mycompany.sapo_leyendo.model.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParcelRepository extends JpaRepository<Parcel, Integer> {
}
