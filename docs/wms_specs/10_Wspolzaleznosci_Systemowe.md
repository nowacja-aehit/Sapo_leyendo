# Architektura Współzależności Systemowych WMS (System Interdependencies)

## 1. Wstęp
Niniejszy dokument opisuje kompleksową sieć powiązań pomiędzy dziewięcioma modułami systemu WMS Sapo Leyendo. Żaden z modułów nie funkcjonuje w próżni; każda operacja w jednym obszarze wywołuje kaskadę zdarzeń w innych. Zrozumienie tych zależności jest kluczowe dla zapewnienia spójności danych (Data Integrity) oraz ciągłości procesów biznesowych.

## 2. Przepływy Makro (End-to-End Flows)

### 2.1 Cykl "Dock-to-Stock" (Od Doku do Półki)
Ten przepływ angażuje moduły: **1 (Przyjęcia) -> 8 (QC) -> 3 (Lokacja) -> 2 (Składowanie)**.

1.  **Inicjacja**: Moduł 1 rejestruje przybycie towaru. Tworzony jest obiekt `InboundOrder`.
2.  **Bramka Jakości**: Przed finalizacją przyjęcia, Moduł 1 odpytuje Moduł 8 (QC) o status produktu.
    *   *Interakcja*: Jeśli Moduł 8 zwróci flagę `QC_REQUIRED`, Moduł 1 nadaje jednostce LPN status `IN_RECEIVING_QC`.
    *   *Blokada*: Taki LPN jest niewidoczny dla Modułu 2 (Składowanie) jako dostępny zapas.
3.  **Decyzja Lokacyjna**: Po zwolnieniu przez QC, Moduł 2 (Składowanie) odpytuje Moduł 3 (Lokacja).
    *   *Algorytm*: Moduł 2 pobiera wymiary LPN i szuka w Module 3 lokalizacji o typie pasującym do nośnika i strefie pasującej do produktu (np. Strefa Chłodnicza).
4.  **Finalizacja**: Operator odkłada towar. Moduł 2 aktualizuje stan magazynowy na `AVAILABLE`.

### 2.2 Cykl "Order-to-Cash" (Realizacja Zamówienia)
Ten przepływ angażuje moduły: **2 (Składowanie) -> 4 (Kompletacja) -> 5 (Pakowanie) -> 6 (Wysyłka)**.

1.  **Rezerwacja (Hard Allocation)**: Moduł 4 (Kompletacja) otrzymuje zamówienie. Odpytuje Moduł 2 o dostępne zasoby.
    *   *Blokada*: Moduł 2 zmienia status konkretnych sztuk towaru na `ALLOCATED`. Są one fizycznie na półce, ale logicznie "znikają" dla innych zamówień.
2.  **Ścieżka Zbiórki**: Moduł 4 pobiera z Modułu 3 (Lokacja) atrybut `pickSequence` dla każdej zarezerwowanej lokalizacji, aby posortować zadania operatora.
3.  **Przekazanie do Pakowania**: Po skompletowaniu, Moduł 4 przekazuje kontener (LPN) do Modułu 5.
    *   *Walidacja*: Moduł 5 nie pozwoli zamknąć paczki, jeśli Moduł 4 nie potwierdził zakończenia zbiórki dla tego zamówienia.
4.  **Wysyłka**: Moduł 5 tworzy paczki (`Parcel`), które są przekazywane do Modułu 6. Moduł 6 blokuje edycję zamówienia w Module 4 i 5 (zamrożenie stanu).

### 2.3 Cykl "Reverse Logistics" (Zwroty)
Ten przepływ angażuje moduły: **7 (Zwroty) -> 8 (QC) -> 2 (Składowanie) -> 9 (Raportowanie)**.

1.  **Przyjęcie Zwrotu**: Moduł 7 identyfikuje towar.
2.  **Ocena**: Moduł 7 wymusza interakcję z Modułem 8 (QC) dla oceny stanu.
3.  **Decyzja Magazynowa**:
    *   Jeśli `RESTOCK`: Moduł 7 tworzy zadanie `Put-away` w Module 2.
    *   Jeśli `SCRAP`: Moduł 7 tworzy zadanie utylizacji, pomijając Moduł 2 (towar nigdy nie staje się dostępny).
4.  **Analityka**: Moduł 9 odnotowuje powód zwrotu, co może wpłynąć na przyszłe decyzje zakupowe (np. zablokowanie wadliwego produktu w Module 1).

## 3. Macierz Zależności Danych (Data Dependency Matrix)

