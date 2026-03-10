package main.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import main.DatabaseTickets;
import main.metrics.CustomerImpact;
import main.metrics.MetricStrategy;
import main.metrics.MetricUtils;
import main.tickets.Ticket;

/**
 * Comanda pentru generarea raportului privind impactul asupra clientilor.
 * Calculeaza distributia tichetelor si media scorului de impact pentru fiecare tip.
 */
public final class GenerateCustomerImpactReport extends Command implements Exe {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructor pentru comanda GenerateCustomerImpactReport.
     *
     * @param command numele comenzii
     * @param username numele utilizatorului care solicita raportul
     * @param timestamp momentul in care este generat raportul
     */
    public GenerateCustomerImpactReport(final String command, final String username,
                                        final String timestamp) {
        super(command, username, timestamp);
    }

    /**
     * Executa logica de colectare a datelor si calculare a metricilor de impact.
     *
     * @return un ObjectNode ce contine raportul de impact detaliat
     */
    @Override
    public ObjectNode execute() {
        ObjectNode rezultat = mapper.createObjectNode();
        MetricStrategy strategieImpact = new CustomerImpact();

        Map<String, Integer> ticheteDupaTip = initializeazaTicheteDupaTip();
        Map<String, Integer> ticheteDupaPrioritate = initializeazaTicheteDupaPrioritate();
        Map<String, List<Double>> scoruriDupaTip = initializeazaScoruriDupaTip();

        List<Ticket> ticheteActive = DatabaseTickets.getInstance().getOpenInProgressTickets();

        for (Ticket tichet : ticheteActive) {
            String tip = tichet.getType();
            String prioritate = tichet.getBusinessPriority();

            if (ticheteDupaTip.containsKey(tip)) {
                ticheteDupaTip.put(tip, ticheteDupaTip.get(tip) + 1);
            }

            if (prioritate != null && ticheteDupaPrioritate.containsKey(prioritate)) {
                ticheteDupaPrioritate.put(prioritate, ticheteDupaPrioritate.get(prioritate) + 1);
            }

            double valoareImpact = strategieImpact.calculate(tichet);

            if (scoruriDupaTip.containsKey(tip)) {
                scoruriDupaTip.get(tip).add(valoareImpact);
            }
        }

        return construiesteIesireJson(rezultat, ticheteActive.size(), ticheteDupaTip,
                                      ticheteDupaPrioritate, scoruriDupaTip);
    }

    /**
     * Initializeaza map-ul pentru numararea tichetelor in functie de tip.
     *
     * @return map-ul initializat cu valorile 0
     */
    private Map<String, Integer> initializeazaTicheteDupaTip() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("BUG", 0);
        map.put("FEATURE_REQUEST", 0);
        map.put("UI_FEEDBACK", 0);
        return map;
    }

    /**
     * Initializeaza map-ul pentru numararea tichetelor in functie de prioritate.
     *
     * @return map-ul initializat cu valorile 0
     */
    private Map<String, Integer> initializeazaTicheteDupaPrioritate() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("LOW", 0);
        map.put("MEDIUM", 0);
        map.put("HIGH", 0);
        map.put("CRITICAL", 0);
        return map;
    }

    /**
     * Pregateste listele de scoruri pentru fiecare tip de tichet.
     *
     * @return map care contine liste goale pentru fiecare tip
     */
    private Map<String, List<Double>> initializeazaScoruriDupaTip() {
        Map<String, List<Double>> map = new LinkedHashMap<>();
        map.put("BUG", new ArrayList<>());
        map.put("FEATURE_REQUEST", new ArrayList<>());
        map.put("UI_FEEDBACK", new ArrayList<>());
        return map;
    }

    /**
     * Construieste structura JSON finala a raportului.
     *
     * @param nod obiectul JSON de baza
     * @param total numarul total de tichete procesate
     * @param tipuri datele grupate dupa tip
     * @param prioritati datele grupate dupa prioritate
     * @param scoruri listele de scoruri pentru calcularea mediei
     * @return ObjectNode-ul completat cu toate datele raportului
     */
    private ObjectNode construiesteIesireJson(final ObjectNode nod, final int total,
                                              final Map<String, Integer> tipuri,
                                              final Map<String, Integer> prioritati,
                                              final Map<String, List<Double>> scoruri) {
        nod.put("command", getCommandType());
        nod.put("username", getUsername());
        nod.put("timestamp", getTimeStamp());

        ObjectNode raport = nod.putObject("report");
        raport.put("totalTickets", total);

        ObjectNode nodTip = raport.putObject("ticketsByType");
        tipuri.forEach(nodTip::put);

        ObjectNode nodPrioritate = raport.putObject("ticketsByPriority");
        prioritati.forEach(nodPrioritate::put);

        ObjectNode nodImpact = raport.putObject("customerImpactByType");
        for (String tip : scoruri.keySet()) {
            double medie = MetricUtils.calculateAverageImpact(scoruri.get(tip));
            nodImpact.put(tip, Double.valueOf(String.format("%.2f", medie)));
        }

        return nod;
    }
}
