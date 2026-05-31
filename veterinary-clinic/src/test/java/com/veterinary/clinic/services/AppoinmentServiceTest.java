package com.veterinary.clinic.services;

import com.veterinary.clinic.dto.AppointmentRequestDTO;
import com.veterinary.clinic.exceptions.AppointmentConflictException;
import com.veterinary.clinic.exceptions.ResourceNotFoundException;
import com.veterinary.clinic.models.Appointment;
import com.veterinary.clinic.models.Doctor;
import com.veterinary.clinic.models.Patient;
import com.veterinary.clinic.repositories.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testy jednostkowe AppointmentService")
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorService doctorService;

    @Mock
    private PatientService patientService;

    @InjectMocks
    private AppointmentService appointmentService;

    // ===== Dane testowe =====
    private Doctor testDoctor;
    private Patient testPatient;
    private LocalDateTime baseDateTime;

    @BeforeEach
    void setUp() {
        testDoctor = new Doctor("Anna", "Kowalska", "Chirurg");
        testDoctor.setId(1L);

        testPatient = new Patient("Burek", "Pies", "Jan Kowalski");
        testPatient.setId(1L);

        baseDateTime = LocalDateTime.of(2025, 6, 16, 10, 0);
    }

    // =========================================================
    //  SCENARIUSZ 1: Poprawne tworzenie wizyty (brak konfliktu)
    // =========================================================

    @Test
    @DisplayName("Powinien zapisać wizytę gdy brak konfliktów terminów")
    void createAppointment_shouldSave_whenNoConflict() {
        // GIVEN: repozytorium nie znajdzie żadnych kolidujących wizyt
        AppointmentRequestDTO request = new AppointmentRequestDTO(
                baseDateTime, "Badanie kontrolne", 1L, 1L);

        when(doctorService.getDoctorEntityById(1L)).thenReturn(testDoctor);
        when(patientService.getPatientEntityById(1L)).thenReturn(testPatient);
        when(appointmentRepository.findConflictingAppointments(
                anyLong(), any(), any(), isNull()))
                .thenReturn(Collections.emptyList());

        Appointment savedAppointment = new Appointment(baseDateTime, "Badanie kontrolne", testDoctor, testPatient);
        savedAppointment.setId(1L);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);

        // WHEN
        var result = appointmentService.createAppointment(request);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Badanie kontrolne");
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    // =========================================================
    //  SCENARIUSZ 2: Konflikt – dokładnie ten sam czas
    // =========================================================

    @Test
    @DisplayName("Powinien rzucić wyjątek gdy lekarz ma wizytę o dokładnie tej samej godzinie")
    void createAppointment_shouldThrow_whenExactSameTime() {
        // GIVEN: istnieje już wizyta lekarza o tym samym czasie
        AppointmentRequestDTO request = new AppointmentRequestDTO(
                baseDateTime, "Nowa wizyta", 1L, 1L);

        Appointment existingAppointment = new Appointment(
                baseDateTime, "Istniejąca wizyta", testDoctor, testPatient);
        existingAppointment.setId(99L);

        when(doctorService.getDoctorEntityById(1L)).thenReturn(testDoctor);
        when(patientService.getPatientEntityById(1L)).thenReturn(testPatient);
        when(appointmentRepository.findConflictingAppointments(
                anyLong(), any(), any(), isNull()))
                .thenReturn(List.of(existingAppointment));

        // WHEN & THEN
        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(AppointmentConflictException.class)
                .hasMessageContaining("zaplanowaną wizytę");

        // Upewnij się, że save() NIE został wywołany
        verify(appointmentRepository, never()).save(any());
    }

    // =========================================================
    //  SCENARIUSZ 3: Konflikt – wizyta 15 minut wcześniej
    // =========================================================

    @Test
    @DisplayName("Powinien rzucić wyjątek gdy istniejąca wizyta jest 15 minut wcześniej (w oknie 30 min)")
    void createAppointment_shouldThrow_whenConflictWithin30Minutes() {
        // GIVEN: wizyta o 9:45, nowa próba na 10:00 → różnica tylko 15 minut
        LocalDateTime conflictingTime = baseDateTime.minusMinutes(15);
        AppointmentRequestDTO request = new AppointmentRequestDTO(
                baseDateTime, "Nowa wizyta", 1L, 1L);

        Appointment conflictingAppointment = new Appointment(
                conflictingTime, "Wcześniejsza wizyta", testDoctor, testPatient);
        conflictingAppointment.setId(50L);

        when(doctorService.getDoctorEntityById(1L)).thenReturn(testDoctor);
        when(patientService.getPatientEntityById(1L)).thenReturn(testPatient);
        when(appointmentRepository.findConflictingAppointments(
                anyLong(), any(), any(), isNull()))
                .thenReturn(List.of(conflictingAppointment));

        // WHEN & THEN
        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(AppointmentConflictException.class);

        verify(appointmentRepository, never()).save(any());
    }

    // =========================================================
    //  SCENARIUSZ 4: Brak konfliktu – wizyta ponad 30 min później
    // =========================================================

    @Test
    @DisplayName("Powinien zapisać wizytę gdy poprzednia jest dokładnie 31 minut wcześniej")
    void createAppointment_shouldSave_whenPreviousAppointmentIs31MinutesBefore() {
        // GIVEN: poprzednia wizyta o 9:29, nowa na 10:00 → różnica 31 minut → OK
        AppointmentRequestDTO request = new AppointmentRequestDTO(
                baseDateTime, "Wizyta popołudniowa", 1L, 1L);

        when(doctorService.getDoctorEntityById(1L)).thenReturn(testDoctor);
        when(patientService.getPatientEntityById(1L)).thenReturn(testPatient);
        // repozytorium nie zwraca konfliktów – okno nie obejmuje 9:29
        when(appointmentRepository.findConflictingAppointments(
                anyLong(), any(), any(), isNull()))
                .thenReturn(Collections.emptyList());

        Appointment saved = new Appointment(baseDateTime, "Wizyta popołudniowa", testDoctor, testPatient);
        saved.setId(2L);
        when(appointmentRepository.save(any())).thenReturn(saved);

        // WHEN & THEN – nie rzuca wyjątku
        assertThatCode(() -> appointmentService.createAppointment(request))
                .doesNotThrowAnyException();

        verify(appointmentRepository, times(1)).save(any());
    }

    // =========================================================
    //  SCENARIUSZ 5: Aktualizacja wizyty – brak konfliktu z samą sobą
    // =========================================================

    @Test
    @DisplayName("Przy aktualizacji powinien wykluczyć własne ID z walidacji konfliktu")
    void updateAppointment_shouldExcludeSelfFromConflictCheck() {
        // GIVEN
        Long appointmentId = 10L;
        Appointment existing = new Appointment(baseDateTime, "Stary opis", testDoctor, testPatient);
        existing.setId(appointmentId);

        AppointmentRequestDTO request = new AppointmentRequestDTO(
                baseDateTime.plusMinutes(10), "Nowy opis", 1L, 1L);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(existing));
        when(doctorService.getDoctorEntityById(1L)).thenReturn(testDoctor);
        when(patientService.getPatientEntityById(1L)).thenReturn(testPatient);
        // Ważne: excludeId = appointmentId → repozytorium zwraca pustą listę
        when(appointmentRepository.findConflictingAppointments(
                anyLong(), any(), any(), eq(appointmentId)))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.save(any())).thenReturn(existing);

        // WHEN & THEN
        assertThatCode(() -> appointmentService.updateAppointment(appointmentId, request))
                .doesNotThrowAnyException();

        // Sprawdź, że zapytanie o konflikty było wywołane z właściwym excludeId
        verify(appointmentRepository).findConflictingAppointments(
                eq(1L), any(), any(), eq(appointmentId));
    }

    // =========================================================
    //  SCENARIUSZ 6: Pobranie wizyty – nieistniejące ID
    // =========================================================

    @Test
    @DisplayName("Powinien rzucić ResourceNotFoundException dla nieistniejącego ID wizyty")
    void getAppointmentById_shouldThrow_whenNotFound() {
        // GIVEN
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> appointmentService.getAppointmentById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // =========================================================
    //  SCENARIUSZ 7: Usunięcie wizyty – nieistniejące ID
    // =========================================================

    @Test
    @DisplayName("Powinien rzucić ResourceNotFoundException przy próbie usunięcia nieistniejącej wizyty")
    void deleteAppointment_shouldThrow_whenNotFound() {
        // GIVEN
        when(appointmentRepository.findById(777L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> appointmentService.deleteAppointment(777L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(appointmentRepository, never()).deleteById(any());
    }

    // =========================================================
    //  GODZINY OTWARCIA – testy checkClinicHours
    // =========================================================

    @Test
    @DisplayName("Powinien zaakceptować wizytę w godzinach otwarcia (pon, 10:00)")
    void checkClinicHours_shouldPass_onWeekdayWithinHours() {
        LocalDateTime valid = LocalDateTime.of(2025, 6, 16, 10, 0); // poniedziałek
        assertThatCode(() -> appointmentService.checkClinicHours(valid))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Powinien rzucić wyjątek dla wizyty w sobotę")
    void checkClinicHours_shouldThrow_onSaturday() {
        LocalDateTime saturday = LocalDateTime.of(2025, 6, 14, 10, 0);
        assertThatThrownBy(() -> appointmentService.checkClinicHours(saturday))
                .isInstanceOf(AppointmentConflictException.class)
                .hasMessageContaining("weekend");
    }

    @Test
    @DisplayName("Powinien rzucić wyjątek dla wizyty w niedzielę")
    void checkClinicHours_shouldThrow_onSunday() {
        LocalDateTime sunday = LocalDateTime.of(2025, 6, 15, 12, 0);
        assertThatThrownBy(() -> appointmentService.checkClinicHours(sunday))
                .isInstanceOf(AppointmentConflictException.class)
                .hasMessageContaining("weekend");
    }

    @Test
    @DisplayName("Powinien rzucić wyjątek dla wizyty przed otwarciem (7:59)")
    void checkClinicHours_shouldThrow_beforeOpeningTime() {
        LocalDateTime tooEarly = LocalDateTime.of(2025, 6, 16, 7, 59);
        assertThatThrownBy(() -> appointmentService.checkClinicHours(tooEarly))
                .isInstanceOf(AppointmentConflictException.class)
                .hasMessageContaining("poza godzinami otwarcia");
    }

    @Test
    @DisplayName("Powinien rzucić wyjątek dla wizyty o godzinie zamknięcia (18:00)")
    void checkClinicHours_shouldThrow_atClosingTime() {
        LocalDateTime atClose = LocalDateTime.of(2025, 6, 16, 18, 0);
        assertThatThrownBy(() -> appointmentService.checkClinicHours(atClose))
                .isInstanceOf(AppointmentConflictException.class)
                .hasMessageContaining("poza godzinami otwarcia");
    }

    @Test
    @DisplayName("Powinien zaakceptować wizytę o 8:00 (dokładnie przy otwarciu)")
    void checkClinicHours_shouldPass_atOpeningTime() {
        LocalDateTime atOpen = LocalDateTime.of(2025, 6, 16, 8, 0);
        assertThatCode(() -> appointmentService.checkClinicHours(atOpen))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Powinien zaakceptować wizytę o 17:59 (ostatnia minuta przed zamknięciem)")
    void checkClinicHours_shouldPass_oneMinuteBeforeClose() {
        LocalDateTime lastMinute = LocalDateTime.of(2025, 6, 16, 17, 59);
        assertThatCode(() -> appointmentService.checkClinicHours(lastMinute))
                .doesNotThrowAnyException();
    }
}