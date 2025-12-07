# Moduł 3: Lokacja (Location) - Specyfikacja Techniczna i Funkcjonalna

## 1. Wstęp i Cel Modułu
Moduł Lokacji (Location) stanowi fundament topograficzny całego systemu WMS. Jego zadaniem jest cyfrowe odwzorowanie fizycznej przestrzeni magazynowej w sposób umożliwiający precyzyjne adresowanie każdego miejsca składowania. Moduł ten nie tylko przechowuje statyczne dane o regałach i gniazdach, ale dostarcza kluczowych parametrów (wymiary, nośność, współrzędne, ścieżki) niezbędnych dla algorytmów optymalizacyjnych wykorzystywanych w procesach składowania (Put-away) i kompletacji (Picking). Bez precyzyjnie zdefiniowanych lokacji, automatyzacja procesów magazynowych jest niemożliwa.

## 2. Założenia Projektowe
1.  **Unikalność Adresowania**: Każda fizyczna lokalizacja w magazynie (gniazdo regałowe, pole odkładcze, dok, stół do pakowania) posiada unikalny w skali systemu identyfikator oraz kod kreskowy/QR.
2.  **Wielowymiarowość**: Lokacje są opisane nie tylko adresem logicznym (np. A-01-02-03), ale również parametrami fizycznymi (szerokość, wysokość, głębokość, maksymalna waga).
3.  **Strefowanie (Zoning)**: Magazyn podzielony jest na logiczne strefy (Zones), które determinują reguły składowania (np. strefa chłodnicza, strefa materiałów niebezpiecznych, strefa wysokiego składowania).
4.  **Sekwencjonowanie Ścieżek**: Każda lokalizacja posiada atrybut "Pick Sequence" określający jej kolejność na optymalnej ścieżce zbiórki, co pozwala na minimalizację drogi pokonywanej przez magazyniera.

## 3. Szczegółowa Funkcjonalność

### 3.1 Zarządzanie Strukturą Magazynu (Location Master Data)
*   **Kreator Magazynu**: Narzędzie umożliwiające masowe generowanie lokalizacji na podstawie parametrów (np. "Utwórz rząd A, 10 kolumn, 5 poziomów").
*   **Edycja Parametrów**: Możliwość zmiany typu lokalizacji (np. zmiana z "Paletowa" na "Półkowa"), wymiarów lub statusu aktywności.
*   **Drukowanie Etykiet**: Funkcjonalność generowania etykiet lokalizacyjnych z kodami kreskowymi i czytelnymi dla człowieka adresami, gotowych do naklejenia na belki regałowe.

### 3.2 Zarządzanie Strefami (Zone Management)
*   **Definicja Stref**: Tworzenie stref logicznych (np. "Strefa Antresoli", "Strefa Paletowa", "Strefa Zwrotów").
*   **Reguły Strefowe**: Przypisywanie reguł do stref, np.:
    *   *Mix Constraints*: Czy w tej strefie można mieszać różne SKU w jednym gnieździe?
    *   *Product Constraints*: Czy strefa jest dedykowana tylko dla określonych grup produktowych (np. tylko Chemia)?
*   **Strefy Robocze**: Definiowanie stref pracy dla konkretnych typów wózków (np. wózki systemowe VNA mogą pracować tylko w korytarzach wąskich).

### 3.3 Optymalizacja Ścieżek (Path Optimization)
*   **Pick Path Sequence**: Algorytm wyznaczający optymalną kolejność odwiedzania lokalizacji (tzw. "Snake path" lub "S-shape"). Każda lokalizacja otrzymuje numer porządkowy.
*   **Współrzędne XYZ**: Opcjonalne mapowanie współrzędnych przestrzennych dla zaawansowanych algorytmów obliczania odległości euklidesowej (przydatne przy integracji z robotami AGV/AMR).

