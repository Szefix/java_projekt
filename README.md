# Veterinary Clinic Management System

## Opis Projektu
System Zarządzania Kliniką Weterynaryjną to kompletny, wielowarstwowy system backendowy (REST API) zbudowany w oparciu o framework Spring Boot 3, zintegrowany z lekkim graficznym panelem użytkownika. Projekt został zrobiony z myślą o pełnej automatyzacji procesów w klinice weterynaryjnej, oferując mechanizmy zarządzania lekarzami, pacjentami (zwierzętami) oraz rezerwacją wizyt.

Aplikacja implementuje rygorystyczną logikę biznesową (walidację czasu pracy oraz konfliktów terminów), wykorzystuje nowoczesny wzorzec separacji warstw za pomocą obiektów DTO, realizuje scentralizowaną, globalną obsługę błędów oraz dostarcza w pełni zautomatyzowane testy jednostkowe.

---

## Technologie i Biblioteki
* Język programowania: Java 17
* Framework główny: Spring Boot 3.2.5
* Warstwa danych (ORM): Spring Data JPA / Hibernate
* Baza danych: SQLite (zapis w pliku lokalnym)
* Interfejs graficzny: Vanilla HTML, CSS, JavaScript (Fetch API)
* Walidacja danych: Spring Boot Starter Validation (adnotacje @Valid, @NotBlank, @NotNull, @Email, @Pattern)
* Dokumentacja i interfejs API: Springdoc OpenAPI / Swagger UI (wersja 2.5.0)
* Narzędzie do budowania: Maven
* Testowanie: JUnit 5, Mockito, AssertJ

---

## Kluczowe Funkcjonalności i Logika Biznesowa

### 1. Zarządzanie Lekarzami Weterynarii (/api/doctors)
* Pełny zestaw operacji CRUD (dodawanie, wyświetlanie, edycja, usuwanie).
* Dynamiczne i opcjonalne filtrowanie listy lekarzy po ich specjalizacji (z ignorowaniem wielkości liter).

### 2. Zarządzanie Pacjentami / Zwierzętami (/api/patients)
* Rejestracja kartotek zwierząt wraz ze szczegółowymi informacjami kontaktowymi właściciela.
* Zaawansowana walidacja danych wejściowych: system weryfikuje poprawność formatu adresu e-mail oraz wymusza podanie dokładnie 9-cyfrowego numeru telefonu komórkowego.
* Zaawansowane wyszukiwanie pacjentów po gatunku zwierzęcia lub nazwisku opiekuna.

### 3. Zaawansowany System Rezerwacji Wizyt (/api/appointments)
Warstwa AppointmentService realizuje kluczowe wymagania biznesowe systemu:
* Weryfikacja godzin otwarcia kliniki: System akceptuje rezerwacje wyłącznie w dni robocze (od poniedziałku do piątku) w godzinach urzędowania (08:00 – 18:00). Próba rejestracji w weekend lub poza tymi godzinami kończy się natychmiastowym zablokowaniem żądania.
* Algorytm zapobiegania konfliktom (Okno 30 minut): Przy tworzeniu lub edycji wizyty system sprawdza, czy dany lekarz nie ma już przypisanego innego zadania w przedziale ±30 minut od proponowanego terminu. Walidacja ta opiera się na natywnym zapytaniu JPQL @Query w bazie danych. Przy aktualizacji wizyty system wyklucza z walidacji ID modyfikowanego obiektu, zapobiegając konfliktom "z samym sobą".
* Cykl życia wizyty (Statusy): Każda wizyta posiada swój status (PLANNED, COMPLETED, CANCELED). Pozwala to na odwoływanie wizyt z poziomu panelu użytkownika bez ich fizycznego usuwania, co zapewnia stałą historię operacji w klinice.

### 4. Scentralizowana Obsługa Wyjątków (GlobalExceptionHandler)
Aplikacja nie eksponuje surowych błędów środowiska Java Runtime. Wszystkie sytuacje wyjątkowe są przechwytywane i mapowane do czytelnego formatu JSON (ErrorResponse jako rekord):
* 400 Bad Request – błędy walidacji formularzy (np. brakujące imię, zły format e-mail, błędny numer telefonu) zwracane jako mapa konkretnych pól i komunikatów.
* 404 Not Found – próba odpytania o nieistniejącego lekarza, pacjenta lub wizytę.
* 409 Conflict – nieprzestrzeganie reguł biznesowych (nakładanie się wizyt, niedozwolone operacje na statusach lub próba rejestracji poza godzinami pracy).

