package com.veterinary.clinic.dto;

import jakarta.validation.constraints.NotBlank;

public class DoctorDTO {

    private Long id;

    @NotBlank(message = "Imię lekarza nie może być puste")
    private String firstName;

    @NotBlank(message = "Nazwisko lekarza nie może być puste")
    private String lastName;

    @NotBlank(message = "Specjalizacja nie może być pusta")
    private String specialization;

    // ===== Konstruktory =====

    public DoctorDTO() {}

    public DoctorDTO(Long id, String firstName, String lastName, String specialization) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
    }

    // ===== Gettery i Settery =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
}