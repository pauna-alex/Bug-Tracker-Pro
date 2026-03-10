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
import main.metrics.TicketRisk;
import main.tickets.Ticket;
import main.user.User;

/**
 * Comanda responsabila pentru generarea raportului de risc al tichetelor.
 * Analizeaza tichetele active si calculeaza nivelul de risc mediu pe categorii.
 */
public final class GenerateTicketRiskReport extends Command implements Exe {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructor pentru comanda GenerateTicketRiskReport.
     *
     * @param command numele comenzii
     * @param username numele utilizatorului care solicita raportul
     * @param timestamp momentul generarii raportului
     */
    public GenerateTicketRiskReport(final String command, final String username,
                                    final String timestamp) {
        super(command, username, timestamp);
    }

    /**
     * Executa procesul de analiza a riscului pentru tichetele in lucru.
     *
     * @return un ObjectNode care contine distributia si calificativele de risc
     */
    @Override
    public ObjectNode execute() {
        ObjectNode rezultatFinal = mapper.createObjectNode();
        rezultatFinal.put("command", super.getCommandType());
        rezultatFinal.put("username", super.getUsername());
        rezultatFinal.put("timestamp", super.getTimeStamp());

        User user = main.DatabaseUser.getInstance().getUserByUsername(super.getUsername());

        if (!user.canGenerateReports()) {
            rezultatFinal.put("error", "The user does not have permission to execute "
                    + "this command: required role user.");
            return rezultatFinal;
        }

        Map<String, Integer> tichetePeTipuri = initializeazaContorTipuri();
        Map<String, Integer> tichetePePrioritati = initializeazaContorPrioritati();
        Map<String, List<Double>> scoruriRiscPeTipuri = initializeazaListeScoruri();

        List<Ticket> ticheteActive = DatabaseTickets.getInstance().getOpenInProgressTickets();
        MetricStrategy strategieRisc = new TicketRisk();

        for (Ticket tichet : ticheteActive) {
            actualizeazaStatisticiRisc(tichet, tichetePeTipuri, tichetePePrioritati,
                                      scoruriRiscPeTipuri, strategieRisc);
        }

        return asambleazaRaportFinal(rezultatFinal, ticheteActive.size(), tichetePeTipuri,
                                     tichetePePrioritati, scoruriRiscPeTipuri);
    }

    /**
     * Actualizeaza colectiile de date cu informatiile extrase dintr-un tichet.
     *
     * @param tichet tichetul procesat
     * @param tipuri map pentru numararea tipurilor
     * @param prioritati map pentru numararea prioritatilor
     * @param scoruri map pentru colectarea valorilor de risc
     * @param strategie strategia de calcul pentru risc
     */
    private void actualizeazaStatisticiRisc(final Ticket tichet,
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

        double valoareRisc = strategie.calculate(tichet);
        if (scoruri.containsKey(tip)) {
            scoruri.get(tip).add(valoareRisc);
        }
    }

    /**
     * Pregateste map-ul pentru categoriile de tichete.
     *
     * @return map initializat cu BUG, FEATURE_REQUEST si UI_FEEDBACK
     */
    private Map<String, Integer> initializeazaContorTipuri() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("BUG", 0);
        map.put("FEATURE_REQUEST", 0);
        map.put("UI_FEEDBACK", 0);
        return map;
    }

    /**
     * Pregateste map-ul pentru nivelurile de prioritate.
     *
     * @return map initializat cu LOW, MEDIUM, HIGH si CRITICAL
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
     * Pregateste listele pentru colectarea scorurilor brute de risc.
     *
     * @return map cu liste goale pentru fiecare tip de tichet
     */
    private Map<String, List<Double>> initializeazaListeScoruri() {
        Map<String, List<Double>> map = new LinkedHashMap<>();
        map.put("BUG", new ArrayList<>());
        map.put("FEATURE_REQUEST", new ArrayList<>());
        map.put("UI_FEEDBACK", new ArrayList<>());
        return map;
    }

    /**
     * Construieste reprezentarea JSON a raportului de risc.
     *
     * @param nod nodul JSON principal
     * @param total numarul de tichete active
     * @param tipuri datele grupate pe tip
     * @param prioritati datele grupate pe prioritate
     * @param scoruri datele de risc colectate
     * @return ObjectNode-ul completat
     */
    private ObjectNode asambleazaRaportFinal(final ObjectNode nod, final int total,
                                             final Map<String, Integer> tipuri,
                                             final Map<String, Integer> prioritati,
                                             final Map<String, List<Double>> scoruri) {
        ObjectNode raport = nod.putObject("report");
        raport.put("totalTickets", total);

        ObjectNode nodTipuri = raport.putObject("ticketsByType");
        tipuri.forEach(nodTipuri::put);

        ObjectNode nodPrioritati = raport.putObject("ticketsByPriority");
        prioritati.forEach(nodPrioritati::put);

        ObjectNode nodRisc = raport.putObject("riskByType");
        for (String tip : scoruri.keySet()) {
            double medie = MetricUtils.calculateAverageImpact(scoruri.get(tip));
            String calificativ = MetricUtils.getRiskLabel(medie);
            nodRisc.put(tip, calificativ);
        }

        return nod;
    }
}
