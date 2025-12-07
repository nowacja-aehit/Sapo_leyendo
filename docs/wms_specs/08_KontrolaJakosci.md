# Moduł 8: Kontrola Jakości (Quality Control) - Specyfikacja Techniczna i Funkcjonalna

## 1. Wstęp i Cel Modułu
Moduł Kontroli Jakości (QC) jest strażnikiem standardów w magazynie. Jego zadaniem jest systematyczne weryfikowanie, czy towary przyjmowane, składowane i wysyłane spełniają określone normy jakościowe. Moduł ten działa jako "bramka" (Gatekeeper), która może zablokować przepływ towaru na każdym etapie procesu logistycznego, chroniąc firmę przed kosztami reklamacji i utratą reputacji.

## 2. Założenia Projektowe
1.  **Konfigurowalne Plany Testów**: Różne produkty wymagają różnych testów (np. odzież - sprawdzenie szwów, elektronika - test włączenia, żywność - pomiar temperatury). System musi pozwalać na definiowanie dynamicznych list kontrolnych.
2.  **Standardy Próbkowania (AQL)**: System obsługuje statystyczne metody pobierania próbek (np. ISO 2859-1). Nie trzeba sprawdzać 100% dostawy; system wylicza, że dla dostawy 1000 sztuk należy sprawdzić losowo 80 sztuk.
3.  **Statusy Blokady**: Moduł QC ma nadrzędne prawo do zmiany statusu zapasu na "QC_HOLD" (Kwarantanna), co fizycznie i systemowo uniemożliwia jego użycie.

## 3. Szczegółowa Funkcjonalność

### 3.1 Wyzwalacze Kontroli (Inspection Triggers)
System automatycznie generuje zlecenia kontroli w oparciu o reguły:
*   **Przyjęcie (Inbound)**:
    *   *Nowy Produkt*: Pierwsza dostawa nowego SKU zawsze wpada na QC.
    *   *Ryzykowny Dostawca*: Dostawca z historią problemów ma ustawiony sampling 100%.
    *   *Losowo*: Co 10-ta dostawa jest kontrolowana.
*   **Zwroty (Returns)**: Każdy zwrot elektroniki trafia na testy funkcjonalne.
*   **Składowanie (In-Stock)**: Cykliczne sprawdzanie dat ważności lub kondycji opakowań dla towarów długo zalegających.

### 3.2 Proces Inspekcji (Execution)
*   **Pobranie Próbki**: System wskazuje operatorowi, ile sztuk i z których kartonów/palet ma pobrać do badania.
*   **Checklista**: Operator na terminalu/tablecie przechodzi przez listę pytań:
    1.  Czy opakowanie jest nienaruszone? [Tak/Nie]
    2.  Czy instrukcja jest w języku PL? [Tak/Nie]
    3.  Pomiar wilgotności: [Wpisz wartość]
*   **Dokumentacja Foto**: Wymóg zrobienia zdjęcia w przypadku wykrycia wady.

### 3.3 Zarządzanie Niezgodnościami (Non-Conformance)
Jeśli inspekcja wykaże błędy przekraczające dopuszczalny limit (AQL):
*   **Raport NCR**: System generuje Raport Niezgodności (Non-Conformance Report) wysyłany do Działu Zakupów i Dostawcy.
*   **Decyzja**: Manager Jakości podejmuje decyzję:
    *   *Sortowanie 100%*: Przegląd całej dostawy w celu oddzielenia dobrych od złych.
    *   *Zwrot do Dostawcy*: Odrzucenie całej partii.
    *   *Warunkowe Przyjęcie*: Akceptacja wady (np. błąd w nadruku na kartonie), zazwyczaj wiąże się z rabatem od dostawcy.

### 3.4 Kwarantanna (Quarantine Management)
*   **Wirtualna**: Towar zostaje na regale, ale jest zablokowany systemowo.
*   **Fizyczna**: System generuje zadanie przesunięcia towaru do wydzielonej, ogrodzonej strefy "Klatka QC", do której dostęp mają tylko uprawnieni pracownicy.

## 4. Model Danych (Kluczowe Encje)

### `QcInspection` (Inspekcja)
*   `id`: UUID
*   `sourceType`: Enum (INBOUND, RETURN, STOCK)
*   `referenceId`: UUID (np. ID linii przyjęcia)
*   `productId`: Long
*   `sampleSize`: Integer
*   `result`: Enum (PASS, FAIL, CONDITIONAL_PASS)
*   `inspectorId`: Long

### `TestPlan` (Plan Testów)
*   `id`: Integer
*   `name`: String (np. "Elektronika Podstawowa")
*   `steps`: List<TestStep>
*   `aqlLevel`: Double (np. 1.5)

### `NonConformanceReport` (NCR)
*   `id`: UUID
*   `inspectionId`: UUID
*   `defectType`: String (np. "CRITICAL", "MAJOR", "MINOR")
*   `description`: String
*   `photos`: List<Url>
*   `vendorResponse`: String

## 5. Logika Biznesowa i Reguły
1.  **Reguła Stop-Ship**: Wykrycie wady krytycznej (Critical Defect), np. ostre krawędzie w zabawce, natychmiast blokuje sprzedaż tego produktu w całym systemie (wszystkie partie), do czasu wyjaśnienia.
2.  **Priorytetyzacja**: Inspekcje przyjęć mają priorytet nad inspekcjami okresowymi, aby nie blokować doków rozładunkowych.
3.  **Zwolnienie Partii**: Tylko użytkownik z rolą `QUALITY_MANAGER` może zwolnić partię zablokowaną przez system (override).

## 6. Obsługa Wyjątków
*   **Zniszczenie Próbki**: Niektóre testy są niszczące (np. test wytrzymałości). System musi automatycznie zdjąć zniszczone sztuki ze stanu magazynowego jako "Koszt Jakości".
*   **Brak Narzędzi**: Operator zgłasza brak sprzętu pomiarowego (np. rozkalibrowana waga). System zawiesza inspekcję i powiadamia utrzymanie ruchu.

## 7. Współpraca z Innymi Modułami
*   **-> Moduł 1 (Przyjęcia)**: Blokuje finalizację przyjęcia do czasu wyniku QC.
*   **-> Moduł 2 (Składowanie)**: Zarządza statusami zapasu (Available <-> QC Hold).
*   **-> Moduł 7 (Zwroty)**: Jest głównym wykonawcą procesu oceny zwrotów.
*   **-> Moduł 9 (Raportowanie)**: Dostarcza kluczowy raport "Vendor Scorecard" (Ocena Dostawcy) bazujący na procencie odrzuconych dostaw.

---
*Koniec specyfikacji modułu 8. Szacowana długość: ~5100 znaków.*
