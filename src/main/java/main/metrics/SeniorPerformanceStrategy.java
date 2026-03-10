package main.metrics;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import main.tickets.Ticket;

/**
 * Strategie pentru evaluarea activitatii expertilor cu experienta.
 * Calculul pune accent pe gestionarea urgentelor si eficienta in
 * solutionarea problemelor complexe intr-un timp cat mai scurt.
 */
public final class SeniorPerformanceStrategy implements PerformanceStrategy {

    private static final double BONUS_SENIOR = 30.0;
    private static final double PONDERE_SARCINI_INCHISE = 0.5;
    private static final double PONDERE_URGENTE = 1.0;
    private static final double PENALIZARE_TIMP = 0.5;
    private static final double ZILE_MINIME = 1.0;
    private static final double SCOR_MINIM = 0.0;

    /**
     * Calculeaza punctajul de performanta analizand rezultatele obtinute.
     *
     * @param closedTickets lista sarcinilor finalizate
     * @return punctajul total ce include bonusul de expertiza
     */
    @Override
    public double calculateScore(final List<Ticket> closedTickets) {
        int numarTicheteInchise = closedTickets.size();
        if (numarTicheteInchise == 0) {
            return SCOR_MINIM;
        }

        int tichetePrioritare = 0;
        double timpTotalRezolutie = 0.0;

        for (Ticket tichet : closedTickets) {
            String prioritate = tichet.getBusinessPriority();
            if ("HIGH".equals(prioritate) || "CRITICAL".equals(prioritate)) {
                tichetePrioritare++;
            }
            timpTotalRezolutie += calculeazaZile(tichet);
        }

        double timpMediu = timpTotalRezolutie / numarTicheteInchise;

        double scorBaza = (PONDERE_SARCINI_INCHISE * numarTicheteInchise)
                        + (PONDERE_URGENTE * tichetePrioritare)
                        - (PENALIZARE_TIMP * timpMediu);

        return Math.max(SCOR_MINIM, scorBaza) + BONUS_SENIOR;
    }

    /**
     * Determina durata necesara pentru finalizarea unei sarcini.
     *
     * @param tichet sarcina analizata
     * @return numarul de zile scurse
     */
    private double calculeazaZile(final Ticket tichet) {
        try {
            LocalDate dataInceput = LocalDate.parse(tichet.getAssignedAt());
            LocalDate dataSfarsit = LocalDate.parse(tichet.getSolvedAt());
            return ChronoUnit.DAYS.between(dataInceput, dataSfarsit) + ZILE_MINIME;
        } catch (Exception e) {
            return ZILE_MINIME;
        }
    }
}
