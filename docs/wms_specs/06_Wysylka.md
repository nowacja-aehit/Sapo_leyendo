# Moduł 6: Wysyłka (Outbound) - Specyfikacja Techniczna i Funkcjonalna

## 1. Wstęp i Cel Modułu
Moduł Wysyłki (Outbound) zarządza ostatnim etapem procesu magazynowego – fizycznym wydaniem towaru przewoźnikowi lub załadunkiem na własną flotę transportową. Jego celem jest zapewnienie, że właściwe paczki/palety trafią na właściwą ciężarówkę, w odpowiednim czasie i z kompletną dokumentacją. Moduł ten integruje się z systemami firm kurierskich (CEP) oraz giełdami transportowymi, a także zarządza przestrzenią ramp załadunkowych.

## 2. Założenia Projektowe
1.  **Skanowanie przy Załadunku**: Każda jednostka logistyczna (paczka lub paleta) musi zostać zeskanowana w momencie wjazdu na naczepę. Zapobiega to wysłaniu towaru błędnym transportem (np. paczka DHL na auto DPD).
2.  **Integracja EDI/API**: Moduł musi komunikować się elektronicznie z przewoźnikami w celu zamawiania podjazdów, przesyłania danych o przesyłkach (Manifest) i pobierania numerów śledzenia.
3.  **Grupowanie (Staging)**: Towary oczekujące na wysyłkę są grupowane w strefie wyjścia (Outbound Staging) według przewoźnika, trasy lub godziny odjazdu.

## 3. Szczegółowa Funkcjonalność

### 3.1 Planowanie Transportu (Carrier Selection / Routing)
*   **Rate Shopping**: Dla przesyłek, gdzie przewoźnik nie jest narzucony przez klienta, system automatycznie wybiera najtańszą lub najszybszą opcję transportu na podstawie wagi, wymiarów i kodu pocztowego odbiorcy.
*   **Planowanie Tras (Dla floty własnej)**: Algorytm układający kolejność stopów (przystanków) dla kierowcy i optymalizujący załadunek (LIFO – Last In, First Out), aby towar dla ostatniego klienta został załadowany jako pierwszy.

### 3.2 Zarządzanie Strefą Wyjścia (Staging)
*   **Sortowanie**: Po spakowaniu, paczki trafiają na sorter lub są ręcznie sortowane na palety zbiorcze dedykowane dla danego kuriera/kierunku.
*   **Konsolidacja Paletowa**: Budowanie palet wysyłkowych z wielu paczek (np. 50 paczek do jednego oddziału Poczty Polskiej). System generuje etykietę zbiorczą na paletę.

### 3.3 Proces Załadunku (Loading)
*   **Weryfikacja**: Operator skanuje ID doku/ciężarówki, a następnie skanuje każdą ładowaną jednostkę. System blokuje załadunek jednostki nieprzypisanej do tego transportu.
*   **Dokumentacja Kierowcy**: Po zakończeniu załadunku system generuje i drukuje:
    *   List przewozowy zbiorczy (Manifest/Bordereau).
    *   Dokument WZ (Wydanie Zewnętrzne).
    *   Instrukcje dla kierowcy (np. kody do bram).

### 3.4 Zamknięcie Dnia (End of Day)
*   **Transmisja Danych**: Wysłanie komunikatu elektronicznego do przewoźnika z listą wszystkich nadanych przesyłek. Jest to moment, w którym przewoźnik formalnie przejmuje odpowiedzialność za towar.
*   **Powiadomienia**: Wysłanie e-maila/SMS do klienta z numerem śledzenia i linkiem do trackingu.

## 4. Model Danych (Kluczowe Encje)

### `Load` (Ładunek/Transport)
*   `id`: UUID
*   `carrierId`: Integer
*   `vehiclePlateNumber`: String
*   `driverName`: String
*   `driverPhone`: String
*   `status`: Enum (PLANNED, LOADING, DISPATCHED, DELIVERED)
*   `dockId`: Integer
*   `departureTime`: Timestamp

### `Manifest` (Lista Przewozowa)
*   `id`: UUID
*   `loadId`: UUID
*   `carrierManifestId`: String (ID nadane przez przewoźnika)
*   `totalParcels`: Integer
*   `totalWeight`: Double
*   `generationDate`: Timestamp

### `Carrier` (Przewoźnik)
*   `id`: Integer
*   `name`: String (np. "DHL", "InPost")
*   `serviceType`: String (np. "Express 12:00", "Economy")
*   `integrationType`: Enum (API, FTP, EMAIL)
*   `cutoffTime`: Time (Godzina graniczna odbioru)

## 5. Logika Biznesowa i Reguły
1.  **Reguła Cut-off**: System musi wiedzieć, o której godzinie przyjeżdża kurier. Zamówienia spakowane po godzinie granicznej (Cut-off) są automatycznie przesuwane na następny dzień roboczy w planie wysyłek.
2.  **Walidacja Kompletności Transportu**: System nie pozwoli zamknąć transportu (Dispatch), jeśli na liście załadunkowej znajdują się paczki, które fizycznie nie zostały zeskanowane na naczepę (ochrona przed zagubieniem paczki na magazynie).
3.  **Reguła Objętości**: System sumuje objętość planowanych przesyłek i ostrzega, jeśli przekracza ona pojemność podstawionego pojazdu (np. 33 palety dla TIRa).

## 6. Obsługa Wyjątków
*   **Paczka Wycofana**: Klient anulował zamówienie, gdy paczka była już na rampie. System blokuje załadunek (sygnał "STOP" przy skanowaniu) i nakazuje zwrot paczki do strefy zwrotów/rozpakowania.
*   **Awaria Przewoźnika**: Kurier X nie przyjechał. Manager może masowo przepiąć wszystkie przesyłki z tego transportu na innego przewoźnika (wymaga przenerowania etykiet) lub przesunąć datę wysyłki.

## 7. Współpraca z Innymi Modułami
*   **<- Moduł 5 (Pakowanie)**: Moduł Wysyłki oczekuje na gotowe paczki z Modułu Pakowania.
*   **<- Moduł 4 (Kompletacja)**: Przyjmuje palety pełne (Full Pallet Picks) bezpośrednio ze strefy kompletacji.
*   **-> Moduł 9 (Raportowanie)**: Dostarcza danych o terminowości wysyłek (On-Time Ship), kosztach transportu i wydajności załadunku.
*   **-> System ERP**: Po wysłaniu transportu, Moduł Wysyłki wysyła sygnał do ERP w celu wystawienia faktury sprzedaży i zdjęcia stanu magazynowego (jeśli WMS nie jest masterem stanu).

---
*Koniec specyfikacji modułu 6. Szacowana długość: ~5000 znaków.*
