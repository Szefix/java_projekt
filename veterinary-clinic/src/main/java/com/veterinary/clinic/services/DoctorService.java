package com.veterinary.clinic.services;

import com.veterinary.clinic.dto.DoctorDTO;
import com.veterinary.clinic.exceptions.ResourceNotFoundException;
import com.veterinary.clinic.models.Doctor;
import com.veterinary.clinic.repositories.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    // ===== Pobranie wszystkich lekarzy =====
    @Transactional(readOnly = true)
    public List<DoctorDTO> getAllDoctors() {
        return doctorRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ===== Pobranie lekarza po ID =====
    @Transactional(readOnly = true)
    public DoctorDTO getDoctorById(Long id) {
        Doctor doctor = findDoctorOrThrow(id);
        return mapToDTO(doctor);
    }

    // ===== Pobranie encji (używane wewnętrznie przez AppointmentService) =====
    @Transactional(readOnly = true)
    public Doctor getDoctorEntityById(Long id) {
        return findDoctorOrThrow(id);
    }

    // ===== Dodanie nowego lekarza =====
    public DoctorDTO createDoctor(DoctorDTO dto) {
        Doctor doctor = new Doctor(dto.getFirstName(), dto.getLastName(), dto.getSpecialization());
        Doctor saved = doctorRepository.save(doctor);
        return mapToDTO(saved);
    }

    // ===== Aktualizacja lekarza =====
    public DoctorDTO updateDoctor(Long id, DoctorDTO dto) {
        Doctor doctor = findDoctorOrThrow(id);
        doctor.setFirstName(dto.getFirstName());
        doctor.setLastName(dto.getLastName());
        doctor.setSpecialization(dto.getSpecialization());
        Doctor updated = doctorRepository.save(doctor);
        return mapToDTO(updated);
    }

    // ===== Usunięcie lekarza =====
    public void deleteDoctor(Long id) {
        findDoctorOrThrow(id);
        doctorRepository.deleteById(id);
    }

    // ===== Wyszukiwanie po specjalizacji =====
    @Transactional(readOnly = true)
    public List<DoctorDTO> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecializationIgnoreCase(specialization)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ===== Mapowanie encja → DTO =====
    private DoctorDTO mapToDTO(Doctor doctor) {
        return new DoctorDTO(
                doctor.getId(),
                doctor.getFirstName(),
                doctor.getLastName(),
                doctor.getSpecialization()
        );
    }

    private Doctor findDoctorOrThrow(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lekarz", id));
    }
}