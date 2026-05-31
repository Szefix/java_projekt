package com.veterinary.clinic.services;

import com.veterinary.clinic.dto.PatientDTO;
import com.veterinary.clinic.exceptions.ResourceNotFoundException;
import com.veterinary.clinic.models.Patient;
import com.veterinary.clinic.repositories.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testy jednostkowe PatientService")
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient testPatient;

    @BeforeEach
    void setUp() {
        testPatient = new Patient("Burek", "Pies", "Jan Kowalski");
        testPatient.setId(1L);
        testPatient.setOwnerPhone("123456789");
        testPatient.setOwnerEmail("jan@kowalski.pl");
    }

    // =========================================================
    //  POBIERANIE
    // =========================================================

    @Test
    @DisplayName("Powinien zwrócić listę wszystkich pacjentów")
    void getAllPatients_shouldReturnList() {
        // GIVEN
        Patient patient2 = new Patient("Mruczek", "Kot", "Maria Nowak");
        patient2.setId(2L);
        when(patientRepository.findAll()).thenReturn(List.of(testPatient, patient2));

        // WHEN
        List<PatientDTO> result = patientService.getAllPatients();

        // THEN
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Burek");
        assertThat(result.get(1).getName()).isEqualTo("Mruczek");
    }

    @Test
    @DisplayName("Powinien zwrócić pacjenta po ID")
    void getPatientById_shouldReturnPatient_whenExists() {
        // GIVEN
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));

        // WHEN
        PatientDTO result = patientService.getPatientById(1L);

        // THEN
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Burek");
        assertThat(result.getSpecies()).isEqualTo("Pies");
        assertThat(result.getOwnerName()).isEqualTo("Jan Kowalski");
        assertThat(result.getOwnerPhone()).isEqualTo("123456789");
        assertThat(result.getOwnerEmail()).isEqualTo("jan@kowalski.pl");
    }

    @Test
    @DisplayName("Powinien rzucić wyjątek gdy pacjent nie istnieje")
    void getPatientById_shouldThrow_whenNotFound() {
        // GIVEN
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> patientService.getPatientById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // =========================================================
    //  TWORZENIE
    // =========================================================

    @Test
    @DisplayName("Powinien zapisać nowego pacjenta z telefonem i emailem")
    void createPatient_shouldSaveWithPhoneAndEmail() {
        // GIVEN
        PatientDTO dto = new PatientDTO(null, "Burek", "Pies", "Jan Kowalski",
                "123456789", "jan@kowalski.pl");
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        // WHEN
        PatientDTO result = patientService.createPatient(dto);

        // THEN
        assertThat(result.getOwnerPhone()).isEqualTo("123456789");
        assertThat(result.getOwnerEmail()).isEqualTo("jan@kowalski.pl");
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    @DisplayName("Powinien zapisać pacjenta bez telefonu i emaila (pola opcjonalne)")
    void createPatient_shouldSave_whenPhoneAndEmailAreNull() {
        // GIVEN
        Patient patientNoContact = new Patient("Reksio", "Pies", "Piotr Zając");
        patientNoContact.setId(3L);
        PatientDTO dto = new PatientDTO(null, "Reksio", "Pies", "Piotr Zając", null, null);
        when(patientRepository.save(any(Patient.class))).thenReturn(patientNoContact);

        // WHEN & THEN
        assertThatCode(() -> patientService.createPatient(dto))
                .doesNotThrowAnyException();

        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    // =========================================================
    //  AKTUALIZACJA
    // =========================================================

    @Test
    @DisplayName("Powinien zaktualizować dane pacjenta wraz z telefonem i emailem")
    void updatePatient_shouldUpdateAllFields() {
        // GIVEN
        PatientDTO dto = new PatientDTO(null, "Burek", "Pies", "Jan Kowalski",
                "987654321", "nowy@mail.pl");
        Patient updated = new Patient("Burek", "Pies", "Jan Kowalski");
        updated.setId(1L);
        updated.setOwnerPhone("987654321");
        updated.setOwnerEmail("nowy@mail.pl");

        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(patientRepository.save(any(Patient.class))).thenReturn(updated);

        // WHEN
        PatientDTO result = patientService.updatePatient(1L, dto);

        // THEN
        assertThat(result.getOwnerPhone()).isEqualTo("987654321");
        assertThat(result.getOwnerEmail()).isEqualTo("nowy@mail.pl");
    }

    @Test
    @DisplayName("Powinien rzucić wyjątek przy aktualizacji nieistniejącego pacjenta")
    void updatePatient_shouldThrow_whenNotFound() {
        // GIVEN
        PatientDTO dto = new PatientDTO(null, "Burek", "Pies", "Jan Kowalski", null, null);
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> patientService.updatePatient(99L, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // =========================================================
    //  USUWANIE
    // =========================================================

    @Test
    @DisplayName("Powinien usunąć pacjenta gdy istnieje")
    void deletePatient_shouldDelete_whenExists() {
        // GIVEN
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));

        // WHEN
        patientService.deletePatient(1L);

        // THEN
        verify(patientRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Powinien rzucić wyjątek przy usuwaniu nieistniejącego pacjenta")
    void deletePatient_shouldThrow_whenNotFound() {
        // GIVEN
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> patientService.deletePatient(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(patientRepository, never()).deleteById(any());
    }

    // =========================================================
    //  WYSZUKIWANIE
    // =========================================================

    @Test
    @DisplayName("Powinien zwrócić pacjentów po gatunku")
    void getPatientsBySpecies_shouldReturnFiltered() {
        // GIVEN
        when(patientRepository.findBySpeciesIgnoreCase("Pies"))
                .thenReturn(List.of(testPatient));

        // WHEN
        List<PatientDTO> result = patientService.getPatientsBySpecies("Pies");

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSpecies()).isEqualTo("Pies");
    }

    @Test
    @DisplayName("Powinien zwrócić pacjentów po nazwisku właściciela")
    void getPatientsByOwner_shouldReturnFiltered() {
        // GIVEN
        when(patientRepository.findByOwnerNameContainingIgnoreCase("Kowalski"))
                .thenReturn(List.of(testPatient));

        // WHEN
        List<PatientDTO> result = patientService.getPatientsByOwner("Kowalski");

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOwnerName()).isEqualTo("Jan Kowalski");
    }

    @Test
    @DisplayName("Powinien zwrócić pustą listę gdy brak pacjentów danego gatunku")
    void getPatientsBySpecies_shouldReturnEmpty_whenNoneFound() {
        // GIVEN
        when(patientRepository.findBySpeciesIgnoreCase("Chomik"))
                .thenReturn(List.of());

        // WHEN
        List<PatientDTO> result = patientService.getPatientsBySpecies("Chomik");

        // THEN
        assertThat(result).isEmpty();
    }
}