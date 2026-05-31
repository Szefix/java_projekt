package com.veterinary.clinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public class PatientDTO {

    private Long id;

    @NotBlank(message = "Imię zwierzęcia nie może być puste")
    private String name;

    @NotBlank(message = "Gatunek nie może być pusty")
    private String species;

    @NotBlank(message = "Imię i nazwisko właściciela nie może być puste")
    private String ownerName;

    @Pattern(regexp = "\\d{9}", message = "Numer telefonu musi składać się z dokładnie 9 cyfr")
    private String ownerPhone;

    @Email(message = "Adres email właściciela jest nieprawidłowy")
    private String ownerEmail;

    // ===== Konstruktory =====

    public PatientDTO() {}

    public PatientDTO(Long id, String name, String species, String ownerName,
                      String ownerPhone, String ownerEmail) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.ownerName = ownerName;
        this.ownerPhone = ownerPhone;
        this.ownerEmail = ownerEmail;
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

    public String getOwnerPhone() { return ownerPhone; }
    public void setOwnerPhone(String ownerPhone) { this.ownerPhone = ownerPhone; }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }
}