package com.veterinary.clinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.veterinary.clinic.models.AppointmentStatus;

import java.time.LocalDateTime;

// DTO do wyświetlania wizyty (odpowiedź serwera)
public class AppointmentDTO {

    private Long id;
    private LocalDateTime dateTime;
    private String description;
    private Long doctorId;
    private String doctorFullName;
    private String doctorSpecialization;
    private Long patientId;
    private String patientName;
    private String patientSpecies;
    private String ownerName;
    private AppointmentStatus status;

    // ===== Konstruktory =====

    public AppointmentDTO() {}

    public AppointmentDTO(Long id, LocalDateTime dateTime, String description,
                          Long doctorId, String doctorFullName, String doctorSpecialization,
                          Long patientId, String patientName, String patientSpecies, String ownerName,
                          AppointmentStatus status) {
        this.id = id;
        this.dateTime = dateTime;
        this.description = description;
        this.doctorId = doctorId;
        this.doctorFullName = doctorFullName;
        this.doctorSpecialization = doctorSpecialization;
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientSpecies = patientSpecies;
        this.ownerName = ownerName;
        this.status = status;
    }

    // ===== Gettery i Settery =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getDoctorFullName() { return doctorFullName; }
    public void setDoctorFullName(String doctorFullName) { this.doctorFullName = doctorFullName; }

    public String getDoctorSpecialization() { return doctorSpecialization; }
    public void setDoctorSpecialization(String doctorSpecialization) { this.doctorSpecialization = doctorSpecialization; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientSpecies() { return patientSpecies; }
    public void setPatientSpecies(String patientSpecies) { this.patientSpecies = patientSpecies; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
}