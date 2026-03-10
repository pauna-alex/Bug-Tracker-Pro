package main.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import main.DatabaseTickets;
import main.metrics.MetricStrategy;
import main.metrics.MetricUtils;
import main.metrics.ResolutionEfficiency;
import main.tickets.Ticket;
import main.user.User;

/**
 * Comanda pentru generarea raportului de eficienta a rezolvarii tichetelor.
 * Analizeaza tichetele finalizate si calculeaza scorul de eficienta pe tipuri si prioritati.
 */
public final class GenerateResolutionEfficiencyReport extends Command implements Exe {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructor pentru comanda GenerateResolutionEfficiencyReport.
     *
     * @param command numele comenzii
     * @param username numele utilizatorului care solicita raportul
     * @param timestamp momentul generarii raportului
     */
    public GenerateResolutionEfficiencyReport(final String command, final String username,
                                               final String timestamp) {
        super(command, username, timestamp);
    }

    /**
     * Executa procesul de calculare a eficientei pentru tichetele rezolvate sau inchise.
     *
     * @return un ObjectNode care contine raportul detaliat de eficienta
     */
    @Override
    public ObjectNode execute() {
        ObjectNode rezultatFinal = mapper.createObjectNode();
        rezultatFinal.put("command", super.getCommandType());
        rezultatFinal.put("username", super.getUsername());
        rezultatFinal.put("timestamp", super.getTimeStamp());

        User manager = main.DatabaseUser.getInstance().getUserByUsername(super.getUsername());

        if (!manager.canGenerateReports()) {
            rezultatFinal.put("error", "The user does not have permission to execute "
                    + "this command: required role MANAGER.");
            return rezultatFinal;
        }

        Map<String, Integer> tichetePeTipuri = initializeazaContorTipuri();
        Map<String, Integer> tichetePePrioritati = initializeazaContorPrioritati();
        Map<String, List<Double>> scoruriPeTipuri = initializeazaListeScoruri();

        int numarTotalTichete = 0;
        MetricStrategy strategieEficienta = new ResolutionEfficiency();

        for (Ticket tichet : DatabaseTickets.getInstance().getTickets()) {
            if (esteTichetFinalizat(tichet)) {
                numarTotalTichete++;
                actualizeazaDateRaport(tichet, tichetePeTipuri,
                        tichetePePrioritati, scoruriPeTipuri, strategieEficienta);
            }
        }

        return finalizeazaRaportJson(rezultatFinal, numarTotalTichete, tichetePeTipuri,
                tichetePePrioritati, scoruriPeTipuri);
    }

    /**
     * Verifica daca un tichet are statusul RESOLVED sau CLOSED.
     *
     * @param tichet obiectul tichet verificat
     * @return true daca este finalizat, false altfel
     */
    private boolean esteTichetFinalizat(final Ticket tichet) {
        String status = tichet.getStatus();
        return status != null && (status.equals("RESOLVED") || status.equals("CLOSED"));
    }

    /**
     * Actualizeaza colectiile de date cu informatiile din tichetul curent.
     *
     * @param tichet tichetul procesat
     * @param tipuri map pentru contorizarea tipurilor
     * @param prioritati map pentru contorizarea prioritatilor
     * @param scoruri map pentru colectarea scorurilor de eficienta
     * @param strategie strategia de calcul utilizata
     */
    private void actualizeazaDateRaport(final Ticket tichet,
                                        final Map<String, Integer> tipuri,
                                        final Map<String, Integer> prioritati,
                                        final Map<String, List<Double>> scoruri,
                                        final MetricStrategy strategie) {
        String tip = tichet.getType();
        String prioritate = tichet.getBusinessPriority();

        if (tipuri.containsKey(tip)) {
            tipuri.put(tip, tipuri.get(tip) + 1);
        }
        if (prioritate != null && prioritati.containsKey(prioritate)) {
            prioritati.put(prioritate, prioritati.get(prioritate) + 1);
        }

        double eficienta = strategie.calculate(tichet);
        if (scoruri.containsKey(tip)) {
            scoruri.get(tip).add(eficienta);
        }
    }

    /**
     * Pregateste map-ul pentru tipurile de tichete.
     *
     * @return map initializat
     */
    private Map<String, Integer> initializeazaContorTipuri() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("BUG", 0);
        map.put("FEATURE_REQUEST", 0);
        map.put("UI_FEEDBACK", 0);
        return map;
    }

    /**
     * Pregateste map-ul pentru prioritatile tichetelor.
     *
     * @return map initializat
     */
    private Map<String, Integer> initializeazaContorPrioritati() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("LOW", 0);
        map.put("MEDIUM", 0);
        map.put("HIGH", 0);
        map.put("CRITICAL", 0);
        return map;
    }

    /**
     * Pregateste listele pentru colectarea scorurilor de eficienta.
     *
     * @return map cu liste goale
     */
    private Map<String, List<Double>> initializeazaListeScoruri() {
        Map<String, List<Double>> map = new LinkedHashMap<>();
        map.put("BUG", new ArrayList<>());
        map.put("FEATURE_REQUEST", new ArrayList<>());
        map.put("UI_FEEDBACK", new ArrayList<>());
        return map;
    }

    /**
     * Asambleaza structura JSON finala a raportului.
     *
     * @param nod nodul JSON principal
     * @param total numarul de tichete
     * @param tipuri datele pe tipuri
     * @param prioritati datele pe prioritati
     * @param scoruri listele de scoruri
     * @return ObjectNode-ul completat
     */
    private ObjectNode finalizeazaRaportJson(final ObjectNode nod, final int total,
                                             final Map<String, Integer> tipuri,
                                             final Map<String, Integer> prioritati,
                                             final Map<String, List<Double>> scoruri) {
        ObjectNode raportNode = nod.putObject("report");
        raportNode.put("totalTickets", total);

        ObjectNode tipNode = raportNode.putObject("ticketsByType");
        tipuri.forEach(tipNode::put);

        ObjectNode prioritateNode = raportNode.putObject("ticketsByPriority");
        prioritati.forEach(prioritateNode::put);

        ObjectNode eficientaNode = raportNode.putObject("efficiencyByType");
        for (String tip : scoruri.keySet()) {
            double medie = MetricUtils.calculateAverageImpact(scoruri.get(tip));
            eficientaNode.put(tip, Double.valueOf(String.format("%.2f", medie)));
        }

        return nod;
    }
}
