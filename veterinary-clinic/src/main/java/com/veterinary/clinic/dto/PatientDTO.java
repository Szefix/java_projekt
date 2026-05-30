package com.veterinary.clinic.dto;

import jakarta.validation.constraints.NotBlank;

public class PatientDTO {

    private Long id;

    @NotBlank(message = "Imię zwierzęcia nie może być puste")
    private String name;

    @NotBlank(message = "Gatunek nie może być pusty")
    private String species;

    @NotBlank(message = "Imię i nazwisko właściciela nie może być puste")
    private String ownerName;

    // ===== Konstruktory =====

    public PatientDTO() {}

    public PatientDTO(Long id, String name, String species, String ownerName) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.ownerName = ownerName;
    }

    // ===== Gettery i Settery =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
}