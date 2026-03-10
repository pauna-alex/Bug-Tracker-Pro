package main.metrics;

import java.util.List;

import main.tickets.Ticket;

/**
 * Strategie de calcul a performantei destinata dezvoltatorilor de nivel Junior.
 * Scorul se bazeaza pe volumul de tichete inchise, influentat de factorul de
 * diversitate al tipurilor de sarcini abordate.
 */
public final class JuniorPerformanceStrategy implements PerformanceStrategy {

    private static final double JUNIOR_BONUS = 5.0;
    private static final double CLOSED_WEIGHT = 0.5;
    private static final double MIN_SCORE = 0.0;

    /**
     * Calculeaza scorul de performanta pentru tichetele inchise de un junior.
     *
     * @param closedTickets lista de tichete finalizate in perioada de raportare
     * @return scorul final calculat, incluzand bonusul specific nivelului
     */
    @Override
    public double calculateScore(final List<Ticket> closedTickets) {
        int numarTicheteInchise = closedTickets.size();
        if (numarTicheteInchise == 0) {
            return MIN_SCORE;
        }

        int numarBuguri = 0;
        int numarFunctionalitati = 0;
        int numarFeedbackUi = 0;

        for (Ticket tichet : closedTickets) {
            String tip = tichet.getType();

            if ("BUG".equals(tip)) {
                numarBuguri++;
            } else if ("FEATURE_REQUEST".equals(tip)) {
                numarFunctionalitati++;
            } else if ("UI_FEEDBACK".equals(tip)) {
                numarFeedbackUi++;
            }
        }

        double factorDiversitate = MetricUtils.ticketDiversityFactor(numarBuguri,
                                   numarFunctionalitati, numarFeedbackUi);

        double scorBaza = (CLOSED_WEIGHT * numarTicheteInchise) - factorDiversitate;

        return Math.max(MIN_SCORE, scorBaza) + JUNIOR_BONUS;
    }
}

