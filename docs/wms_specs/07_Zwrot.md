# Moduł 7: Zwrot (Returns/RMA) - Specyfikacja Techniczna i Funkcjonalna

## 1. Wstęp i Cel Modułu
Moduł Zwrotów (Reverse Logistics) obsługuje proces powrotu towarów od klienta do magazynu. Jest to proces o wysokim stopniu skomplikowania ze względu na nieprzewidywalność stanu zwracanego towaru oraz konieczność szybkiej decyzji o jego dalszym losie (odsprzedaż, naprawa, utylizacja). Celem modułu jest sprawne przyjęcie zwrotu, ocena jego jakości, zaktualizowanie stanu magazynowego oraz uruchomienie procedury zwrotu środków klientowi (poprzez integrację z ERP/Płatnościami).

## 2. Założenia Projektowe
1.  **Autoryzacja (RMA)**: Preferowany proces opiera się na numerze RMA (Return Merchandise Authorization) wygenerowanym wcześniej przez klienta. Ułatwia to identyfikację zamówienia pierwotnego.
2.  **Strefa Kwarantanny**: Fizycznie zwroty trafiają do odseparowanej strefy, aby nie pomieszały się z towarem pełnowartościowym przed oceną jakości.
3.  **Gradacja (Grading)**: Każda zwracana sztuka musi przejść proces oceny stanu (np. Klasa A - jak nowy, Klasa B - otwarte pudełko, Klasa C - uszkodzony).

## 3. Szczegółowa Funkcjonalność

### 3.1 Rejestracja Zwrotu (RMA Creation)
*   **Portal Klienta**: Moduł udostępnia API dla sklepu internetowego, gdzie klient zgłasza chęć zwrotu.
*   **Powody Zwrotu**: Rejestracja przyczyny (np. "Nie pasuje rozmiar", "Uszkodzone w transporcie", "Niezgodne z opisem") – kluczowe dla analityki.
*   **Generowanie Etykiety**: System może wygenerować etykietę zwrotną dla klienta.

### 3.2 Przyjęcie Fizyczne (Receiving Returns)
*   **Skanowanie Przesyłki**: Operator skanuje etykietę zwrotną. System wyszukuje powiązane zamówienie sprzedaży.
*   **Blind Returns**: Jeśli brak etykiety RMA, operator wyszukuje zamówienie po nazwisku klienta, numerze faktury lub numerze seryjnym produktu.
*   **Weryfikacja Zawartości**: Sprawdzenie czy w paczce znajduje się to, co klient zgłosił.

### 3.3 Ocena Stanu i Decyzja (Grading & Disposition)
Operator (lub specjalista QC) ocenia każdą sztukę odpowiadając na pytania w systemie (np. "Czy folia naruszona?", "Czy nosi ślady użytkowania?").
Na podstawie odpowiedzi system sugeruje dyspozycję:
*   **Restock (Do zapasu)**: Towar pełnowartościowy, wraca na półkę do sprzedaży.
*   **Refurbish (Do odnowienia)**: Wymaga przepakowania, czyszczenia lub drobnej naprawy.
*   **Vendor Return (Do dostawcy)**: Towar wadliwy fabrycznie, do odesłania producentowi.
*   **Scrap (Utylizacja)**: Towar zniszczony, niebezpieczny lub przeterminowany.

### 3.4 Procesowanie Finansowe
*   **Trigger Zwrotu Środków**: Po zatwierdzeniu oceny stanu, moduł wysyła sygnał do systemu ERP/E-commerce: "Zwróć 100% kwoty" lub "Zwróć 50% kwoty (uszkodzenie z winy klienta)".

## 4. Model Danych (Kluczowe Encje)

### `RmaRequest` (Zgłoszenie Zwrotu)
*   `id`: UUID
*   `originalOrderId`: UUID
*   `customerReason`: String
*   `status`: Enum (PENDING, APPROVED, RECEIVED, COMPLETED, REJECTED)
*   `trackingNumberIn`: String (Nr przesyłki zwrotnej)

### `ReturnItem` (Pozycja Zwrotu)
*   `id`: UUID
*   `rmaId`: UUID
*   `productId`: Long
*   `serialNumber`: String
*   `gradingStatus`: Enum (GRADE_A, GRADE_B, GRADE_C, SCRAP)
*   `disposition`: Enum (RESTOCK, REFURBISH, VENDOR, TRASH)
*   `inspectorComment`: String

### `RefurbishTask` (Zadanie Odnowienia)
*   `id`: UUID
*   `returnItemId`: UUID
*   `requiredActions`: List<String> (np. ["Wymień pudełko", "Wyczyść ekran"])
*   `status`: Enum (OPEN, IN_PROGRESS, DONE)

## 5. Logika Biznesowa i Reguły
1.  **Reguła Walidacji Seryjnej**: System sprawdza, czy zwracany numer seryjny faktycznie został sprzedany temu klientowi. Zapobiega to oszustwom (podmiana sprzętu na stary/zepsuty).
2.  **Reguła Higieniczna**: Dla kategorii takich jak bielizna czy kosmetyki, otwarcie opakowania automatycznie wymusza dyspozycję "Scrap" (Utylizacja), bez względu na stan wizualny.
3.  **Automatyczny Restock**: Jeśli towar jest w stanie idealnym (Grade A), system może automatycznie wygenerować zadanie Put-away do strefy kompletacji, aby jak najszybciej sprzedać go ponownie.

## 6. Obsługa Wyjątków
*   **Paczka Pusta/Cegła**: Klient odesłał puste pudełko. Operator dokumentuje to zdjęciem (załącznik do `ReturnItem`) i odrzuca zwrot.
*   **Towar Obcy**: Klient odesłał produkt, którego nie mamy w ofercie (pomyłka klienta). System generuje proces "Odesłanie do klienta na jego koszt".

## 7. Współpraca z Innymi Modułami
*   **-> Moduł 2 (Składowanie)**: Towary zakwalifikowane jako "Restock" zwiększają stan magazynowy.
*   **-> Moduł 8 (QC)**: Skomplikowane zwroty (np. elektronika) są przekazywane do Modułu QC na szczegółowe testy funkcjonalne.
*   **-> Moduł 1 (Przyjęcia)**: Zwroty wykorzystują te same doki co dostawy, ale wymagają osobnego procesu w systemie.
*   **-> Moduł 9 (Raportowanie)**: Analiza powodów zwrotów pomaga działowi zakupów eliminować wadliwe produkty z oferty.

---
*Koniec specyfikacji modułu 7. Szacowana długość: ~4900 znaków.*
