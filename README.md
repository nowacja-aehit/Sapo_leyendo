# Sapo Leyendo WMS

Sapo Leyendo WMS to system zarządzania magazynem (Warehouse Management System) stworzony w technologii Java (Spring Boot) z frontendem w React.

## Wymagania

*   Java 21
*   Maven
*   Node.js (v20+)

## Uruchomienie aplikacji

### Backend (Spring Boot)

1.  Otwórz terminal w głównym katalogu projektu.
2.  Uruchom aplikację za pomocą Maven:

    ```bash
    mvn spring-boot:run
    ```

    Aplikacja backendowa uruchomi się domyślnie na porcie `8080`.

### Frontend (React)

1.  Otwórz nowy terminal i przejdź do katalogu `frontend`:

    ```bash
    cd frontend
    ```

2.  Zainstaluj zależności (tylko przy pierwszym uruchomieniu):

    ```bash
    npm install
    ```

3.  Uruchom serwer deweloperski:

    ```bash
    npm run dev
    ```

    Aplikacja frontendowa będzie dostępna pod adresem: [http://localhost:5173](http://localhost:5173)

## Logowanie

Domyślne konto administratora:

*   **Login:** `admin`
*   **Hasło:** `password`

## Główne funkcjonalności

*   **Zarządzanie produktami:** Dodawanie, edycja i przeglądanie produktów.
*   **Zarządzanie użytkownikami:** Kontrola dostępu i ról użytkowników.
*   **Lokalizacje:** Definiowanie struktury magazynu.
*   **Inbound (Przyjęcia):** Planowanie dostaw, rezerwacja doków, przyjęcia towaru (LPN).
*   **Inventory (Zapasy):** Podgląd stanów magazynowych.
*   **Picking (Kompletacja):** Tworzenie fal (Wave Planning), alokacja zapasów, zadania kompletacji.
*   **Packing (Pakowanie):** Stanowisko pakowania, tworzenie przesyłek i paczek.
*   **Shipping (Wysyłka):** Planowanie transportów, przypisywanie przesyłek, generowanie manifestów.
*   **Quality Control (Kontrola Jakości):** Inspekcje, plany testów (AQL), raporty niezgodności (NCR).

## Baza danych

Projekt wykorzystuje wbudowaną bazę danych SQLite. Plik bazy danych `sapo_wms_main.db` tworzony jest automatycznie w katalogu `data/` przy pierwszym uruchomieniu.
Dane początkowe są ładowane z pliku `src/main/resources/database/FillDatabase_sqlite.sql`.
