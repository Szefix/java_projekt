package com.veterinary.clinic.repositories;

import com.veterinary.clinic.models.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    // Wyszukiwanie lekarzy po specjalizacji
    List<Doctor> findBySpecializationIgnoreCase(String specialization);

    // Wyszukiwanie po nazwisku
    List<Doctor> findByLastNameContainingIgnoreCase(String lastName);
}