# PO_II Projekt

#### Wymagania:

1. Nalezy opracowac aplikacje kliencka i serwerowa przy podanych ponizej zalozeniach:

2. Aplikacja kliencka:
   - Uruchamiana jest z dwoma parametrami: nazwa uzytkownika i sciezka do lokalnego folderu
   - Kazdy klient ma swoj lokalny folder z plikami
   - Aplikacja obserwuje lokalny folder i reaguje na zmiany. Jak pojawia sie tam nowe pliki, to wysyla je na serwer
   - Jak pojawi sie nowy plik dla danego uzytkownika, to jest on pobierany do lokalnego folderu
   - Aplikacja po uruchomieniu odpytuje serwer o nowe pliki i je sciaga
   - Wysylanie / odbieranie dzieje sie przy wykorzystaniu puli watkow
   - Aplikacja kliencka ma interfejs graficzny (np. Java FX) pokazujacy w czasie rzeczywistym czym sie w danej chwili zajmuje klient ("Pobieram...", "Wysylam ...", "Sprawdzam ....") oraz wyswietlajacy liste aktualnych plikow w lokalnym folderze. Panel graficzny ma umozliwic takze udostepnienie danego pliku innemu uzytkownikowi. Liste dostepnych uzytkownikow nalezy pobrac z serwera.

3. Serwer:
   - 5 folderow, ktore symulują 5 serwerow lub 5 dyskow
   - Klient wysyla np. 10 plikow, wiec serwer uruchamia iles watkow na ktorych rownolegle kopiuje pliki do tych dyskow (folderow)
   - Wymagany jest kontroler, ktory tak rozlozy ruch, ze do kazdego z dyskow (folderow) jednoczesnie jest kopiowana taka sama liczba plikow
   - Jezeli podlaczy sie drugi klient, ktory zacznie wysylac pliki, nie moze on czekac az skoncza sie zadania pierwszego klienta. Lista zadan na serwerze musi ulec reorganizacji, tak aby obydwaj klienci mieli wrazenie natychmiastowej obslugi (zaproponuj stosowny algorytm)
   - Na kazdym dysku serwera znajduje sie plik tekstowy (np. csv), w ktorym jest opisana zawartosc danego dysku i kto jest jego wlascicielem. Zauwaz, ze plik bedzie uaktualniany przez wiele watkow. Rozwiaz ten problem.
   - W celu wizualizacji symulacji na niewielkiej liczbie uzytkownikow, czas kopiowania ma byc sztucznie wydluzony poprzez usypianie watku na losowa liczbe sekund.
   - Serwer posiada panel graficzny (np. Java FX) pokazujacy zawartosc 5ciu dyskow (serwerow) oraz aktualnie wykonywane operacje.

4. Zaproponuj liste testow jednostkowych opracowanych w JUnit, osobno dla klienta i serwera obejmujacych kluczowe operacje wykonywane przez kazdego z nich.

5. Powyzsza implementacja pozwala na uzyskanie oceny 4. W celu uzyskania oceny 5 nalezy zaimplementowac dodatkowe funkcjonalnosci (uogolniajac: mozna zaproponowac dwie funkcjonalnosci, z ktorych kazda podnosi ocene o 0,5) uzgodnione z prowadzacym. Uzgodnienie tych funkcjonalnosci musi nastapic najpozniej na laboratorium nr 10.

#### Funkcjonalności dodatkowe

1. Limit miejsca:
   - Każdy użytkownik ma z góry przydzieloną ilość miejsca na serwerze
   - Serwer nie przyjmie pliku kiedy jego zapis spowoduje przekroczenie limitu
   - Klient przenosi pliki które nie zostały przesłane do oddzielnego katalogu.
   
2. Powiadomienia
   - Klient wysyła powiadomienia kiedy:
     - Zbliża się do osiągnięcia limitu miejsca (przy każdym wysyłaniu pliku)
     - Wysyłanie pliku zostało odrzucone
     - Użytkownikowi udostępniono nowy plik
     - Plik zdalny (udstępniony) został usunięty
     - Plik udostępniony użytkownikowi został zedytowany przez użytkownika (zmiana na plik własny)
     - Został zgłoszony wyjątek którego nie da się obsłużyć.
   - Serwer wysyła powiadomienia kiedy:
     - Został zgłoszony wyjątek którego nie da się obsłużyć.
