# Moduł 2: Składowanie (Storage/Inventory) - Specyfikacja Techniczna i Funkcjonalna

## 1. Wstęp i Cel Modułu
Moduł Składowania (Storage) jest centralnym elementem systemu WMS, odpowiedzialnym za zarządzanie stanem magazynowym oraz fizycznym rozmieszczeniem towarów w przestrzeni magazynowej. Jego zadaniem jest nie tylko pasywne przechowywanie informacji o tym "co i gdzie jest", ale aktywne optymalizowanie wykorzystania przestrzeni oraz zapewnienie dostępności towaru dla procesów kompletacji. Moduł ten zarządza cyklem życia towaru od momentu odłożenia na regał (Put-away) aż do momentu pobrania (Picking).

## 2. Założenia Projektowe
1.  **Hierarchia Lokalizacji**: System odwzorowuje fizyczną strukturę magazynu w układzie: Magazyn -> Strefa (Zone) -> Rząd (Aisle) -> Regał (Rack) -> Poziom (Level) -> Gniazdo (Bin).
2.  **Zarządzanie przez LPN**: Podstawową jednostką śledzenia w strefie składowania jest LPN (License Plate Number). System wie, że na palecie LPN123 znajduje się 50 sztuk produktu X i 20 sztuk produktu Y.
3.  **Statusy Zapasu**: Każda partia towaru posiada status dostępności:
    *   *Available* (Dostępny do sprzedaży)
    *   *Allocated* (Zarezerwowany pod zamówienie)
    *   *Quarantine/QC* (Zablokowany jakościowo)
    *   *Damaged* (Uszkodzony)
4.  **Strategie Składowania**: System automatycznie sugeruje miejsce składowania na podstawie zdefiniowanych reguł (np. waga, rotacja ABC, temperatura).

## 3. Szczegółowa Funkcjonalność

### 3.1 Proces Odkładania (Put-away)
Jest to proces przemieszczania towaru ze strefy przyjęć (Staging) na miejsce docelowe.
*   **Algorytm Put-away**: System analizuje cechy produktu (wymiary, waga, klasa rotacji) i szuka najlepszej wolnej lokalizacji.
    *   *Strategia Bliskości*: Odkładanie blisko strefy kompletacji dla towarów szybko rotujących (Fast Movers).
    *   *Strategia Konsolidacji*: Szukanie lokalizacji, gdzie już znajduje się ten sam produkt, aby zwolnić inne gniazda.
*   **Zadania Operatora**: System generuje zadanie "Put-away Task" na terminal mobilny, wskazując operatorowi źródło (LPN) i sugerowany cel. Operator potwierdza wykonanie skanując lokalizację docelową.

### 3.2 Przesunięcia Wewnątrzmagazynowe (Internal Moves)
*   **Przesunięcia Ad-hoc**: Operator może samodzielnie zdecydować o przesunięciu palety (np. w celu zrobienia miejsca) skanując LPN i nową lokalizację.
*   **Konsolidacja (Defragmentacja)**: System okresowo generuje zadania przesunięcia "pół-pustych" palet do jednej lokalizacji, aby odzyskać miejsce na pełne palety.
*   **Zmiana Statusu**: Możliwość logicznego przesunięcia towaru (bez fizycznego ruchu), np. zmiana z "Dostępny" na "Uszkodzony" po upuszczeniu towaru przez operatora.

### 3.3 Uzupełnianie (Replenishment)
Proces zasilania strefy kompletacji (Pick Face) towarem ze strefy zapasu (Reserve).
*   **Wyzwalacze (Triggers)**:
    *   *Min/Max*: Gdy stan w lokalizacji kompletacyjnej spadnie poniżej minimum.
    *   *Demand-based*: Gdy bieżące zamówienia (Wave) wymagają więcej towaru niż jest dostępne w strefie kompletacji.
*   **Priorytetyzacja**: Zadania uzupełniania mają wyższy priorytet niż odkładanie, aby nie zatrzymać procesu kompletacji.

