package main.metrics;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import main.tickets.Ticket;

/**
 * Strategie de calcul a performantei pentru dezvoltatorii de nivel Mid.
 * Aceasta foloseste o formula echilibrata intre numarul de tichete rezolvate,
 * prioritatea acestora si timpul mediu de raspuns.
 */
public final class MidPerformanceStrategy implements PerformanceStrategy {

    private static final double BONUS_MID = 15.0;
    private static final double PONDERE_INCHISE = 0.5;
    private static final double PONDERE_PRIORITATE = 0.7;
    private static final double PENALIZARE_TIMP = 0.3;
    private static final double ZILE_MINIME = 1.0;

    /**
     * Calculeaza scorul de performanta pentru un set de tichete inchise.
     *
     * @param closedTickets lista de tichete finalizate de dezvoltator
     * @return scorul final de performanta incluzand bonusul de senioritate
     */
    @Override
    public double calculateScore(final List<Ticket> closedTickets) {
        int numarTicheteInchise = closedTickets.size();
        if (numarTicheteInchise == 0) {
            return 0.0;
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

        double scorBaza = (PONDERE_INCHISE * numarTicheteInchise)
                        + (PONDERE_PRIORITATE * tichetePrioritare)
                        - (PENALIZARE_TIMP * timpMediu);

        return Math.max(0.0, scorBaza) + BONUS_MID;
    }

    /**
     * Calculeaza durata in zile intre momentul asignarii si cel al rezolvarii.
     *
     * @param tichet tichetul analizat
     * @return numarul de zile scurse (minim 1.0)
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