### 3.4 Zarządzanie Pojemnością (Capacity Management)
*   **Limity Fizyczne**: Definiowanie maksymalnej wagi i objętości dla każdej lokalizacji.
*   **Typy Nośników**: Określanie jakie typy nośników (np. Paleta EURO, Paleta Przemysłowa, Karton) są akceptowalne w danej lokalizacji.
*   **Blokady Techniczne**: Możliwość wyłączenia lokalizacji z użytku (np. "Uszkodzona belka regałowa"), co natychmiast blokuje możliwość generowania zadań do tego miejsca.

## 4. Model Danych (Kluczowe Encje)

### `Location` (Lokalizacja)
*   `id`: Integer
*   `name`: String (Kod czytelny, np. "A-01-01")
*   `barcode`: String
*   `zoneId`: Integer
*   `aisle`: String (Rząd)
*   `rack`: String (Regał/Kolumna)
*   `level`: String (Poziom)
*   `bin`: String (Gniazdo)
*   `typeId`: Integer (Relacja do LocationType)
*   `pickSequence`: Integer (Kolejność zbiórki)
*   `status`: Enum (ACTIVE, INACTIVE, BLOCKED_DAMAGE)
*   `currentWeight`: Double (Obliczana dynamicznie)
*   `currentVolume`: Double (Obliczana dynamicznie)

### `LocationType` (Typ Lokalizacji)
*   `id`: Integer
*   `name`: String (np. "Paletowa Standard", "Półka Drobnicowa")
*   `maxWeight`: Double
*   `maxVolume`: Double
*   `length`: Double
*   `width`: Double
*   `height`: Double
*   `allowedUnitTypes`: List<UnitType> (np. [EURO, CHEP])

### `Zone` (Strefa)
*   `id`: Integer
*   `name`: String
*   `isTemperatureControlled`: Boolean
*   `isSecure`: Boolean
*   `allowMixedSku`: Boolean

## 5. Logika Biznesowa i Reguły
1.  **Walidacja Wymiarów**: System nie pozwoli zmienić typu lokalizacji na mniejszy, jeśli aktualnie znajduje się w niej towar, który przekracza wymiary nowego typu.
2.  **Dziedziczenie Reguł**: Lokalizacja dziedziczy reguły ze Strefy, w której się znajduje (np. jeśli Strefa jest "Mroźnia", to każda lokalizacja w niej ma atrybut "Temp < -18C").
3.  **Unikalność w Strefie**: W ramach jednej strefy nie mogą istnieć dwie lokalizacje o tym samym kodzie wizualnym, aby uniknąć pomyłek operatora.

## 6. Obsługa Wyjątków
*   **Błąd Skanowania**: Jeśli kod kreskowy lokalizacji jest nieczytelny, system umożliwia wprowadzenie cyfry kontrolnej (Check Digit) - krótkiego kodu weryfikacyjnego, który jest inny niż adres lokalizacji, aby potwierdzić obecność operatora we właściwym miejscu.
*   **Przepełnienie Wirtualne**: Sytuacja, w której system uważa, że lokalizacja jest pełna, a fizycznie jest pusta (błąd danych). Moduł udostępnia funkcję "Weryfikuj Pustą Lokalizację", która zeruje stan po potwierdzeniu przez Managera.

## 7. Współpraca z Innymi Modułami
*   **-> Moduł 2 (Składowanie)**: Moduł Lokacji jest "dawcą reguł" dla algorytmów Put-away. To tutaj zdefiniowane jest, że "Rząd A to strefa dla towarów ciężkich".
*   **-> Moduł 4 (Kompletacja)**: Atrybut `pickSequence` z Modułu Lokacji jest kluczowy dla sortowania listy zbiórki (Pick List), co bezpośrednio wpływa na wydajność procesu kompletacji.
*   **-> Moduł 1 (Przyjęcia)**: Definiuje lokalizacje typu "Dock" i "Staging", które są punktami startowymi dla towarów wchodzących.
*   **-> Moduł 9 (Raportowanie)**: Umożliwia generowanie map cieplnych (Heat Maps) magazynu, pokazując które aleje są najbardziej obciążone ruchem.

---
*Koniec specyfikacji modułu 3. Szacowana długość: ~5000 znaków.*
