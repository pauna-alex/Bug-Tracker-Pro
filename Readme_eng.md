# Bug Tracker Pro

## Architecture and Data Management

* The application relies on three central data sources, each implemented as a **Singleton**: `DatabaseUser`, `DatabaseTickets`, and `DatabaseMilestone`. Using the Singleton pattern ensures a single instance exists during runtime, granting every module access to the same "data reality" without passing state through parameters.
* The entry point is the `App` class. Here, users are loaded from JSON via `UserFactory`, commands are parsed and constructed from the input file through `CommandFactory`, the testing phase start time is set in `DatabaseTickets`, and commands are then processed sequentially.
* At the start of execution, `App` explicitly clears all databases to ensure an isolated and predictable testing environment.
* **Crucial Detail:** Before executing each command, `App` invokes `DatabaseMilestone.refreshAllMilestones(current_command_timestamp)`. This recalculates the state of all milestones at that specific moment, applying update rules (priorities, blockers, progress, notifications) to ensure a correct execution context.

---

## Ticket System and Visitor Pattern

* To avoid cluttering ticket classes with mathematical formulas, calculations are externalized using the **Visitor** pattern.
* The `TicketVisitor` interface defines visiting methods for each concrete ticket type (`Bug`, `FeatureRequest`, `UiFeedback`).
* The implementation utilizes the **Double Dispatch** mechanism (`accept` method in Ticket and `visit` in Visitor) to ensure the correct formula is applied at runtime based on the concrete ticket type, without using `instanceof` checks.

---

## Calculation Strategies and Performance Metrics

* The `main/metrics` package employs the **Strategy** pattern to decouple calculation algorithms from the rest of the code.
* The `MetricStrategy` interface is used for ticket-related calculations (impact, risk, resolution efficiency).
* The `PerformanceStrategy` interface defines how performance scores are calculated for developers based on closed tickets.
* Strategies are tiered by level: **Junior** (focus on diversity), **Mid** (balance between volume and priority), and **Senior** (heavy focus on urgency and resolution speed).
* The `MetricUtils` utility centralizes the conversion of text values (e.g., "CRITICAL", "XL") into numerical scores for concise formulas.

---

## Search Engine and Filters

* `SearchFilter<T>` Interface: Represents the base for atomic filtering criteria (priority, type, creation date, keyword, expertise, seniority, performance score).
* `AllFilters<T>`: Groups multiple filters and applies them in a chain, ensuring an element is validated only if it satisfies all conditions (**AND logic**).
* `FilterBuilder`: Constructs the filter set starting from JSON parameters and the user's role (e.g., a manager can filter subordinates, while a developer only sees accessible tickets).

---

## Update Logic and Rules

`Update` coordinates rules for each milestone in a stable order to prevent side effects:
* `SyncTicketsStateWithMilestone`: Synchronizes open/closed tickets and stabilizes milestone status.
* `CheckDueTomorrowRule`: Checks for upcoming deadlines, issues notifications, and upgrades unclosed tickets to **CRITICAL** priority.
* `UpdateBlockingStatus`: Sets blockers based on dependencies between milestones.
* `ApplyThreeDayRule`: Automatically increases the priority of unclosed tickets for every three days of stagnation.
* `UpdatePrioritiesTicketBecauseOfDev`: Checks compatibility with the assigned developer and automatically unassigns them if the new priority exceeds their permitted level.
* `UpdateMilestoneMetrics`: Recalculates percentage progress and deadline indicators (`daysUntilDue` / `overdueBy`).
* Each rule is isolated in its own class implementing `UpdateRule`, allowing the update pipeline to be extended without modifying existing logic.

---

## Command Management

* Commands are independent objects implementing the `Exe` interface; `CommandFactory` creates the correct instance from JSON input.
* `App` orchestrates command execution and application state management.
* The system supports **Undo** operations (e.g., `UndoAssignTicket`, `UndoChangeStatus`) by saving the previous state in `TicketAction` objects within each ticket's history.

---

## Open/Closed Principles

Following the **Open/Closed Principle**, the system is designed to be extended by adding new classes without modifying the core:
* **New Commands:** Implement `Exe` + add to `CommandFactory`.
* **New Metrics:** Implement `MetricStrategy` + add support in `TicketVisitor`.
* **New Filters:** Implement `SearchFilter<T>` + integrate into `FilterBuilder`.
* **New Rules:** Implement `UpdateRule` + add to the execution list in `Update`.

---

## Operational Flow

1.  `App.run` clears Singletons and loads the initial database.
2.  **For each command:** It runs automated updates on milestones at the command's timestamp, executes the command, and stores the result in a final report.
3.  Finally, it writes the JSON file containing the results of all operations.

---

## Design Patterns Used

* **Singleton:** Used for databases (`DatabaseUser`, `DatabaseTickets`, `DatabaseMilestone`) and the command collector (`CommandList`).
* **Factory:** Centralizes the instantiation of complex objects in `UserFactory`, `TicketFactory`, and `CommandFactory` based on JSON data.
* **Command:** Every action (report, assign, status change) is encapsulated in an object implementing the `Exe` interface, facilitating traceability and **Undo** operations.
* **Visitor:** Used in the metrics package to calculate risk and impact without bloating ticket classes with math.
* **Strategy:** Applied in developer performance calculations, allowing the algorithm to switch at runtime based on the seniority of the user being evaluated.
* **Observer:** The `Observer` interface is implemented by `User` to receive automated notifications regarding milestone unblocking or emergencies.
* **Builder:** Utilized in `FilterBuilder` to step-by-step construct complex search filter lists from variable JSON parameters.