| Moduł Źródłowy | Moduł Docelowy | Przekazywane Dane | Cel Biznesowy |
| :--- | :--- | :--- | :--- |
| **3. Lokacja** | 2. Składowanie | Wymiary, Typ, Strefa | Walidacja czy towar zmieści się w gnieździe. |
| **3. Lokacja** | 4. Kompletacja | Sekwencja (XYZ) | Optymalizacja ścieżki zbiórki. |
| **8. QC** | 1. Przyjęcia | Sampling Plan | Określenie ile sztuk z dostawy trzeba sprawdzić. |
| **8. QC** | 2. Składowanie | Status (Hold/Release) | Blokowanie/Odblokowywanie zapasu do sprzedaży. |
| **2. Składowanie** | 9. Raportowanie | Snapshot Stanu | Wyliczenie wartości magazynu na koniec dnia. |
| **4. Kompletacja** | 5. Pakowanie | Zawartość Kosza | Weryfikacja czy pakujemy właściwe produkty. |
| **5. Pakowanie** | 6. Wysyłka | Waga, Wymiary Paczek | Zamówienie odpowiedniego transportu (TIR vs Bus). |
| **6. Wysyłka** | 4. Kompletacja | Cut-off Time | Priorytetyzacja fal (które zamówienia muszą wyjść teraz). |

## 4. Zarządzanie Spójnością Transakcyjną

W systemie rozproszonym logicznie na 9 modułów, kluczowe jest zachowanie spójności transakcji (ACID).

*   **Scenariusz Błędu**: Operator pobrał towar (Moduł 4), ale system padł przed zapisaniem tego faktu w Module 2.
    *   *Rozwiązanie*: Mechanizm "Two-Phase Commit" lub Sagi. Operacja pobrania nie jest uznana za zakończoną, dopóki Moduł 2 nie potwierdzi zdjęcia stanu `ALLOCATED` i pomniejszenia `ON_HAND`.
*   **Scenariusz Wyścigu (Race Condition)**: Dwóch operatorów próbuje pobrać ostatnią sztukę towaru (jeden dla zamówienia A, drugi dla zamówienia B).
    *   *Rozwiązanie*: Moduł 2 (Składowanie) działa jako "Single Source of Truth". Blokada na poziomie rekordu bazy danych (Row Lock) podczas rezerwacji zapobiega podwójnej alokacji.

## 5. Wpływ Awarii Modułu (Failure Modes)

Analiza wpływu niedostępności poszczególnych modułów na całość systemu:

1.  **Awaria Modułu 3 (Lokacja)**:
    *   *Skutek*: Paraliż krytyczny. Moduł 1 nie wie gdzie odłożyć towar. Moduł 4 nie wie skąd pobrać.
    *   *Mitigacja*: Cache'owanie danych lokalizacyjnych w pamięci podręcznej innych modułów (tryb Read-Only).
2.  **Awaria Modułu 8 (QC)**:
    *   *Skutek*: Zator na przyjęciach. Towary czekają na zwolnienie.
    *   *Mitigacja*: Tryb awaryjny "Bypass QC" – Manager może ręcznie zezwolić na pominięcie kontroli dla towarów niskiego ryzyka.
3.  **Awaria Modułu 9 (Raportowanie)**:
    *   *Skutek*: Brak widoczności zarządczej. Operacje fizyczne (przyjęcia, wysyłki) mogą trwać nadal.
    *   *Mitigacja*: Praca "na ślepo" do czasu przywrócenia analityki.

## 6. Podsumowanie Architektury
System Sapo Leyendo WMS został zaprojektowany jako organizm naczyń połączonych. Chociaż każdy moduł posiada własną odpowiedzialność biznesową, ich siła leży w ścisłej integracji.
*   **Moduł 2 (Składowanie)** jest "sercem" (pompuje zapas).
*   **Moduł 3 (Lokacja)** jest "szkieletem" (nadaje strukturę).
*   **Moduł 9 (Raportowanie)** jest "mózgiem" (analizuje i wnioskuje).
*   **Moduły 1, 4, 5, 6, 7** są "kończynami" (wykonują pracę fizyczną).
*   **Moduł 8 (QC)** jest "układem odpornościowym" (eliminuje zagrożenia).

Taka architektura zapewnia skalowalność, bezpieczeństwo procesów i możliwość dalszego rozwoju systemu.

---
*Koniec dokumentacji współzależności. Dokument ten stanowi mapę drogową dla integracji wszystkich 9 modułów.*
