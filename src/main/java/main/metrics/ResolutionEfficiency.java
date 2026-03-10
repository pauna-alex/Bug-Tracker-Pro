package main.metrics;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import main.tickets.Bug;
import main.tickets.FeatureRequest;
import main.tickets.Ticket;
import main.tickets.UiFeedback;

/**
 * Strategie de calcul pentru eficienta rezolvarii tichetelor.
 * Utilizeaza pattern-ul Visitor pentru a aplica formule de calcul specifice
 * fiecarui tip de tichet in functie de durata de rezolutie si prioritate.
 */
public final class ResolutionEfficiency implements MetricStrategy, TicketVisitor {

    private static final double DEFAULT_DAYS = 1.0;
    private static final double PERCENTAGE_FACTOR = 100.0;
    private static final double BUG_MULTIPLIER = 10.0;
    private static final double MAX_SCORE_BUG = 70.0;
    private static final double MAX_SCORE_FEATURE_UI = 20.0;

    /**
     * Calculeaza eficienta pentru un tichet prin mecanismul de dispatch polimorfic.
     *
     * @param ticket tichetul pentru care se doreste calculul
     * @return valoarea eficientei calculata procentual
     */
    @Override
    public double calculate(final Ticket ticket) {
        return ticket.accept(this);
    }

    /**
     * Calculeaza eficienta pentru un Bug.
     * Formula: ((frecventa + severitate) * 10) / zileRezolutie.
     *
     * @param bug obiectul de tip Bug vizitat
     * @return scorul de eficienta raportat la valoarea maxima (0-100)
     */
    @Override
    public double visit(final Bug bug) {
        double frecventa = MetricUtils.getFrequencyValue(bug.getFrequency());
        double severitate = MetricUtils.getSeverityValue(bug.getSeverity());
        double zile = getDaysToResolve(bug);

        double scorEficienta = ((frecventa + severitate) * BUG_MULTIPLIER) / zile;

        return (scorEficienta * PERCENTAGE_FACTOR) / MAX_SCORE_BUG;
    }

    /**
     * Calculeaza eficienta pentru o cerere de functionalitate (FeatureRequest).
     * Formula: (valoareBusiness + cerereClient) / zileRezolutie.
     *
     * @param feature obiectul de tip FeatureRequest vizitat
     * @return scorul de eficienta raportat la valoarea maxima (0-100)
     */
    @Override
    public double visit(final FeatureRequest feature) {
        double business = MetricUtils.getBusinessValue(feature.getBusinessValue());
        double cerere = MetricUtils.getCustomerDemandValue(feature.getCustomerDemand());
        double zile = getDaysToResolve(feature);

        double scorEficienta = (business + cerere) / zile;

        return (scorEficienta * PERCENTAGE_FACTOR) / MAX_SCORE_FEATURE_UI;
    }

    /**
     * Calculeaza eficienta pentru un feedback de interfata (UiFeedback).
     * Formula: (scorUsability + valoareBusiness) / zileRezolutie.
     *
     * @param ui obiectul de tip UiFeedback vizitat
     * @return scorul de eficienta raportat la valoarea maxima (0-100)
     */
    @Override
    public double visit(final UiFeedback ui) {
        double business = MetricUtils.getBusinessValue(ui.getBusinessValue());
        double usability = ui.getUsabilityScore();
        double zile = getDaysToResolve(ui);

        double scorEficienta = (usability + business) / zile;

        return (scorEficienta * PERCENTAGE_FACTOR) / MAX_SCORE_FEATURE_UI;
    }

    /**
     * Calculeaza numarul de zile scurse intre data alocarii si data rezolvarii.
     * Calculul este incluziv (+1 zi).
     *
     * @param ticket tichetul analizat
     * @return numarul de zile (minim 1.0)
     */
    private double getDaysToResolve(final Ticket ticket) {
        try {
            String dataAlocareStr = ticket.getAssignedAt();
            String dataRezolvareStr = ticket.getSolvedAt();

            if (dataAlocareStr == null || dataAlocareStr.isEmpty()
                    || dataRezolvareStr == null || dataRezolvareStr.isEmpty()) {
                return DEFAULT_DAYS;
            }

            LocalDate dataAlocare = LocalDate.parse(dataAlocareStr);
            LocalDate dataRezolvare = LocalDate.parse(dataRezolvareStr);

            long zileDiferenta = ChronoUnit.DAYS.between(dataAlocare, dataRezolvare) + 1;

            return Math.max(DEFAULT_DAYS, (double) zileDiferenta);

        } catch (Exception e) {
            return DEFAULT_DAYS;
        }
    }
}
