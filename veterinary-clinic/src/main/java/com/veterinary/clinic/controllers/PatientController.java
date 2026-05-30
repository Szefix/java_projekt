package com.veterinary.clinic.controllers;

import com.veterinary.clinic.dto.PatientDTO;
import com.veterinary.clinic.services.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@Tag(name = "Pacjenci", description = "Zarządzanie pacjentami (zwierzętami)")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    @Operation(summary = "Pobierz wszystkich pacjentów",
            description = "Opcjonalne filtry: gatunek lub nazwisko właściciela")
    public ResponseEntity<List<PatientDTO>> getAllPatients(
            @Parameter(description = "Filtruj po gatunku (np. Pies, Kot)")
            @RequestParam(required = false) String species,
            @Parameter(description = "Filtruj po nazwisku właściciela")
            @RequestParam(required = false) String owner) {

        if (species != null && !species.isBlank()) {
            return ResponseEntity.ok(patientService.getPatientsBySpecies(species));
        }
        if (owner != null && !owner.isBlank()) {
            return ResponseEntity.ok(patientService.getPatientsByOwner(owner));
        }
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Pobierz pacjenta po ID")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @PostMapping
    @Operation(summary = "Dodaj nowego pacjenta")
    public ResponseEntity<PatientDTO> createPatient(@Valid @RequestBody PatientDTO patientDTO) {
        PatientDTO created = patientService.createPatient(patientDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aktualizuj dane pacjenta")
    public ResponseEntity<PatientDTO> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientDTO patientDTO) {
        return ResponseEntity.ok(patientService.updatePatient(id, patientDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Usuń pacjenta")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}