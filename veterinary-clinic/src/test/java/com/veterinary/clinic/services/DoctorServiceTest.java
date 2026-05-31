package com.veterinary.clinic.services;

import com.veterinary.clinic.dto.DoctorDTO;
import com.veterinary.clinic.exceptions.ResourceNotFoundException;
import com.veterinary.clinic.models.Doctor;
import com.veterinary.clinic.repositories.DoctorRepository;
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
@DisplayName("Testy jednostkowe DoctorService")
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorService doctorService;

    private Doctor testDoctor;

    @BeforeEach
    void setUp() {
        testDoctor = new Doctor("Anna", "Kowalska", "Chirurg");
        testDoctor.setId(1L);
    }

    // =========================================================
    //  POBIERANIE
    // =========================================================

    @Test
    @DisplayName("Powinien zwrócić listę wszystkich lekarzy")
    void getAllDoctors_shouldReturnList() {
        // GIVEN
        Doctor doctor2 = new Doctor("Marek", "Nowak", "Dermatolog");
        doctor2.setId(2L);
        when(doctorRepository.findAll()).thenReturn(List.of(testDoctor, doctor2));

        // WHEN
        List<DoctorDTO> result = doctorService.getAllDoctors();

        // THEN
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFirstName()).isEqualTo("Anna");
        assertThat(result.get(1).getFirstName()).isEqualTo("Marek");
    }

    @Test
    @DisplayName("Powinien zwrócić lekarza po ID")
    void getDoctorById_shouldReturnDoctor_whenExists() {
        // GIVEN
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));

        // WHEN
        DoctorDTO result = doctorService.getDoctorById(1L);

        // THEN
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("Anna");
        assertThat(result.getLastName()).isEqualTo("Kowalska");
        assertThat(result.getSpecialization()).isEqualTo("Chirurg");
    }

    @Test
    @DisplayName("Powinien rzucić wyjątek gdy lekarz nie istnieje")
    void getDoctorById_shouldThrow_whenNotFound() {
        // GIVEN
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> doctorService.getDoctorById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // =========================================================
    //  TWORZENIE
    // =========================================================

    @Test
    @DisplayName("Powinien zapisać nowego lekarza i zwrócić DTO")
    void createDoctor_shouldSaveAndReturnDTO() {
        // GIVEN
        DoctorDTO dto = new DoctorDTO(null, "Anna", "Kowalska", "Chirurg");
        when(doctorRepository.save(any(Doctor.class))).thenReturn(testDoctor);

        // WHEN
        DoctorDTO result = doctorService.createDoctor(dto);

        // THEN
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("Anna");
        verify(doctorRepository, times(1)).save(any(Doctor.class));
    }

    // =========================================================
    //  AKTUALIZACJA
    // =========================================================

    @Test
    @DisplayName("Powinien zaktualizować dane lekarza")
    void updateDoctor_shouldUpdateAndReturnDTO() {
        // GIVEN
        DoctorDTO dto = new DoctorDTO(null, "Anna", "Nowak", "Internista");
        Doctor updated = new Doctor("Anna", "Nowak", "Internista");
        updated.setId(1L);

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(updated);

        // WHEN
        DoctorDTO result = doctorService.updateDoctor(1L, dto);

        // THEN
        assertThat(result.getLastName()).isEqualTo("Nowak");
        assertThat(result.getSpecialization()).isEqualTo("Internista");
    }

    @Test
    @DisplayName("Powinien rzucić wyjątek przy aktualizacji nieistniejącego lekarza")
    void updateDoctor_shouldThrow_whenNotFound() {
        // GIVEN
        DoctorDTO dto = new DoctorDTO(null, "Anna", "Nowak", "Internista");
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> doctorService.updateDoctor(99L, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // =========================================================
    //  USUWANIE
    // =========================================================

    @Test
    @DisplayName("Powinien usunąć lekarza gdy istnieje")
    void deleteDoctor_shouldDelete_whenExists() {
        // GIVEN
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));

        // WHEN
        doctorService.deleteDoctor(1L);

        // THEN
        verify(doctorRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Powinien rzucić wyjątek przy usuwaniu nieistniejącego lekarza")
    void deleteDoctor_shouldThrow_whenNotFound() {
        // GIVEN
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> doctorService.deleteDoctor(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(doctorRepository, never()).deleteById(any());
    }

    // =========================================================
    //  WYSZUKIWANIE
    // =========================================================

    @Test
    @DisplayName("Powinien zwrócić lekarzy po specjalizacji")
    void getDoctorsBySpecialization_shouldReturnFiltered() {
        // GIVEN
        when(doctorRepository.findBySpecializationIgnoreCase("Chirurg"))
                .thenReturn(List.of(testDoctor));

        // WHEN
        List<DoctorDTO> result = doctorService.getDoctorsBySpecialization("Chirurg");

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSpecialization()).isEqualTo("Chirurg");
    }

    @Test
    @DisplayName("Powinien zwrócić pustą listę gdy brak lekarzy o danej specjalizacji")
    void getDoctorsBySpecialization_shouldReturnEmpty_whenNoneFound() {
        // GIVEN
        when(doctorRepository.findBySpecializationIgnoreCase("Kardiolog"))
                .thenReturn(List.of());

        // WHEN
        List<DoctorDTO> result = doctorService.getDoctorsBySpecialization("Kardiolog");

        // THEN
        assertThat(result).isEmpty();
    }
}