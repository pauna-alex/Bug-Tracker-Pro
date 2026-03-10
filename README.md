# Bug Tracker Pro

# Arhitectura si Gestiunea Datelor

- Aplicatia se bazeaza pe trei surse centrale de date, fiecare implementata ca **Singleton**: `DatabaseUser`, `DatabaseTickets` si `DatabaseMilestone`. Folosirea Singleton asigura o singura instanta pe durata rularii, astfel incat orice modul are acces la aceeasi realitate a datelor, fara a trece starea prin parametri peste tot.
- Punctul de intrare este `App`. Aici se incarca utilizatorii din JSON cu `UserFactory`, se citesc si se construiesc comenzile din fisierul de input prin `CommandFactory`, se seteaza momentul de inceput al fazei de testare in `DatabaseTickets`, iar apoi se proceseaza comenzile secvential.
- La inceputul executiei, `App` curata explicit toate bazele de date pentru a asigura un mediu de testare izolat si predictibil.
- Un detaliu important: inainte de fiecare comanda, `App` invoca `DatabaseMilestone.refreshAllMilestones(timestamp-ul curent al comenzii)`. Aceasta recalculeaza starea tuturor milestone-urilor la momentul respectiv, aplicand regulile de actualizare (prioritati, blocaje, progres, notificari), asigurand un context corect pentru executie.

# Sistemul de Tichete si Visitor

- Pentru a evita aglomerarea claselor de tichete cu formule, calculele sunt externalizate prin pattern-ul **Visitor**.
- Interfata `TicketVisitor` defineste metode de vizitare pentru fiecare tip concret de tichet (`Bug`, `FeatureRequest`, `UiFeedback`).
- Implementarea foloseste mecanismul de **Double Dispatch** (metoda `accept` in Ticket si `visit` in Visitor) pentru a asigura ca formula corecta este aplicata la runtime, in functie de tipul concret al tichetului, fara verificari de tip `instanceof`.

# Strategii de Calcul si Metrici ale Performantei

- Pachetul `main/metrics` foloseste **Strategy** pentru a separa algoritmii de calcul de restul codului.
- Interfata `MetricStrategy` se foloseste pentru calcule pe tichete (impact, risc, eficienta rezolvarii).
- Interfata `PerformanceStrategy` defineste cum se calculeaza scorul de performanta pentru dezvoltatori pe baza tichetelelor inchise.
- Strategiile sunt diferentiate pe nivele: Junior (accent pe diversitate), Mid (echilibru intre volum si prioritate) si Senior (accent puternic pe urgenta si viteza de rezolvare).
- Utilitarul `MetricUtils` centralizeaza conversiile din valori text (ex: "CRITICAL", "XL") in scoruri numerice pentru formule concise.

# Motor de Cautare si Filtre

- Interfata `SearchFilter<T>` — reprezinta baza pentru criteriile atomice de filtrare (prioritate, tip, data crearii, keyword, expertiza, senioritate, scor performanta).
- `AllFilters<T>` — grupeaza mai multe filtre si le aplica in lant, asigurand ca un element este validat doar daca satisface toate conditiile (logica AND).
- `FilterBuilder` — construieste setul de filtre pornind din parametrii JSON si din rolul utilizatorului (ex: managerul poate filtra subordonatii, developerul doar tichetele accesibile).

# Update si Reguli de Actualizare

- `Update` coordoneaza reguli pe fiecare milestone, in ordine stabila pentru a evita efectele secundare:
    - `SyncTicketsStateWithMilestone`: Sincronizeaza tichetele deschise/inchise si stabilizeaza statusul milestone-ului.
    - `CheckDueTomorrowRule`: Verifica termenul apropiat, emite notificari si trece tichetele neinchise la prioritate CRITICAL.
    - `UpdateBlockingStatus`: Seteaza blocaje pe baza dependentelor dintre milestone-uri.
    - `ApplyThreeDayRule`: Creste automat prioritatea ticheteleor neinchise la fiecare trei zile de stagnare.
    - `UpdatePrioritiesTicketBecauseOfDev`: Verifica compatibilitatea cu developerul asignat si il dezasigneaza automat daca noua prioritate depaseste nivelul permis.
    - `UpdateMilestoneMetrics`: Recalculeaza progresul procentual si indicatorii de termen (daysUntilDue/overdueBy).
- Fiecare regula este izolata intr-o clasa proprie care implementeaza `UpdateRule`, permitand extinderea pipeline-ului de update fara a modifica logica existenta.

# Gestiunea Comenzilor

- Comenzile sunt obiecte independente care implementeaza interfata `Exe`; `CommandFactory` creeaza instanta corecta din input JSON.
- `App` orchestreaza executarea comenzilor si gestionarea starii aplicatiei.
- Sistemul suporta operatii de **Undo** (ex: `UndoAssignTicket`, `UndoChangeStatus`) prin salvarea starii anterioare in obiecte de tip `TicketAction` in istoricul fiecarui tichet.

# Open/Closed Principles

- Urmand principiul **Open/Closed**, am proiectat sistemul astfel incat sa poata fi extins prin adaugarea de noi clase, fara a modifica nucleul aplicatiei:
    - **Comenzi noi**: Implementare `Exe` + adaugare in `CommandFactory`.
    - **Metrici noi**: Implementare `MetricStrategy` + suport in `TicketVisitor`.
    - **Filtre noi**: Implementare `SearchFilter<T>` + integrare in `FilterBuilder`.
    - **Reguli noi**: Implementare `UpdateRule` + plasare in lista de executie din `Update`.

# Flux Operational

- `App.run` curata Singletoanele si incarca baza de date initiala.
- Pentru fiecare comanda: ruleaza actualizarile automate pe milestone-uri la timestamp-ul comenzii, executa comanda si stocheaza rezultatul intr-un raport final.
- La final, scrie fisierul JSON cu rezultatele tuturor operatiunilor.

# Design Patterns Utilizate

* **Singleton**: Folosit pentru bazele de date (`DatabaseUser`, `DatabaseTickets`, `DatabaseMilestone`) si colectorul de comenzi (`CommandList`).
* **Factory**: Centralizeaza instantierea obiectelor complexe in `UserFactory`, `TicketFactory` si `CommandFactory` pe baza datelor din JSON.
* **Command**: Fiecare actiune (report, assign, status change) este incapsulata intr-un obiect ce implementeaza interfata `Exe`, facilitand trasabilitatea si operatiile de **Undo**.
* **Visitor**: Utilizat in pachetul de metrici pentru a calcula riscul si impactul fara a incarca clasele de tichete cu formule matematice.
* **Strategy**: Aplicat in calculul performantei dezvoltatorilor, permitand schimbarea algoritmului la runtime in functie de senioritatea utilizatorului evaluat.
* **Observer**: Interfata `Observer` este implementata de `User` pentru a primi notificari automate despre deblocari de milestone-uri sau urgente.
* **Builder**: Regasit in `FilterBuilder` pentru a construi pas cu pas liste complexe de filtre de cautare din parametri JSON variabili.