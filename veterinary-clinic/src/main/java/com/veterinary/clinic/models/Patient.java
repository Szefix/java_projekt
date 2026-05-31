package com.veterinary.clinic.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;
import com.veterinary.clinic.models.BaseEntity;

@Entity
@Table(name = "patients")
public class Patient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Imię zwierzęcia nie może być puste")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Gatunek nie może być pusty")
    @Column(nullable = false)
    private String species;

    @NotBlank(message = "Imię i nazwisko właściciela nie może być puste")
    @Column(nullable = false)
    private String ownerName;

    @Pattern(regexp = "\\d{9}", message = "Numer telefonu musi składać się z dokładnie 9 cyfr")
    @Column
    private String ownerPhone;

    @Email(message = "Adres email właściciela jest nieprawidłowy")
    @Column
    private String ownerEmail;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appointment> appointments = new ArrayList<>();

    // ===== Konstruktory =====

    public Patient() {}

    public Patient(String name, String species, String ownerName) {
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

    public String getOwnerPhone() { return ownerPhone; }
    public void setOwnerPhone(String ownerPhone) { this.ownerPhone = ownerPhone; }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }

    public List<Appointment> getAppointments() { return appointments; }
    public void setAppointments(List<Appointment> appointments) { this.appointments = appointments; }

    @Override
    public String toString() {
        return "Patient{id=" + id + ", name='" + name + "', species='" + species + "', owner='" + ownerName + "'}";
    }
}