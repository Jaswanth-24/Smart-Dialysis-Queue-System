package com.dialysis.repository;

import com.dialysis.entity.Patient;
import com.dialysis.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    boolean existsByUser(User user);

    List<Patient> findByCenterId(UUID centerId);

    List<Patient> findByDoctorId(UUID doctorId);

}