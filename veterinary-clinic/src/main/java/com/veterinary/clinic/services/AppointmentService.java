package com.veterinary.clinic.services;

import com.veterinary.clinic.dto.AppointmentDTO;
import com.veterinary.clinic.dto.AppointmentRequestDTO;
import com.veterinary.clinic.exceptions.AppointmentConflictException;
import com.veterinary.clinic.exceptions.ResourceNotFoundException;
import com.veterinary.clinic.models.Appointment;
import com.veterinary.clinic.models.Doctor;
import com.veterinary.clinic.models.Patient;
import com.veterinary.clinic.repositories.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Service
@Transactional
public class AppointmentService {

    // Margines czasu (minuty) – dwie wizyty nie mogą być bliżej niż 30 minut
    public static final int APPOINTMENT_WINDOW_MINUTES = 30;
    public static final LocalTime CLINIC_OPEN = LocalTime.of(8, 0);
    public static final LocalTime CLINIC_CLOSE = LocalTime.of(18, 0);


    private final AppointmentRepository appointmentRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              DoctorService doctorService,
                              PatientService patientService) {
        this.appointmentRepository = appointmentRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    // ===== Pobranie wszystkich wizyt =====
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAllAppointments() {
        return appointmentRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ===== Pobranie wizyty po ID =====
    @Transactional(readOnly = true)
    public AppointmentDTO getAppointmentById(Long id) {
        Appointment appointment = findAppointmentOrThrow(id);
        return mapToDTO(appointment);
    }

    // ===== Pobranie wizyt lekarza =====
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsByDoctor(Long doctorId) {
        // Sprawdź czy lekarz istnieje
        doctorService.getDoctorById(doctorId);
        return appointmentRepository.findByDoctorId(doctorId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ===== Pobranie wizyt pacjenta =====
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointmentsByPatient(Long patientId) {
        // Sprawdź czy pacjent istnieje
        patientService.getPatientById(patientId);
        return appointmentRepository.findByPatientId(patientId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ===== Rejestracja nowej wizyty =====
    public AppointmentDTO createAppointment(AppointmentRequestDTO requestDTO) {
        Doctor doctor = doctorService.getDoctorEntityById(requestDTO.getDoctorId());
        Patient patient = patientService.getPatientEntityById(requestDTO.getPatientId());

        // WALIDACJA BIZNESOWA: sprawdzenie konfliktów terminu
        checkClinicHours(requestDTO.getDateTime());
        checkForConflicts(doctor.getId(), requestDTO.getDateTime(), null);

        Appointment appointment = new Appointment(
                requestDTO.getDateTime(),
                requestDTO.getDescription(),
                doctor,
                patient
        );

        Appointment saved = appointmentRepository.save(appointment);
        return mapToDTO(saved);
    }

    // ===== Aktualizacja wizyty =====
    public AppointmentDTO updateAppointment(Long id, AppointmentRequestDTO requestDTO) {
        Appointment appointment = findAppointmentOrThrow(id);
        Doctor doctor = doctorService.getDoctorEntityById(requestDTO.getDoctorId());
        Patient patient = patientService.getPatientEntityById(requestDTO.getPatientId());

        // WALIDACJA BIZNESOWA: sprawdzenie konfliktów z wykluczeniem bieżącej wizyty
        checkClinicHours(requestDTO.getDateTime());
        checkForConflicts(doctor.getId(), requestDTO.getDateTime(), id);

        appointment.setDateTime(requestDTO.getDateTime());
        appointment.setDescription(requestDTO.getDescription());
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);

        Appointment updated = appointmentRepository.save(appointment);
        return mapToDTO(updated);
    }

    // ===== Usunięcie wizyty =====
    public void deleteAppointment(Long id) {
        findAppointmentOrThrow(id);
        appointmentRepository.deleteById(id);
    }

    // =========================================================
    // LOGIKA BIZNESOWA: Walidacja konfliktu terminów wizyt
    // =========================================================

    /**
     * Sprawdza, czy dany lekarz nie ma już zaplanowanej wizyty
     * w oknie czasowym ±APPOINTMENT_WINDOW_MINUTES od podanej daty.
     *
     * @param doctorId  ID lekarza
     * @param dateTime  Proponowana data i godzina wizyty
     * @param excludeId ID wizyty do wykluczenia (przy aktualizacji), null przy tworzeniu
     * @throws AppointmentConflictException jeśli lekarz ma już wizytę w tym czasie
     */
    public void checkForConflicts(Long doctorId, LocalDateTime dateTime, Long excludeId) {
        LocalDateTime windowStart = dateTime.minusMinutes(APPOINTMENT_WINDOW_MINUTES);
        LocalDateTime windowEnd   = dateTime.plusMinutes(APPOINTMENT_WINDOW_MINUTES);

        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                doctorId, windowStart, windowEnd, excludeId
        );

        if (!conflicts.isEmpty()) {
            Appointment conflict = conflicts.get(0);
            throw new AppointmentConflictException(
                    String.format(
                            "Lekarz ma już zaplanowaną wizytę w dniu %s o godzinie %s. " +
                                    "Nowa wizyta musi być oddalona o co najmniej %d minut.",
                            conflict.getDateTime().toLocalDate(),
                            conflict.getDateTime().toLocalTime(),
                            APPOINTMENT_WINDOW_MINUTES
                    )
            );
        }
    }

    // ===== Mapowanie encja → DTO =====
    private AppointmentDTO mapToDTO(Appointment a) {
        return new AppointmentDTO(
                a.getId(),
                a.getDateTime(),
                a.getDescription(),
                a.getDoctor().getId(),
                a.getDoctor().getFullName(),
                a.getDoctor().getSpecialization(),
                a.getPatient().getId(),
                a.getPatient().getName(),
                a.getPatient().getSpecies(),
                a.getPatient().getOwnerName()
        );
    }

    private Appointment findAppointmentOrThrow(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wizyta", id));
    }

    public void checkClinicHours(LocalDateTime dateTime) {
        DayOfWeek day = dateTime.getDayOfWeek();
        LocalTime time = dateTime.toLocalTime();

        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            throw new AppointmentConflictException(
                    String.format(
                            "Klinika jest nieczynna w weekendy. " +
                                    "Wybierz termin od poniedziałku do piątku (godziny %s–%s).",
                            CLINIC_OPEN, CLINIC_CLOSE
                    )
            );
        }

        if (time.isBefore(CLINIC_OPEN) || !time.isBefore(CLINIC_CLOSE)) {
            throw new AppointmentConflictException(
                    String.format(
                            "Wizyta o godzinie %s jest poza godzinami otwarcia kliniki (%s–%s).",
                            time, CLINIC_OPEN, CLINIC_CLOSE
                    )
            );
        }
    }
}