### 5. Automatyczny Audyt Danych (Auditing)
Dzięki klasie BaseEntity i adnotacji @EnableJpaAuditing, każdy nowy rekord i każda modyfikacja w bazie są automatycznie oznaczane precyzyjną datą i godziną wykonania akcji.

---

## Struktura Katalogów
```text
veterinary-clinic/
├── src/main/java/com/veterinary/clinic/
│   ├── controllers/       # Odpowiadają za routing i endpointy REST (np. AppointmentController)
│   ├── dto/               # Bezpieczne obiekty transferu danych (ukrywają encje)
│   ├── exceptions/        # Globalna obsługa błędów (GlobalExceptionHandler)
│   ├── models/            # Encje bazodanowe i Enumsy (Doctor, Patient, Appointment)
│   ├── repositories/      # Interfejsy Spring Data JPA (komunikacja z bazą SQLite)
│   ├── services/          # Główna logika biznesowa i walidacja
│   ├── DataInitializer.java        # Skrypt generujący dane startowe
│   └── VeterinaryClinicApplication.java
└── src/main/resources/
    ├── static/
    │   └── index.html     # Interfejs graficzny użytkownika (Frontend SPA)
    └── application.properties # Konfiguracja bazy danych i serwera
```
---

## Architektura Projektu

Projekt opiera się na sprawdzonym wzorcu wielowarstwowym, zapewniając pełną separację odpowiedzialności:

* **Warstwa Prezentacji (UI & Controllers):** Interfejs graficzny w pliku index.html oraz punkty wejściowe REST API obsługujące żądania sieciowe.
* **Warstwa Logiki Biznesowej (Services):** Główne mechanizmy systemu, odpowiadające za walidację reguł (np. zapobieganie konfliktom terminów).
* **Warstwa Dostępu do Danych (Repositories):** Interfejsy zarządzające komunikacją z bazą danych poprzez Spring Data JPA.
* **Modele i DTO (Models / DTO):** Ścisły podział na wewnętrzne encje bazodanowe oraz bezpieczne obiekty transferu danych.
* **Obsługa Błędów (Exceptions):** Scentralizowany mechanizm przechwytywania wyjątków.

---

## Uruchomienie Projektu

### Wymagania wstępne
* Zainstalowane środowisko JDK 17 (lub nowsze, skonfigurowane jako Project SDK).
* Zainstalowany Maven (lub wbudowany w IDE).

Aplikacja wystartuje na domyślnym porcie 8080. Plik DataInitializer automatycznie wprowadzi do bazy danych przykładowe wpisy (3 lekarzy, 4 pacjentów, 4 wizyty), wyświetlając stosowne potwierdzenie w logach konsoli.

Aby uruchomić aplikację, wykonaj w terminalu:
mvn spring-boot:run

---

## Interfejsy Deweloperskie i Panel Użytkownika

Po uruchomieniu systemu masz do dyspozycji następujące narzędzia:

* **Panel Użytkownika (GUI):**
  Wystarczy otworzyć plik index.html w przeglądarce internetowej. Znajdziesz tam kompletny, interaktywny interfejs pozwalający na łatwe dodawanie, usuwanie i odwoływanie wizyt czy pacjentów wraz z wymuszeniem odpowiednich danych kontaktowych.

* **DB Browser for SQLite:**
  W głównym folderze projektu znajduje się narzędzie DB Browser for SQLite. Po jego uruchomieniu wystarczy otworzyć wygenerowany plik bazy veterinary_clinic.db, aby mieć swobodny i przejrzysty dostęp do struktury tabel, wprowadzonych rekordów i znaczników czasowych audytu.

* **Swagger UI (Centrum Zarządzania API):** http://localhost:8080/swagger-ui.html
  Interaktywny panel pozwalający na ręczne wywoływanie wszystkich żądań (GET, POST, PUT, DELETE), testowanie walidacji pól oraz sprawdzanie struktur modeli JSON.

---

## Testy Automatyczne (plik AppointmentServiceTest)
System posiada zestaw 36 kompleksowych, izolowanych testów jednostkowych napisanych przy użyciu frameworka Mockito i JUnit 5, które weryfikują poprawność działania kluczowych algorytmów rezerwacyjnych:

