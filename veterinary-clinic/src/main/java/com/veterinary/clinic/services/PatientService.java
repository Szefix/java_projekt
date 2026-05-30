package com.veterinary.clinic.services;

import com.veterinary.clinic.dto.PatientDTO;
import com.veterinary.clinic.exceptions.ResourceNotFoundException;
import com.veterinary.clinic.models.Patient;
import com.veterinary.clinic.repositories.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    // ===== Pobranie wszystkich pacjentów =====
    @Transactional(readOnly = true)
    public List<PatientDTO> getAllPatients() {
        return patientRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ===== Pobranie pacjenta po ID =====
    @Transactional(readOnly = true)
    public PatientDTO getPatientById(Long id) {
        Patient patient = findPatientOrThrow(id);
        return mapToDTO(patient);
    }

    // ===== Pobranie encji (używane wewnętrznie przez AppointmentService) =====
    @Transactional(readOnly = true)
    public Patient getPatientEntityById(Long id) {
        return findPatientOrThrow(id);
    }

    // ===== Dodanie nowego pacjenta =====
    public PatientDTO createPatient(PatientDTO dto) {
        Patient patient = new Patient(dto.getName(), dto.getSpecies(), dto.getOwnerName());
        Patient saved = patientRepository.save(patient);
        return mapToDTO(saved);
    }

    // ===== Aktualizacja pacjenta =====
    public PatientDTO updatePatient(Long id, PatientDTO dto) {
        Patient patient = findPatientOrThrow(id);
        patient.setName(dto.getName());
        patient.setSpecies(dto.getSpecies());
        patient.setOwnerName(dto.getOwnerName());
        Patient updated = patientRepository.save(patient);
        return mapToDTO(updated);
    }

    // ===== Usunięcie pacjenta =====
    public void deletePatient(Long id) {
        findPatientOrThrow(id);
        patientRepository.deleteById(id);
    }

    // ===== Wyszukiwanie po gatunku =====
    @Transactional(readOnly = true)
    public List<PatientDTO> getPatientsBySpecies(String species) {
        return patientRepository.findBySpeciesIgnoreCase(species)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ===== Wyszukiwanie po właścicielu =====
    @Transactional(readOnly = true)
    public List<PatientDTO> getPatientsByOwner(String ownerName) {
        return patientRepository.findByOwnerNameContainingIgnoreCase(ownerName)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ===== Mapowanie encja → DTO =====
    private PatientDTO mapToDTO(Patient patient) {
        return new PatientDTO(
                patient.getId(),
                patient.getName(),
                patient.getSpecies(),
                patient.getOwnerName()
        );
    }

    private Patient findPatientOrThrow(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pacjent", id));
    }
}