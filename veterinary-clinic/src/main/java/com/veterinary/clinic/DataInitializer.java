package com.veterinary.clinic;

import com.veterinary.clinic.models.Appointment;
import com.veterinary.clinic.models.Doctor;
import com.veterinary.clinic.models.Patient;
import com.veterinary.clinic.repositories.AppointmentRepository;
import com.veterinary.clinic.repositories.DoctorRepository;
import com.veterinary.clinic.repositories.PatientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    public DataInitializer(DoctorRepository doctorRepository,
                           PatientRepository patientRepository,
                           AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public void run(String... args) {
        // Dodaj dane startowe tylko jeśli baza jest pusta
        if (doctorRepository.count() > 0) {
            System.out.println("Baza danych już zawiera dane, pomijam inicjalizacje");
            return;
        }

        // ===== Lekarze =====
        Doctor dr1 = doctorRepository.save(new Doctor("Anna",    "Kowalska",  "Weterynarz ogólny"));
        Doctor dr2 = doctorRepository.save(new Doctor("Marek",   "Nowak",     "Weterynarz chirurg"));
        Doctor dr3 = doctorRepository.save(new Doctor("Katarzyna","Wiśniewska","Weterynarz egzotyczny"));

        // ===== Pacjenci =====
        Patient p1 = patientRepository.save(new Patient("Burek",   "Pies", "Jan Kowalski"));
        Patient p2 = patientRepository.save(new Patient("Mruczek", "Kot",  "Maria Nowak"));
        Patient p3 = patientRepository.save(new Patient("Złotka",  "Pies", "Piotr Zając"));
        Patient p4 = patientRepository.save(new Patient("Filemon", "Kot",  "Anna Wróbel"));

        // ===== Wizyty =====
        LocalDateTime base = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);

        appointmentRepository.save(new Appointment(base, "Badanie kontrolne po operacji", dr1, p1));
        appointmentRepository.save(new Appointment(base.plusHours(1), "Alergia skórna – pierwsza wizyta", dr2, p2));
        appointmentRepository.save(new Appointment(base.plusHours(2), "Szczepienie coroczne", dr3, p3));
        appointmentRepository.save(new Appointment(base.plusMinutes(60), "Kastracja – konsultacja przed zabiegiem", dr1, p4));

        System.out.println("=== Dane testowe załadowane pomyślnie ===");
        System.out.println("Lekarze: " + doctorRepository.count());
        System.out.println("Pacjenci: " + patientRepository.count());
        System.out.println("Wizyty: " + appointmentRepository.count());
    }
}