### AppointmentServiceTest (14 testów)
Weryfikacja logiki rezerwacji wizyt:
1. createAppointment_shouldSave_whenNoConflict – Zapis wizyty gdy termin jest wolny.
2. createAppointment_shouldThrow_whenExactSameTime – Blokada gdy lekarz ma wizytę o tej samej godzinie.
3. createAppointment_shouldThrow_whenConflictWithin30Minutes – Blokada gdy wizyta jest w oknie 30 minut.
4. createAppointment_shouldSave_whenPreviousAppointmentIs31MinutesBefore – Zapis gdy poprzednia wizyta jest 31 minut wcześniej.
5. updateAppointment_shouldExcludeSelfFromConflictCheck – Edycja nie generuje konfliktu z samą sobą.
6. getAppointmentById_shouldThrow_whenNotFound – Błąd 404 dla nieistniejącego ID.
7. deleteAppointment_shouldThrow_whenNotFound – Błąd przy usuwaniu nieistniejącej wizyty.
8. checkClinicHours_shouldPass_onWeekdayWithinHours – Akceptacja wizyty w dzień roboczy w godzinach pracy.
9. checkClinicHours_shouldThrow_onSaturday – Blokada wizyty w sobotę.
10. checkClinicHours_shouldThrow_onSunday – Blokada wizyty w niedzielę.
11. checkClinicHours_shouldThrow_beforeOpeningTime – Blokada wizyty przed godziną otwarcia (07:59).
12. checkClinicHours_shouldThrow_atClosingTime – Blokada wizyty o godzinie zamknięcia (18:00).
13. checkClinicHours_shouldPass_atOpeningTime – Akceptacja wizyty o 08:00.
14. checkClinicHours_shouldPass_oneMinuteBeforeClose – Akceptacja wizyty o 17:59.

### DoctorServiceTest (10 testów)
Weryfikacja operacji CRUD dla lekarzy:
1. getAllDoctors_shouldReturnList – Zwrócenie listy wszystkich lekarzy.
2. getDoctorById_shouldReturnDoctor_whenExists – Pobranie lekarza po ID.
3. getDoctorById_shouldThrow_whenNotFound – Błąd 404 dla nieistniejącego lekarza.
4. createDoctor_shouldSaveAndReturnDTO – Zapis nowego lekarza i zwrot DTO.
5. updateDoctor_shouldUpdateAndReturnDTO – Aktualizacja danych lekarza.
6. updateDoctor_shouldThrow_whenNotFound – Błąd przy aktualizacji nieistniejącego lekarza.
7. deleteDoctor_shouldDelete_whenExists – Usunięcie lekarza gdy istnieje.
8. deleteDoctor_shouldThrow_whenNotFound – Błąd przy usuwaniu nieistniejącego lekarza.
9. getDoctorsBySpecialization_shouldReturnFiltered – Filtrowanie lekarzy po specjalizacji.
10. getDoctorsBySpecialization_shouldReturnEmpty_whenNoneFound – Pusta lista gdy brak lekarzy o danej specjalizacji.

### PatientServiceTest (12 testów)
Weryfikacja operacji CRUD dla pacjentów z walidacją danych kontaktowych:
1. getAllPatients_shouldReturnList – Zwrócenie listy wszystkich pacjentów.
2. getPatientById_shouldReturnPatient_whenExists – Pobranie pacjenta po ID wraz z telefonem i emailem.
3. getPatientById_shouldThrow_whenNotFound – Błąd 404 dla nieistniejącego pacjenta.
4. createPatient_shouldSaveWithPhoneAndEmail – Zapis pacjenta z telefonem i emailem.
5. createPatient_shouldSave_whenPhoneAndEmailAreNull – Zapis pacjenta gdy dane kontaktowe są opcjonalne.
6. updatePatient_shouldUpdateAllFields – Aktualizacja wszystkich pól włącznie z danymi kontaktowymi.
7. updatePatient_shouldThrow_whenNotFound – Błąd przy aktualizacji nieistniejącego pacjenta.
8. deletePatient_shouldDelete_whenExists – Usunięcie pacjenta gdy istnieje.
9. deletePatient_shouldThrow_whenNotFound – Błąd przy usuwaniu nieistniejącego pacjenta.
10. getPatientsBySpecies_shouldReturnFiltered – Filtrowanie pacjentów po gatunku.
11. getPatientsByOwner_shouldReturnFiltered – Filtrowanie pacjentów po nazwisku właściciela.
12. getPatientsBySpecies_shouldReturnEmpty_whenNoneFound – Pusta lista gdy brak pacjentów danego gatunku.

Aby uruchomić testy w środowisku IntelliJ, kliknij prawym przyciskiem myszy na folder src/test/java i wybierz opcję Run 'All Tests' lub użyj narzędzia Maven:
mvn test