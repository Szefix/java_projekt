package com.veterinary.clinic.repositories;

import com.veterinary.clinic.models.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Wszystkie wizyty danego lekarza
    List<Appointment> findByDoctorId(Long doctorId);

    // Wszystkie wizyty danego pacjenta
    List<Appointment> findByPatientId(Long patientId);

    // Kluczowe zapytanie: czy lekarz ma wizytę w przedziale czasowym (do walidacji konfliktu)
    // Zwraca wizyty lekarza, których czas mieści się w oknie [windowStart, windowEnd]
    @Query("""
            SELECT a FROM Appointment a
            WHERE a.doctor.id = :doctorId
              AND a.dateTime >= :windowStart
              AND a.dateTime <= :windowEnd
              AND (:excludeId IS NULL OR a.id <> :excludeId)
            """)
    List<Appointment> findConflictingAppointments(
            @Param("doctorId") Long doctorId,
            @Param("windowStart") LocalDateTime windowStart,
            @Param("windowEnd") LocalDateTime windowEnd,
            @Param("excludeId") Long excludeId
    );

    // Wizyty w określonym przedziale dat
    List<Appointment> findByDateTimeBetweenOrderByDateTimeAsc(LocalDateTime from, LocalDateTime to);
}