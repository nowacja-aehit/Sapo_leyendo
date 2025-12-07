# Moduł 1: Przyjęcia (Inbound) - Specyfikacja Techniczna i Funkcjonalna

## 1. Wstęp i Cel Modułu
Moduł Przyjęć (Inbound) stanowi bramę wejściową dla wszystkich towarów trafiających do magazynu. Jest to krytyczny element systemu WMS, determinujący dokładność stanów magazynowych oraz efektywność dalszych procesów. Jego głównym celem jest szybkie, bezbłędne i kontrolowane przyjęcie fizyczne towarów, zweryfikowanie ich zgodności z zamówieniem zakupu lub awizacją (ASN) oraz przygotowanie ich do procesu składowania.

W kontekście systemu Sapo Leyendo, moduł ten musi obsługiwać zarówno przyjęcia planowane (na podstawie ASN), jak i nieplanowane (tzw. blind receiving), a także zarządzać harmonogramem doków rozładunkowych.

## 2. Założenia Projektowe
Dla zapewnienia wysokiej wydajności i skalowalności, moduł Przyjęć opiera się na następujących założeniach:
1.  **Awizacja jako standard**: Podstawą procesu przyjęcia jest dokument ASN (Advanced Shipping Notice). Przyjęcia bez ASN są traktowane jako wyjątki wymagające dodatkowej autoryzacji.
2.  **Identyfikowalność (Traceability)**: Każdy przyjęty towar musi być natychmiast identyfikowalny w systemie. Jeśli towar nie posiada kodu kreskowego, system musi wymusić wydruk etykiety systemowej.
3.  **Jednostki Logistyczne (LPN)**: System operuje na poziomie License Plate Number (LPN). Każda paleta lub karton zbiorczy otrzymuje unikalny identyfikator LPN już na etapie rozładunku.
4.  **Separacja stref**: Fizyczne przyjęcie odbywa się w strefie buforowej (Staging Area). Towar nie jest dostępny do sprzedaży/kompletacji dopóki nie zostanie sfinalizowany proces przyjęcia i (opcjonalnie) kontroli jakości.
5.  **Integracja z QC**: Moduł musi automatycznie blokować towary wymagające kontroli jakości (zdefiniowane w kartotece produktu lub losowo) przed odłożeniem na regał.

## 3. Szczegółowa Funkcjonalność

### 3.1 Zarządzanie Awizacjami (ASN - Advanced Shipping Notice)
System umożliwia importowanie, tworzenie i edycję dokumentów ASN.
*   **Import danych**: Możliwość wczytania ASN z plików XML/CSV lub poprzez API z systemu ERP.
*   **Walidacja**: Sprawdzanie poprawności kodów SKU, ilości oraz daty ważności (jeśli dotyczy) na etapie awizacji.
*   **Statusy ASN**: Oczekujący, W Transporcie, Na Dok, W Trakcie Przyjęcia, Przyjęty, Zamknięty, Anulowany.

### 3.2 Zarządzanie Oknami Czasowymi (Dock Scheduling)
Podmoduł odpowiedzialny za planowanie wykorzystania bram rozładunkowych.
*   **Kalendarz Doków**: Graficzna reprezentacja zajętości bram w podziale na godziny.
*   **Rezerwacja**: Przypisanie konkretnego ASN do bramy i godziny. System pilnuje, aby nie przekroczyć przepustowości magazynu (np. max 100 palet na godzinę).
*   **Check-in Kierowcy**: Rejestracja przyjazdu kierowcy, weryfikacja dokumentów przewozowych i przydzielenie ostatecznej bramy.

### 3.3 Proces Rozładunku i Weryfikacji (Receiving)
To serce modułu, obsługiwane zazwyczaj za pomocą terminali mobilnych.
*   **Skanowanie**: Operator skanuje kod SSCC palety dostawcy lub kod produktu.
*   **Weryfikacja Ilościowa**: System porównuje ilość zliczoną z ilością zadeklarowaną w ASN.
    *   *Nadmiar (Overage)*: System blokuje przyjęcie nadmiarowe lub wymaga autoryzacji przełożonego.
    *   *Niedobór (Shortage)*: System rejestruje brak i aktualizuje status ASN.
*   **Weryfikacja Jakościowa (Wstępna)**: Operator ma możliwość oznaczenia widocznych uszkodzeń opakowania zbiorczego (np. "Zgnieciony karton", "Zalana paleta").

