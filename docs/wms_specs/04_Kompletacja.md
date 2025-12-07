# Moduł 4: Kompletacja (Picking) - Specyfikacja Techniczna i Funkcjonalna

## 1. Wstęp i Cel Modułu
Moduł Kompletacji (Picking) jest operacyjnym sercem magazynu dystrybucyjnego. To tutaj realizowany jest główny cel biznesowy – przygotowanie zamówień klientów do wysyłki. Proces ten jest zazwyczaj najbardziej pracochłonnym i kosztownym elementem logistyki magazynowej, dlatego głównym celem tego modułu jest maksymalizacja wydajności (ilość linii na godzinę) przy jednoczesnym zachowaniu 100% dokładności. Moduł zarządza przekształceniem wirtualnych zamówień sprzedażowych w fizyczne ruchy magazynowe, sterując pracą ludzi i maszyn.

## 2. Założenia Projektowe
1.  **Rezerwacja Zapasu (Allocation)**: Żadne zadanie kompletacji nie może zostać wydane operatorowi, jeśli system wcześniej nie zarezerwował pod nie konkretnego towaru w konkretnej lokalizacji (Hard Allocation).
2.  **Strategie Zbiórki**: System musi obsługiwać wiele metod kompletacji jednocześnie, dobierając odpowiednią w zależności od profilu zamówienia (np. Single Order Picking dla dużych zamówień B2B, Multi-Order Batch Picking dla e-commerce).
3.  **Bezpapierowość**: Proces jest projektowany jako w pełni cyfrowy, obsługiwany przez terminale radiowe (RF), systemy Voice Picking lub Pick-by-Light.
4.  **Ścieżka Optymalna**: Operator nigdy nie powinien cofać się w alejce. Kolejność zadań wynika z sekwencji lokalizacji zdefiniowanej w Module 3.

## 3. Szczegółowa Funkcjonalność

### 3.1 Planowanie Fal (Wave Planning)
Mechanizm grupowania zamówień w logiczne paczki (Fale) w celu optymalizacji pracy.
*   **Kryteria Grupowania**: Możliwość tworzenia fal według przewoźnika (np. "Wszystkie zamówienia DHL na 14:00"), priorytetu, strefy magazynowej lub typu klienta.
*   **Symulacja Fali**: Przed uruchomieniem fali system sprawdza dostępność towaru. Jeśli brakuje towaru dla 5% zamówień, Manager może zdecydować o puszczeniu fali częściowej lub wstrzymaniu jej do czasu uzupełnienia (Replenishment).
*   **Uwolnienie Fali**: Moment przekształcenia zamówień w zadania magazynowe (Pick Tasks).

### 3.2 Strategie Kompletacji
*   **Discrete Picking (Jeden do Jednego)**: Operator pobiera towar dla jednego zamówienia na raz. Stosowane dla dużych zamówień paletowych.
*   **Batch Picking (Zbiórka Multizamówieniowa)**: Operator pobiera sumaryczną ilość towaru dla wielu zamówień jednocześnie do jednego wózka, a następnie sortuje je (w procesie Pakowania lub na wózku z przegródkami).
*   **Zone Picking (Zbiórka Strefowa)**: Zamówienie jest dzielone na części. Operator w strefie A zbiera swoje linie, operator w strefie B swoje. Pojemnik z zamówieniem podróżuje między strefami (Pick & Pass) lub części są konsolidowane na końcu.

### 3.3 Realizacja Zbiórki (Execution)
*   **Nawigacja**: Terminal wskazuje operatorowi lokalizację źródłową, kod produktu i ilość do pobrania.
*   **Potwierdzenie**: Wymagane zeskanowanie lokalizacji oraz produktu (lub LPN) w celu potwierdzenia poprawności.
*   **Obsługa Numerów Seryjnych**: Dla towarów śledzonych (np. elektronika), system wymusza zeskanowanie numeru seryjnego (S/N) każdej sztuki podczas pobrania.
*   **Catch Weight**: Dla towarów ważonych (np. żywność), operator musi wprowadzić lub pobrać z wagi rzeczywistą masę pobranego towaru.

