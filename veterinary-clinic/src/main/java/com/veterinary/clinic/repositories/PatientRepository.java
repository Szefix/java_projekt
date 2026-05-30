package com.veterinary.clinic.repositories;

import com.veterinary.clinic.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Wyszukiwanie pacjentów po gatunku
    List<Patient> findBySpeciesIgnoreCase(String species);

    // Wyszukiwanie po właścicielu
    List<Patient> findByOwnerNameContainingIgnoreCase(String ownerName);

    // Wyszukiwanie po imieniu zwierzęcia
    List<Patient> findByNameContainingIgnoreCase(String name);
}