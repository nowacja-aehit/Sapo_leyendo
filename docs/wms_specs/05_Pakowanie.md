# Moduł 5: Pakowanie (Packing) - Specyfikacja Techniczna i Funkcjonalna

## 1. Wstęp i Cel Modułu
Moduł Pakowania (Packing) jest ostatnim etapem wewnątrzmagazynowym przed wydaniem towaru przewoźnikowi. Jego celem jest przekształcenie luźnych produktów zebranych w procesie kompletacji w bezpieczne, gotowe do transportu przesyłki (paczki lub palety). Jest to również kluczowy punkt kontrolny (Quality Gate) – ostatnia szansa na weryfikację poprawności zamówienia przed wysyłką do klienta. Moduł ten odpowiada również za estetykę przesyłki, dobór odpowiednich materiałów pakowych oraz generowanie dokumentacji przewozowej.

## 2. Założenia Projektowe
1.  **Weryfikacja 100%**: Każda sztuka towaru wkładana do paczki musi zostać zeskanowana. Eliminuje to błędy powstałe podczas kompletacji (np. pobranie złego koloru).
2.  **Stanowiska Pakowania (Pack Stations)**: System obsługuje dedykowane stanowiska wyposażone w komputer, skaner, wagę, drukarkę etykiet i drukarkę dokumentów.
3.  **Integracja z Wagą**: System automatycznie odczytuje wagę paczki z wagi podłączonej do stanowiska, co jest niezbędne do wyliczenia kosztów transportu.
4.  **Konsolidacja**: System musi umieć połączyć towary z wielu pojemników kompletacyjnych (np. z różnych stref) w jedną przesyłkę dla klienta.

## 3. Szczegółowa Funkcjonalność

### 3.1 Proces Pakowania (Scan-to-Pack)
*   **Identyfikacja Źródła**: Operator skanuje LPN pojemnika kompletacyjnego, który przyjechał ze strefy zbiórki. System wyświetla listę towarów do spakowania z tego pojemnika.
*   **Skanowanie Produktów**: Operator skanuje kod EAN każdego produktu przed włożeniem go do kartonu wysyłkowego. System potwierdza poprawność sygnałem dźwiękowym/wizualnym.
*   **Obsługa Numerów Seryjnych**: Jeśli numery seryjne nie były zbierane przy kompletacji, muszą zostać zarejestrowane na etapie pakowania.

### 3.2 Kartonizacja (Cartonization Logic)
Algorytm sugerujący optymalne opakowanie.
*   **Sugestia Kartonu**: Na podstawie wymiarów produktów, system podpowiada operatorowi: "Użyj kartonu typu C (30x20x15)".
*   **Split Shipment**: Jeśli zamówienie nie mieści się w jednym kartonie, system zarządza podziałem na wiele paczek (Multiparcel), generując odpowiednią liczbę etykiet (np. 1/2, 2/2).

### 3.3 Zarządzanie Materiałami Eksploatacyjnymi
*   **Rejestracja Zużycia**: System może śledzić zużycie kartonów, taśmy, wypełniaczy.
*   **Dodatki Marketingowe**: System może nakazać operatorowi dołożenie ulotki, próbki gratisowej lub katalogu, traktując to jako linię zamówienia o wartości 0 PLN.

### 3.4 Finalizacja i Etykietowanie
*   **Ważenie**: Po zamknięciu kartonu operator stawia go na wadze. System zapisuje wagę rzeczywistą.
*   **Generowanie Dokumentów**: Automatyczny wydruk:
    *   Listu Przewozowego (Shipping Label) dla kuriera.
    *   Listu Pakowego (Packing Slip) dla klienta (z ukrytymi cenami lub bez).
    *   Faktury (opcjonalnie).
*   **Parowanie**: System pilnuje, aby etykieta kurierska została naklejona na właściwą paczkę (np. poprzez wymuszenie zeskanowania wydrukowanej etykiety).

## 4. Model Danych (Kluczowe Encje)

### `PackingStation` (Stanowisko Pakowania)
*   `id`: Integer
*   `name`: String
*   `printerIp`: String
*   `scaleIp`: String
*   `status`: Enum (ACTIVE, CLOSED, MAINTENANCE)

### `Shipment` (Przesyłka)
*   `id`: UUID
*   `orderId`: UUID
*   `carrierId`: Integer
*   `trackingNumber`: String
*   `status`: Enum (PACKING, PACKED, SHIPPED)
*   `totalWeight`: Double
*   `parcels`: List<Parcel>

### `Parcel` (Paczka)
*   `id`: UUID
*   `shipmentId`: UUID
*   `boxType`: String (np. "BOX-M")
*   `weight`: Double
*   `trackingSubNumber`: String (dla multipaczki)
*   `items`: List<ParcelItem>

### `PackingMaterial` (Opakowanie)
*   `id`: Integer
*   `code`: String
*   `dimensions`: Dimensions
*   `tareWeight`: Double
*   `maxWeight`: Double

## 5. Logika Biznesowa i Reguły
1.  **Reguła Kompletności**: System nie pozwoli zamknąć paczki i wydrukować etykiety, dopóki wszystkie towary z zamówienia (lub danej części zamówienia) nie zostaną zeskanowane.
2.  **Walidacja Wagi**: System porównuje wagę teoretyczną (suma wag produktów + waga kartonu) z wagą rzeczywistą z wagi. Jeśli różnica przekracza tolerancję (np. 5%), system blokuje wysyłkę i nakazuje rewizję (podejrzenie pomyłki w towarze lub błędnych danych w kartotece).
3.  **Reguła Hazmat**: Jeśli w paczce znajduje się towar niebezpieczny (ADR), system wymusza na operatorze naklejenie dodatkowych oznaczeń ostrzegawczych.

## 6. Obsługa Wyjątków
*   **Brak Towaru w Pojemniku**: Operator skanuje pojemnik z kompletacji, ale brakuje w nim towaru, który system uważa za zebrany. Operator zgłasza "Missing at Pack". System uruchamia procedurę wyjaśniającą (np. sprawdzenie monitoringu, inwentaryzacja strefy pakowania).
*   **Uszkodzenie przy Pakowaniu**: Operator upuścił produkt. Zgłasza uszkodzenie, system zdejmuje produkt z zamówienia i (zależnie od konfiguracji) albo generuje zadanie domówienia brakującej sztuki (Hot Replenishment), albo zamyka paczkę z brakiem (Short Ship).

## 7. Współpraca z Innymi Modułami
*   **<- Moduł 4 (Kompletacja)**: Moduł Pakowania jest bezpośrednim odbiorcą pracy Modułu Kompletacji.
*   **-> Moduł 6 (Wysyłka)**: Spakowane paczki (Parcels) są przekazywane logicznie do Modułu Wysyłki. Moduł Pakowania dostarcza dokładnych danych o wadze i wymiarach, niezbędnych do awizacji kuriera.
*   **-> Moduł 2 (Składowanie)**: W przypadku anulowania zamówienia w trakcie pakowania, towary muszą zostać zwrócone na magazyn (proces "Put-back").
*   **-> Moduł 9 (Raportowanie)**: Mierzy wydajność pakowaczy (Paczki/Godzinę) oraz zużycie materiałów pakowych.

---
*Koniec specyfikacji modułu 5. Szacowana długość: ~5100 znaków.*