### 3.4 Obsługa Braków (Short Picking)
Sytuacja, gdy system wskazuje lokalizację, a fizycznie towaru tam nie ma.
*   **Zgłoszenie Braku**: Operator wybiera opcję "Short Pick".
*   **Reakcja Systemu**:
    1.  System próbuje znaleźć ten sam towar w innej lokalizacji i dynamicznie aktualizuje zadanie operatora (jeśli konfiguracja na to pozwala).
    2.  Jeśli towaru nie ma nigdzie indziej, linia zamówienia jest oznaczana jako "Short" i trafia do wyjaśnienia.
    3.  Lokalizacja źródłowa jest automatycznie oflagowana do inwentaryzacji (Cycle Count Trigger).

## 4. Model Danych (Kluczowe Encje)

### `Wave` (Fala)
*   `id`: UUID
*   `name`: String (np. "DHL_1400_BATCH1")
*   `status`: Enum (PLANNED, ALLOCATED, RELEASED, IN_PROGRESS, COMPLETED, CLOSED)
*   `createdDate`: Timestamp
*   `cutoffTime`: Timestamp

### `PickList` (Lista Zbiórki)
*   `id`: UUID
*   `waveId`: UUID
*   `assignedUserId`: Long
*   `type`: Enum (BATCH, SINGLE, ZONE)
*   `status`: Enum (PENDING, STARTED, COMPLETED)
*   `containerLpn`: String (LPN wózka/pojemnika kompletacyjnego)

### `PickTask` (Zadanie Pobrania)
*   `id`: UUID
*   `pickListId`: UUID
*   `orderId`: UUID
*   `productId`: Long
*   `locationId`: Integer
*   `quantityToPick`: Integer
*   `quantityPicked`: Integer
*   `status`: Enum (OPEN, ASSIGNED, PICKED, SHORT)
*   `sequence`: Integer (Kolejność ścieżki)

## 5. Logika Biznesowa i Reguły
1.  **Reguła Rezerwacji (Allocation Strategy)**: System rezerwuje towar według ściśle określonych priorytetów:
    *   Najpierw pełne palety (jeśli zamówienie jest duże).
    *   Potem pełne kartony.
    *   Na końcu sztuki luzem z lokalizacji "Pick Face".
2.  **Blokada Współbieżności**: Dwóch operatorów nie może otrzymać zadania pobrania z tej samej lokalizacji w tym samym czasie, aby uniknąć "korków" w alejce (chyba że lokalizacja jest bardzo duża).
3.  **Walidacja Nośnika**: Przed rozpoczęciem zbiórki operator musi zeskanować LPN wózka lub pojemnika, do którego będzie zbierał towar. System wiąże ten LPN z realizowanymi zamówieniami.

## 6. Obsługa Wyjątków
*   **Uszkodzony Towar przy Zbiórce**: Operator znajduje towar, ale jest on uszkodzony. Zgłasza "Damaged", system blokuje tę sztukę (zmiana statusu na QC/Damaged) i próbuje przekierować operatora do innej lokalizacji po dobrą sztukę.
*   **Przepełnienie Wózka**: Operator zgłasza, że wózek jest pełny przed zakończeniem listy. System pozwala "odstawić" pełny wózek (generując etykietę "Part 1 of 2") i zeskanować nowy pusty wózek, aby kontynuować zadanie.

## 7. Współpraca z Innymi Modułami
*   **<- Moduł 2 (Składowanie)**: Moduł Kompletacji jest "konsumentem" zapasu zarządzanego przez Moduł 2. Każde pobranie pomniejsza stan "Allocated" w lokalizacji.
*   **<- Moduł 3 (Lokacja)**: Wykorzystuje sekwencję ścieżek do sortowania zadań.
*   **-> Moduł 5 (Pakowanie)**: Po zakończeniu zbiórki, pojemniki/wózki trafiają do strefy pakowania. Moduł Kompletacji przekazuje dane o zawartości każdego pojemnika (LPN) do Modułu Pakowania.
*   **-> Moduł 6 (Wysyłka)**: W przypadku kompletacji całopaletowej (gdzie nie ma procesu pakowania), Moduł Kompletacji może bezpośrednio przekazać paletę do Modułu Wysyłki (Stage & Load).

---
*Koniec specyfikacji modułu 4. Szacowana długość: ~5300 znaków.*
