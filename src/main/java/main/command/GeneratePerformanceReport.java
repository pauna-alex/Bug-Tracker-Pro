package main.command;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import main.DatabaseTickets;
import main.DatabaseUser;
import main.metrics.JuniorPerformanceStrategy;
import main.metrics.MidPerformanceStrategy;
import main.metrics.PerformanceStrategy;
import main.metrics.SeniorPerformanceStrategy;
import main.tickets.Ticket;
import main.user.User;

/**
 * Comanda responsabila pentru generarea raportului de performanta al subordonatilor.
 * Analizeaza activitatea din luna precedenta pentru fiecare dezvoltator eligibil.
 */
public final class GeneratePerformanceReport extends Command implements Exe {

    private final ObjectMapper mapper = new ObjectMapper();
    private static final double MIN_DAYS = 1.0;

    /**
     * Constructor pentru comanda GeneratePerformanceReport.
     *
     * @param command numele comenzii
     * @param username numele utilizatorului care solicita raportul
     * @param timestamp momentul in care este solicitata generarea
     */
    public GeneratePerformanceReport(final String command, final String username,
                                     final String timestamp) {
        super(command, username, timestamp);
    }

    /**
     * Executa procesul de calcul al performantei pentru toti subordonatii.
     *
     * @return un ObjectNode ce contine lista de performanta a dezvoltatorilor
     */
    @Override
    public ObjectNode execute() {
        ObjectNode rezultatFinal = mapper.createObjectNode();

        rezultatFinal.put("command", super.getCommandType());
        rezultatFinal.put("username", super.getUsername());
        rezultatFinal.put("timestamp", super.getTimeStamp());

        User manager = DatabaseUser.getInstance().getUserByUsername(super.getUsername());

        if (!manager.canGeneratePerformanceReport()) {
            return rezultatFinal;
        }

        LocalDate dataComanda = LocalDate.parse(super.getTimeStamp());
        LocalDate lunaTinta = dataComanda.minusMonths(1);
        int lunaCautata = lunaTinta.getMonthValue();
        int anCautat = lunaTinta.getYear();

        List<User> subordonatiEligibili = gasesteSubordonatiRaportabili(manager);
        Collections.sort(subordonatiEligibili, Comparator.comparing(User::getUsername));

        ArrayNode nodRaport = rezultatFinal.putArray("report");

        for (User dezvoltator : subordonatiEligibili) {
            List<Ticket> ticheteRezolvate = colecteazaTicheteInchise(dezvoltator,
                                                                    lunaCautata, anCautat);

            int numarTichete = ticheteRezolvate.size();
            double timpMediuRezolutie = 0.0;
            double scorPerformanta = 0.0;

            if (numarTichete > 0) {
                double totalZile = 0;
                for (Ticket t : ticheteRezolvate) {
                    totalZile += calculeazaZileLucrate(t);
                }
                timpMediuRezolutie = totalZile / numarTichete;

                PerformanceStrategy strategie = determinaStrategia(dezvoltator.getSeniority());
                scorPerformanta = strategie.calculateScore(ticheteRezolvate);
            }

            dezvoltator.updatePerformanceScore(scorPerformanta);
            nodRaport.add(creeazaNodDezvoltator(dezvoltator, numarTichete,
                                                timpMediuRezolutie, scorPerformanta));
        }

        return rezultatFinal;
    }

    /**
     * Identifica toti subordonatii unui manager care sunt eligibili pentru raportare.
     *
     * @param manager managerul pentru care se cauta subordonatii
     * @return o lista de obiecte User
     */
    private List<User> gasesteSubordonatiRaportabili(final User manager) {
        List<User> rezultat = new ArrayList<>();
        List<String> numeSubordonati = manager.getSubordinateUsernames();
        if (numeSubordonati != null) {
            for (String nume : numeSubordonati) {
                User sub = DatabaseUser.getInstance().getUserByUsername(nume);
                if (sub != null && sub.isReportableDeveloper()) {
                    rezultat.add(sub);
                }
            }
        }
        return rezultat;
    }

    /**
     * Colecteaza toate tichetele inchise de un dezvoltator intr-o anumita luna si an.
     *
     * @param dev dezvoltatorul vizat
     * @param luna luna calendaristica
     * @param an anul calendaristic
     * @return lista de tichete care indeplinesc criteriile
     */
    private List<Ticket> colecteazaTicheteInchise(final User dev, final int luna, final int an) {
        List<Ticket> filtrate = new ArrayList<>();
        for (Ticket t : DatabaseTickets.getInstance().getTickets()) {
            if (dev.getUsername().equals(t.getAssignedTo())
                && "CLOSED".equalsIgnoreCase(t.getStatus())) {
                String dataInchidere = t.getSolvedAt();
                if (dataInchidere != null && !dataInchidere.isEmpty()) {
                    try {
                        LocalDate dataParsata = LocalDate.parse(dataInchidere);
                        if (dataParsata.getMonthValue() == luna && dataParsata.getYear() == an) {
                            filtrate.add(t);
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
            }
        }
        return filtrate;
    }

    /**
     * Calculeaza numarul de zile scurse intre alocarea si rezolvarea unui tichet.
     *
     * @param t tichetul pentru care se face calculul
     * @return numarul de zile (minim 1.0)
     */
    private double calculeazaZileLucrate(final Ticket t) {
        try {
            LocalDate inceput = LocalDate.parse(t.getAssignedAt());
            LocalDate sfarsit = LocalDate.parse(t.getSolvedAt());
            long diferenta = ChronoUnit.DAYS.between(inceput, sfarsit) + 1;
            return Math.max(MIN_DAYS, (double) diferenta);
        } catch (Exception e) {
            return MIN_DAYS;
        }
    }

    /**
     * Determina strategia de performanta bazata pe nivelul de experienta.
     *
     * @param seniority nivelul de experienta (JUNIOR, MID, SENIOR)
     * @return obiectul de tip PerformanceStrategy corespunzator
     */
    private PerformanceStrategy determinaStrategia(final String seniority) {
        if (seniority == null) {
            return new JuniorPerformanceStrategy();
        }
        return switch (seniority.toUpperCase()) {
            case "MID" -> new MidPerformanceStrategy();
            case "SENIOR" -> new SeniorPerformanceStrategy();
            default -> new JuniorPerformanceStrategy();
        };
    }

    /**
     * Construieste nodul JSON detaliat pentru un singur dezvoltator.
     *
     * @param dev obiectul utilizator
     * @param numar tichete rezolvate
     * @param timp timp mediu de rezolvare
     * @param scor scorul final de performanta
     * @return un ObjectNode populat cu date
     */
    private ObjectNode creeazaNodDezvoltator(final User dev, final int numar,
                                             final double timp, final double scor) {
        ObjectNode nod = mapper.createObjectNode();
        nod.put("username", dev.getUsername());
        nod.put("closedTickets", numar);
        nod.put("averageResolutionTime", Double.valueOf(String.format("%.2f", timp)));
        nod.put("performanceScore", Double.valueOf(String.format("%.2f", scor)));
        nod.put("seniority", dev.getSeniority() != null ? dev.getSeniority() : "JUNIOR");
        return nod;
    }
}
