# 📦 Sapo Leyendo WMS

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-18.x-blue?logo=react&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-3.x-blue?logo=sqlite&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.x-red?logo=apachemaven&logoColor=white)

Sapo Leyendo WMS to nowoczesny system zarządzania magazynem (Warehouse Management System) stworzony w technologii **Java Spring Boot** z responsywnym frontendem w **React**. System zapewnia kompleksowe rozwiązanie do zarządzania procesami magazynowymi.

## 📋 Spis treści

- [⚡ Wymagania](#-wymagania)
- [🚀 Uruchomienie aplikacji](#-uruchomienie-aplikacji)
- [🔐 Logowanie](#-logowanie)
- [🎯 Główne funkcjonalności](#-główne-funkcjonalności)
- [💾 Baza danych](#-baza-danych)
- [☁️ Deployment na Azure](#️-deployment-na-azure)

## 🚀 Uruchomienie aplikacji

### Backend (Spring Boot)

1.  Otwórz terminal w głównym katalogu projektu.
2.  Uruchom aplikację za pomocą Maven:

    ```bash
    mvn spring-boot:run
    ```

    Aplikacja backendowa uruchomi się domyślnie na porcie `8080`.

### Frontend (React)

1.  Otwórz nowy terminal i przejdź do katalogu `Visualization`:

    ```bash
    cd Visualization
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

## 🔐 Logowanie

Domyślne konto administratora:

| Pole | Wartość |
|------|---------|
| **Login (email)** | `admin@example.com` |
| **Hasło** | `password` |

## 🎯 Główne funkcjonalności

### 📦 Zarządzanie podstawowe
*   **Zarządzanie produktami:** Dodawanie, edycja i przeglądanie produktów.
*   **Zarządzanie użytkownikami:** Kontrola dostępu i ról użytkowników.
*   **Lokalizacje:** Definiowanie struktury magazynu.

### 🔄 Procesy magazynowe
*   **Inbound (Przyjęcia):** Planowanie dostaw, rezerwacja doków, przyjęcia towaru (LPN).
*   **Inventory (Zapasy):** Podgląd stanów magazynowych.
*   **Picking (Kompletacja):** Tworzenie fal (Wave Planning), alokacja zapasów, zadania kompletacji.
*   **Packing (Pakowanie):** Stanowisko pakowania, tworzenie przesyłek i paczek.
*   **Shipping (Wysyłka):** Planowanie transportów, przypisywanie przesyłek, generowanie manifestów.

### 🔍 Kontrola jakości
*   **Quality Control (Kontrola Jakości):** Inspekcje, plany testów (AQL), raporty niezgodności (NCR).

## 💾 Baza danych

Projekt wykorzystuje wbudowaną bazę danych **SQLite** dla łatwości uruchomienia i rozwoju.

### 📁 Lokalizacja i struktura
- **Plik bazy danych:** `sapo_wms_main.db` 
- **Katalog:** `data/` (tworzony automatycznie przy pierwszym uruchomieniu)
- **Dane początkowe:** Ładowane z `src/main/resources/database/FillDatabase_sqlite.sql`

## 📁 Struktura projektu

```
Sapo_leyendo/
├── src/
│   ├── main/java/          # Kod źródłowy Java (Spring Boot)
│   ├── main/resources/     # Zasoby aplikacji (properties, SQL)
│   └── test/java/          # Testy jednostkowe
├── Visualization/          # Frontend React + Vite
│   ├── src/               # Komponenty React
│   └── build/             # Build produkcyjny
├── data/                  # Baza danych SQLite (generowana automatycznie)
└── target/                # Pliki skompilowane Maven
```

## ☁️ Deployment na Azure

### 📋 Przygotowane szablony ARM

Projekt zawiera gotowe szablony Azure Resource Manager do automatycznego wdrożenia:

| Plik | Opis |
|------|------|
| `template.json` | Definicja zasobów Azure (App Service, MySQL, VNet, itp.) |
| `parameters.json` | Parametry konfiguracyjne (subskrypcja, nazwy, regiony) |

### 🏗️ Architektura wdrożenia

- **App Service** z runtime `JAVA|21-java21`
- **MySQL Flexible Server** z Private Endpoint
- **Virtual Network** dla bezpiecznej komunikacji
- **Managed Identity** z federacją GitHub Actions
- **Region:** Poland Central
- **Plan:** B1

### 🚀 Jak wdrożyć

```bash
az deployment group create \
    --subscription <SUBSCRIPTION_ID> \

## 🛠️ Technologie

### Backend
- **Java 21** - Najnowsza wersja LTS
- **Spring Boot 3.x** - Framework aplikacyjny
- **Maven** - Zarządzanie zależnościami
- **SQLite** - Baza danych (rozwój)
- **MySQL** - Baza danych (produkcja)

### Frontend
- **React 18.x** - Biblioteka UI
- **Vite** - Build tool
- **TypeScript** - Typowanie statyczne

### DevOps
- **Azure App Service** - Hosting aplikacji
- **Azure MySQL** - Baza danych w chmurze
- **GitHub Actions** - CI/CD
- **Docker** - Konteneryzacja (opcjonalnie)
    --resource-group Studia \
    --template-file template.json \
    --parameters @parameters.json \
    --parameters mySqlServerAdminPwd="<silne_haslo>"
```

### ⚠️ Ważne uwagi

> **🔑 Hasło MySQL:** Uzupełnij `mySqlServerAdminPwd` (SecureString) przed uruchomieniem
> 
> **🔧 CI/CD:** Repozytorium i branch skonfigurowane w parametrach (`repoUrl`, `branch`)
> 
> **🌐 Sieć:** Domyślne nazwy VNet/subnet/PE generowane automatycznie
