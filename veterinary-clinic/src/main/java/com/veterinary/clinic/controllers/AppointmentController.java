package com.veterinary.clinic.controllers;

import com.veterinary.clinic.dto.AppointmentDTO;
import com.veterinary.clinic.dto.AppointmentRequestDTO;
import com.veterinary.clinic.services.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PatchMapping;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@Tag(name = "Wizyty", description = "Rejestracja i zarządzanie wizytami weterynaryjnymi")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    @Operation(summary = "Pobierz wszystkie wizyty", operationId = "1",
            description = "Opcjonalne filtry: doctorId lub patientId")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments(
            @Parameter(description = "Filtruj po ID lekarza")
            @RequestParam(required = false) Long doctorId,
            @Parameter(description = "Filtruj po ID pacjenta")
            @RequestParam(required = false) Long patientId) {

        if (doctorId != null) {
            return ResponseEntity.ok(appointmentService.getAppointmentsByDoctor(doctorId));
        }
        if (patientId != null) {
            return ResponseEntity.ok(appointmentService.getAppointmentsByPatient(patientId));
        }
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Pobierz wizytę po ID", operationId = "2")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    @PostMapping
    @Operation(summary = "Zarejestruj nową wizytę", operationId = "3",
            description = "Automatycznie sprawdza konflikty terminu lekarza (okno 30 minut)")
    public ResponseEntity<AppointmentDTO> createAppointment(
            @Valid @RequestBody AppointmentRequestDTO requestDTO) {
        AppointmentDTO created = appointmentService.createAppointment(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aktualizuj wizytę", operationId = "5",
            description = "Zmiana terminu ponownie sprawdza konflikty")
    public ResponseEntity<AppointmentDTO> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRequestDTO requestDTO) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Odwołaj / usuń wizytę", operationId = "4")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Odwołaj wizytę (zmiana statusu na ODWOLANA)",
            description = "Nie usuwa wizyty z bazy — zachowuje historię")
    public ResponseEntity<AppointmentDTO> cancelAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(id));
    }
}

