package com.veterinary.clinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

// DTO do tworzenia / aktualizowania wizyty (żądanie klienta)
public class AppointmentRequestDTO {

    @NotNull(message = "Data i godzina wizyty nie mogą być puste")
    private LocalDateTime dateTime;

    @NotBlank(message = "Opis wizyty nie może być pusty")
    private String description;

    @NotNull(message = "ID lekarza jest wymagane")
    private Long doctorId;

    @NotNull(message = "ID pacjenta jest wymagane")
    private Long patientId;

    // ===== Konstruktory =====

    public AppointmentRequestDTO() {}

    public AppointmentRequestDTO(LocalDateTime dateTime, String description, Long doctorId, Long patientId) {
        this.dateTime = dateTime;
        this.description = description;
        this.doctorId = doctorId;
        this.patientId = patientId;
    }

    // ===== Gettery i Settery =====

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
}