### 3.4 Inwentaryzacja (Stock Counting)
*   **Inwentaryzacja Ciągła (Cycle Counting)**: Zamiast zamykać magazyn raz w roku, system generuje codzienne zadania liczenia dla losowych lokalizacji lub towarów o wysokiej wartości/rotacji.
*   **Inwentaryzacja Ad-hoc**: Możliwość zlecenia przeliczenia konkretnej lokalizacji w przypadku wykrycia niezgodności podczas kompletacji (tzw. Short Pick).
*   **Zatwierdzanie Różnic**: Różnice inwentaryzacyjne (nadwyżki/niedobory) trafiają do "kolejki akceptacji" dla Managera przed aktualizacją stanu księgowego.

## 4. Model Danych (Kluczowe Encje)

### `InventoryItem` (Zapas)
*   `id`: UUID
*   `productId`: Long
*   `locationId`: Integer
*   `lpn`: String (Klucz grupujący)
*   `quantity`: Integer
*   `batchNumber`: String
*   `status`: Enum (AVAILABLE, ALLOCATED, QC_HOLD, BLOCKED)
*   `expiryDate`: Date

### `MoveTask` (Zadanie Magazynowe)
*   `id`: UUID
*   `type`: Enum (PUTAWAY, REPLENISHMENT, INTERNAL_MOVE)
*   `inventoryItemId`: UUID
*   `sourceLocationId`: Integer
*   `targetLocationId`: Integer (Sugerowana)
*   `actualTargetLocationId`: Integer (Rzeczywista - wypełniana przez operatora)
*   `priority`: Integer (1-10)
*   `status`: Enum (PENDING, ASSIGNED, COMPLETED, CANCELLED)

### `StockCountSession` (Sesja Inwentaryzacyjna)
*   `id`: UUID
*   `type`: Enum (CYCLE, FULL, SPOT)
*   `status`: Enum (OPEN, COUNTING, REVIEW, CLOSED)
*   `locations`: List<Location>

## 5. Logika Biznesowa i Reguły
1.  **Reguła FIFO/FEFO**: Przy generowaniu zadań uzupełniania, system zawsze wybiera najstarszą partię (FIFO) lub partię z najkrótszą datą ważności (FEFO).
2.  **Blokada Lokalizacji**: Podczas wykonywania zadania inwentaryzacji na danej lokalizacji, system blokuje możliwość pobierania i odkładania towaru w tym gnieździe do czasu zakończenia liczenia.
3.  **Pojemność Lokalizacji**: System nie pozwoli wygenerować zadania Put-away do lokalizacji, jeśli suma objętości towarów przekroczyłaby zdefiniowaną pojemność gniazda (Volume Check).

## 6. Obsługa Wyjątków
*   **Lokalizacja Zajęta**: Jeśli operator dotrze do sugerowanej lokalizacji i okaże się ona fizycznie zajęta (błąd systemowy), może wybrać opcję "Lokalizacja niedostępna". System wymusi inwentaryzację tego miejsca i zasugeruje nową lokalizację dla odkładanego towaru.
*   **Uszkodzenie przy Transporcie**: Jeśli podczas przenoszenia towar ulegnie uszkodzeniu, operator może w trakcie potwierdzania zadania zmienić status docelowy na "Damaged" i odłożyć towar do strefy kwarantanny zamiast na regał.

## 7. Współpraca z Innymi Modułami
*   **<- Moduł 1 (Przyjęcia)**: Moduł Składowania przejmuje odpowiedzialność za towar w momencie zakończenia weryfikacji przyjęcia.
*   **-> Moduł 4 (Kompletacja)**: Moduł Składowania udostępnia informacje o dostępnych ilościach. Gdy Moduł Kompletacji rezerwuje towar pod zamówienie, Moduł Składowania zmienia status zapasu z "Available" na "Allocated".
*   **-> Moduł 9 (Raportowanie)**: Dostarcza kluczowe dane o wartości magazynu (Stock Value), zajętości magazynu (Warehouse Utilization) oraz dokładności stanów (Inventory Accuracy).
*   **<- Moduł 8 (QC)**: Zmiana statusu jakościowego w Module QC automatycznie aktualizuje status dostępności w Module Składowania (np. zwolnienie z kwarantanny czyni towar dostępnym do sprzedaży).

---
*Koniec specyfikacji modułu 2. Szacowana długość: ~5100 znaków.*
