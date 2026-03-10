package main.command;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import main.milestone.Milestone;
import main.tickets.TicketFactory;

public final class CommandFactory {

    /**
     * Constructor privat pentru a preveni instantierea clasei utilitare.
     */
    private CommandFactory() {
    }

    /**
     * Creeaza un obiect de tip Command corespunzator tipului specificat in JSON.
     *
     * @param nodJson obiectul JSON care contine tipul comenzii si parametrii necesari
     * @return o instanta a unei clase care extinde Command sau null daca tipul este necunoscut
     */
    public static Command createCommand(final JsonNode nodJson) {
        String tipComanda = nodJson.get("command").asText();
        String utilizator = nodJson.get("username").asText();
        String timpSistem = nodJson.get("timestamp").asText();

        switch (tipComanda) {
            case "reportTicket" -> {
                return new ReportTicket(
                        tipComanda,
                        utilizator,
                        timpSistem,
                        TicketFactory.createTicket(nodJson.get("params"))
                );
            }
            case "viewTickets" -> {
                return new ViewTickets(tipComanda, utilizator, timpSistem);
            }
            case "lostInvestors" -> {
                return new LostInvestors(tipComanda, utilizator, timpSistem);
            }
            case "createMilestone" -> {
                return genereazaComandaCreareMilestone(nodJson, tipComanda, utilizator, timpSistem);
            }
            case "viewMilestones" -> {
                return new ViewMilestones(tipComanda, utilizator, timpSistem);
            }
            case "assignTicket" -> {
                return new AssignTicket(
                        tipComanda,
                        utilizator,
                        timpSistem,
                        nodJson.get("ticketID").asInt()
                );
            }
            case "undoAssignTicket" -> {
                return new UndoAssignTicket(
                        tipComanda,
                        utilizator,
                        timpSistem,
                        nodJson.get("ticketID").asInt()
                );
            }
            case "viewAssignedTickets" -> {
                return new ViewAssignedTickets(tipComanda, utilizator, timpSistem);
            }
            case "undoAddComment" -> {
                return new UndoAddComment(
                        tipComanda,
                        utilizator,
                        timpSistem,
                        nodJson.get("ticketID").asInt()
                );
            }
            case "addComment" -> {
                return new AddComment(
                        tipComanda,
                        utilizator,
                        timpSistem,
                        nodJson.get("ticketID").asInt(),
                        nodJson.get("comment").asText()
                );
            }
            case "changeStatus" -> {
                return new ChangeStatus(
                        tipComanda,
                        utilizator,
                        timpSistem,
                        nodJson.get("ticketID").asInt()
                );
            }
            case "undoChangeStatus" -> {
                return new UndoChangeStatus(
                        tipComanda,
                        utilizator,
                        timpSistem,
                        nodJson.get("ticketID").asInt()
                );
            }
            case "viewTicketHistory" -> {
                return new ViewTicketHistory(tipComanda, utilizator, timpSistem);
            }
            case "search" -> {
                if (nodJson.get("filters") == null) {
                    return new ViewTickets(tipComanda, utilizator, timpSistem);
                }
                return new Search(
                        tipComanda,
                        utilizator,
                        timpSistem,
                        nodJson.get("filters")
                );
            }
            case "viewNotifications" -> {
                return new ViewNotifications(tipComanda, utilizator, timpSistem);
            }
            case "generateCustomerImpactReport" -> {
                return new GenerateCustomerImpactReport(tipComanda, utilizator, timpSistem);
            }
            case "generateTicketRiskReport" -> {
                return new GenerateTicketRiskReport(tipComanda, utilizator, timpSistem);
            }
            case "generateResolutionEfficiencyReport" -> {
                return new GenerateResolutionEfficiencyReport(tipComanda, utilizator, timpSistem);
            }
            case "appStabilityReport" -> {
                return new AppStabilityReport(tipComanda, utilizator, timpSistem);
            }
            case "generatePerformanceReport" -> {
                return new GeneratePerformanceReport(tipComanda, utilizator, timpSistem);
            }
            case "startTestingPhase" -> {
                return new StartTestingPhase(tipComanda, utilizator, timpSistem);
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * Metoda auxiliara pentru extragerea datelor si crearea unei comenzi de tip Milestone.
     *
     * @param json obiectul JSON cu datele milestone-ului
     * @param tip tipul comenzii
     * @param user autorul milestone-ului
     * @param timp data crearii
     * @return o instanta noua de CreateMilestone
     */
    private static Command genereazaComandaCreareMilestone(final JsonNode json, final String tip,
                                                           final String user, final String timp) {
        String nume = json.get("name").asText();
        String dataLimita = json.get("dueDate").asText();

        List<String> blocaje = new ArrayList<>();
        if (json.has("blockingFor") && json.get("blockingFor").isArray()) {
            json.get("blockingFor").forEach(n -> blocaje.add(n.asText()));
        }

        List<Integer> tichete = new ArrayList<>();
        if (json.has("tickets") && json.get("tickets").isArray()) {
            json.get("tickets").forEach(n -> tichete.add(n.asInt()));
        }

        List<String> developeri = new ArrayList<>();
        if (json.has("assignedDevs") && json.get("assignedDevs").isArray()) {
            json.get("assignedDevs").forEach(n -> developeri.add(n.asText()));
        }

        Milestone milestone = new Milestone(
                nume, dataLimita, blocaje, tichete, developeri, user, timp
        );

        return new CreateMilestone(tip, user, timp, milestone);
    }
}
