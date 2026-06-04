package com.dialysis.repository;

import com.dialysis.entity.Center;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CenterRepository extends JpaRepository<Center, UUID> {
}