### 3.4 Generowanie Jednostek Logistycznych (LPN)
Dla towarów przyjmowanych luzem lub na paletach nieoznaczonych standardem GS1:
*   **Tworzenie LPN**: System generuje unikalny numer LPN (np. kod QR/Barcode) dla nośnika.
*   **Asocjacja**: Przypisanie konkretnych ilości SKU i partii (Batch/Lot) do wygenerowanego LPN.
*   **Druk Etykiet**: Automatyczny wydruk etykiety przyjęciowej z informacjami: LPN, SKU, Ilość, Data Przyjęcia, Operator.

## 4. Model Danych (Kluczowe Encje)

### `InboundOrder` (Nagłówek Przyjęcia)
*   `id`: UUID
*   `externalReference`: String (Numer zamówienia z ERP)
*   `supplierId`: Long
*   `expectedArrivalDate`: Timestamp
*   `status`: Enum (PLANNED, ARRIVED, IN_PROGRESS, COMPLETED, CLOSED)
*   `dockId`: Integer (Przypisana brama)

### `InboundLine` (Pozycja Przyjęcia)
*   `id`: UUID
*   `inboundOrderId`: UUID
*   `productId`: Long
*   `expectedQuantity`: Integer
*   `receivedQuantity`: Integer
*   `batchNumber`: String (opcjonalnie)

### `Receipt` (Potwierdzenie Przyjęcia - Log operacyjny)
*   `id`: UUID
*   `inboundLineId`: UUID
*   `lpn`: String (Wygenerowany lub zeskanowany identyfikator nośnika)
*   `quantity`: Integer
*   `operatorId`: Long
*   `timestamp`: Timestamp
*   `damageCode`: String (opcjonalnie)

## 5. Logika Biznesowa i Reguły
1.  **Reguła Tolerancji**: System może dopuścić przyjęcie +/- 5% ilości względem ASN bez blokady, ale z ostrzeżeniem. Powyżej tej wartości wymagana jest akceptacja Managera.
2.  **Reguła Mieszania Partii**: Na jednym nośniku LPN nie mogą znajdować się różne partie (Batch) tego samego produktu, chyba że konfiguracja produktu na to pozwala.
3.  **Reguła Kwarantanny**: Jeśli produkt jest oznaczony flagą "Wymaga QC", system automatycznie zmienia status przyjętego LPN na "Q-Hold" (Zablokowany dla QC) i nie pozwala na jego odłożenie w strefie kompletacji.

## 6. Obsługa Wyjątków
*   **Brak ASN (Blind Receipt)**: Operator ręcznie wybiera dostawcę i dodaje produkty "w locie". System tworzy w tle dokument "Ad-hoc ASN".
*   **Uszkodzony Kod Kreskowy**: Możliwość ręcznego wpisania kodu SKU lub wyszukania po nazwie, z wymuszeniem ponownego wydruku etykiety.
*   **Niezgodny Towar**: Jeśli fizyczny towar nie zgadza się z opisem w systemie (np. inna waga, inny kolor), operator może oflagować pozycję jako "Mismatch", co wstrzymuje proces dla danej linii.

## 7. Współpraca z Innymi Modułami
*   **-> Moduł 8 (Kontrola Jakości)**: Każde przyjęcie sprawdza reguły QC. Jeśli wymagana jest kontrola, LPN otrzymuje status "Do Kontroli" i system kieruje operatora do strefy QC zamiast do strefy składowania.
*   **-> Moduł 2 (Składowanie)**: Po zakończeniu przyjęcia (i ewentualnym zwolnieniu przez QC), moduł Przyjęć przekazuje dane o LPN do modułu Składowania, który oblicza optymalną lokalizację docelową (Put-away logic).
*   **-> Moduł 9 (Raportowanie)**: Moduł generuje dane do raportów: "Dokładność Dostawców" (Supplier Accuracy), "Czas Rozładunku" (Unloading Time), "Oczekujące Przyjęcia" (Inbound Backlog).
*   **-> Moduł 7 (Zwroty)**: Moduł Przyjęć współdzieli logikę z modułem Zwrotów w zakresie identyfikacji towaru, jednak Zwroty mają odrębny proces oceny stanu technicznego.

---
*Koniec specyfikacji modułu 1. Szacowana długość: ~5200 znaków.*
