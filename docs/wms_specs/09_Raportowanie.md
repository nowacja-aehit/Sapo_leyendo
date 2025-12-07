# Moduł 9: Raportowanie (Reporting & Analytics) - Specyfikacja Techniczna i Funkcjonalna

## 1. Wstęp i Cel Modułu
Moduł Raportowania (Reporting) to warstwa analityczna systemu WMS, która przekształca surowe dane operacyjne w użyteczną wiedzę biznesową (Business Intelligence). Jego celem jest dostarczenie kadrze zarządzającej pełnego obrazu sytuacji w magazynie w czasie rzeczywistym oraz narzędzi do analizy historycznej. Moduł ten wspiera podejmowanie decyzji w obszarach planowania zasobów, optymalizacji procesów i kontroli kosztów.

## 2. Założenia Projektowe
1.  **Czas Rzeczywisty (Real-time)**: Kluczowe wskaźniki (np. postęp kompletacji fal) muszą być odświeżane na bieżąco, aby Kierownik Zmiany mógł reagować na zatory.
2.  **Dostępność Wielokanałowa**: Raporty dostępne na komputerach PC, tabletach managerów oraz na dużych ekranach (TV) w hali magazynowej (Visual Management).
3.  **Uprawnienia**: Ścisła kontrola dostępu – magazynier widzi tylko swoje wyniki, Kierownik Zmiany widzi całą zmianę, Dyrektor widzi koszty finansowe.

## 3. Szczegółowa Funkcjonalność

### 3.1 Dashboard Operacyjny (Cockpit)
Główny ekran zarządczy prezentujący "puls magazynu".
*   **Postęp Prac**: Wykresy typu Gauge pokazujące % realizacji przyjęć, zbiórki i wysyłek zaplanowanych na dziś.
*   **Alerty Krytyczne**: Czerwone powiadomienia o problemach, np. "Zator w strefie pakowania", "Opóźniony odjazd kuriera", "Brak miejsca na regałach".
*   **Zasoby Ludzkie**: Liczba zalogowanych pracowników w podziale na strefy.

### 3.2 Raporty Wydajnościowe (Labor Management)
Analiza efektywności pracy ludzi i maszyn.
*   **Wydajność Indywidualna**: Ilość linii/sztuk na godzinę dla każdego pracownika. Porównanie do średniej i celu (Target).
*   **Czas Pośredni**: Analiza czasu spędzonego na czynnościach niedodających wartości (np. przerwy, szukanie wózka, awarie).
*   **Koszt na Operację (Cost to Serve)**: Wyliczenie, ile kosztuje fizyczne obsłużenie jednego zamówienia (roboczogodziny + materiały).

### 3.3 Raporty Stanów Magazynowych (Inventory Health)
*   **Wiekowanie Zapasu (Aging)**: Raport pokazujący towary zalegające (Slow Movers), zbliżające się do terminu ważności lub "martwe" (Dead Stock).
*   **Dokładność Inwentaryzacyjna (IRA)**: Wskaźnik zgodności stanu systemowego ze stanem fizycznym (Inventory Record Accuracy).
*   **Obłożenie Magazynu**: Wykres zajętości miejsc paletowych i półkowych (Capacity Utilization). Ostrzega przed brakiem miejsca przed szczytem sezonu.

### 3.4 Raporty Przepływu (Flow Analytics)
*   **Dock-to-Stock Time**: Średni czas od przyjazdu ciężarówki do odłożenia towaru na półkę.
*   **Order Cycle Time**: Średni czas od wpłynięcia zamówienia do wydruku etykiety wysyłkowej.
*   **Zwroty**: Analiza najczęściej zwracanych produktów i powodów zwrotów (Pareto).

## 4. Model Danych (Kluczowe Encje)

### `ReportDefinition` (Definicja Raportu)
*   `id`: Integer
*   `name`: String
*   `queryTemplate`: String (SQL/HQL)
*   `parameters`: List<ReportParam>
*   `visualizationType`: Enum (TABLE, BAR_CHART, PIE_CHART, HEATMAP)
*   `requiredPermission`: String

### `KpiMetric` (Wskaźnik KPI)
*   `id`: UUID
*   `name`: String (np. "LinesPerHour")
*   `value`: Double
*   `timestamp`: Timestamp
*   `dimension`: String (np. "User:JanKowalski")

### `AuditLog` (Log Audytowy)
*   `id`: UUID
*   `action`: String
*   `entity`: String
*   `oldValue`: String
*   `newValue`: String
*   `user`: String
*   `timestamp`: Timestamp
*   *Służy do raportów bezpieczeństwa i śledzenia zmian.*

## 5. Logika Biznesowa i Reguły
1.  **Agregacja Danych**: Ze względu na wolumen danych (miliony rekordów operacyjnych), raporty historyczne korzystają z tabel agregujących (Data Warehouse approach), odświeżanych nocą, aby nie obciążać bazy produkcyjnej.
2.  **Subskrypcje**: System umożliwia zdefiniowanie automatycznej wysyłki raportów (np. "Raport Stanów Zerowych") na e-mail Managera codziennie o 8:00 rano.
3.  **Anonimizacja**: W raportach udostępnianych na hali (tablice wyników), dane pracowników mogą być anonimizowane (np. użycie pseudonimów), aby uniknąć konfliktów, zgodnie z RODO/polityką firmy.

## 6. Obsługa Wyjątków
*   **Brak Danych**: Jeśli raport nie może zostać wygenerowany (np. brak danych za wybrany okres), system wyświetla czytelny komunikat zamiast pustego wykresu.
*   **Timeout**: Dla bardzo złożonych zapytań system posiada mechanizm timeoutu i sugeruje użytkownikowi zawężenie zakresu dat lub wygenerowanie raportu w tle (Background Job).

## 7. Współpraca z Innymi Modułami
*   **<- WSZYSTKIE MODUŁY**: Moduł Raportowania jest "pijawką" danych. Pobiera informacje ze wszystkich 8 pozostałych modułów.
    *   Z Modułu 1: Czas rozładunku.
    *   Z Modułu 4: Ilość braków (Short Picks).
    *   Z Modułu 6: Terminowość dostaw.
    *   Z Modułu 8: Poziom jakości dostawców.
*   **-> Eksport Zewnętrzny**: Moduł umożliwia wystawienie danych dla zewnętrznych systemów BI (np. PowerBI, Tableau) poprzez dedykowane API analityczne.

---
*Koniec specyfikacji modułu 9. Szacowana długość: ~5000 znaków.*
