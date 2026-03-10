package main.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import main.CommandList;
import main.DatabaseTickets;
import main.metrics.CustomerImpact;
import main.metrics.MetricStrategy;
import main.metrics.MetricUtils;
import main.metrics.TicketRisk;
import main.tickets.Ticket;

/**
 * Comanda pentru generarea unui raport de stabilitate a aplicatiei.
 * Analizeaza tichetele deschise in functie de tip, prioritate, risc si impact.
 */
public final class AppStabilityReport extends Command implements Exe {

    private final ObjectMapper mapper = new ObjectMapper();
    private static final double IMPACT_THRESHOLD = 50.0;

    /**
     * Constructor pentru comanda AppStabilityReport.
     *
     * @param command numele comenzii
     * @param username numele utilizatorului care solicita raportul
     * @param timestamp momentul in care este solicitat raportul
     */
    public AppStabilityReport(final String command, final String username, final String timestamp) {
        super(command, username, timestamp);
    }

    /**
     * Executa generarea raportului de stabilitate.
     * Calculeaza mediile de risc si impact si determina starea sistemului.
     * Daca aplicatia este stabila, se solicita oprirea sistemului.
     *
     * @return un ObjectNode ce contine raportul detaliat
     */
    @Override
    public ObjectNode execute() {
        ObjectNode output = mapper.createObjectNode();

        output.put("command", getCommandType());
        output.put("username", getUsername());
        output.put("timestamp", getTimeStamp());

        Map<String, Integer> ticketsCountByType = new LinkedHashMap<>();
        ticketsCountByType.put("BUG", 0);
        ticketsCountByType.put("FEATURE_REQUEST", 0);
        ticketsCountByType.put("UI_FEEDBACK", 0);

        Map<String, Integer> ticketsCountByPriority = new LinkedHashMap<>();
        ticketsCountByPriority.put("LOW", 0);
        ticketsCountByPriority.put("MEDIUM", 0);
        ticketsCountByPriority.put("HIGH", 0);
        ticketsCountByPriority.put("CRITICAL", 0);

        Map<String, List<Double>> riskScoresByType = new LinkedHashMap<>();
        Map<String, List<Double>> impactScoresByType = new LinkedHashMap<>();

        for (String type : ticketsCountByType.keySet()) {
            riskScoresByType.put(type, new ArrayList<>());
            impactScoresByType.put(type, new ArrayList<>());
        }

        MetricStrategy riskStrategy = new TicketRisk();
        MetricStrategy impactStrategy = new CustomerImpact();

        List<Ticket> activeTickets = DatabaseTickets.getInstance().getOpenInProgressTickets();

        for (Ticket ticket : activeTickets) {
            String type = ticket.getType();
            String priority = ticket.getBusinessPriority();

            ticketsCountByType.computeIfPresent(type, (k, val) -> val + 1);
            ticketsCountByPriority.computeIfPresent(priority, (k, val) -> val + 1);

            double riskValue = riskStrategy.calculate(ticket);
            double impactValue = impactStrategy.calculate(ticket);

            if (riskScoresByType.containsKey(type)) {
                riskScoresByType.get(type).add(riskValue);
                impactScoresByType.get(type).add(impactValue);
            }
        }

        ObjectNode reportNode = output.putObject("report");
        reportNode.put("totalOpenTickets", activeTickets.size());

        ObjectNode typeNode = reportNode.putObject("openTicketsByType");
        ticketsCountByType.forEach(typeNode::put);

        ObjectNode priorityNode = reportNode.putObject("openTicketsByPriority");
        ticketsCountByPriority.forEach(priorityNode::put);

        ObjectNode riskNode = reportNode.putObject("riskByType");
        ObjectNode impactNode = reportNode.putObject("impactByType");

        boolean allRisksNegligible = true;
        boolean anySignificantRiskDetected = false;
        boolean allImpactsAreLow = true;

        for (String type : ticketsCountByType.keySet()) {
            List<Double> typeRiskScores = riskScoresByType.get(type);
            double avgRisk = MetricUtils.calculateAverageImpact(typeRiskScores);
            String riskLabel = MetricUtils.getRiskLabel(avgRisk);
            riskNode.put(type, riskLabel);

            if (!"NEGLIGIBLE".equals(riskLabel)) {
                allRisksNegligible = false;
            }
            if ("SIGNIFICANT".equals(riskLabel) || "MAJOR".equals(riskLabel)) {
                anySignificantRiskDetected = true;
            }

            List<Double> typeImpactScores = impactScoresByType.get(type);
            double avgImpact = MetricUtils.calculateAverageImpact(typeImpactScores);
            impactNode.put(type, Double.valueOf(String.format("%.2f", avgImpact)));

            if (avgImpact >= IMPACT_THRESHOLD) {
                allImpactsAreLow = false;
            }
        }

        String stabilityResult = "PARTIALLY STABLE";

        if (activeTickets.isEmpty()) {
            stabilityResult = "STABLE";
        } else if (allRisksNegligible && allImpactsAreLow) {
            stabilityResult = "STABLE";
        } else if (anySignificantRiskDetected) {
            stabilityResult = "UNSTABLE";
        }

        reportNode.put("appStability", stabilityResult);

        if ("STABLE".equals(stabilityResult)) {
             CommandList.getInstance().requestStop();
        }

        return output;
    }
}
