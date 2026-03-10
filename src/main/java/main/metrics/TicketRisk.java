package main.metrics;

import main.tickets.Bug;
import main.tickets.FeatureRequest;
import main.tickets.Ticket;
import main.tickets.UiFeedback;

/**
 * Implementare a strategiei de calcul pentru riscul asociat unui tichet.
 * Utilizeaza pattern-ul Visitor pentru a aplica formule specifice fiecarui tip de tichet.
 */
public final class TicketRisk implements MetricStrategy, TicketVisitor {

    private static final double MAX_BUG_RISK = 12.0;
    private static final double MAX_FEATURE_RISK = 20.0;
    private static final double MAX_UI_RISK = 100.0;
    private static final double PERCENTAGE_FACTOR = 100.0;
    private static final int UI_BASE_SCORE = 11;

    /**
     * Calculeaza scorul de risc pentru un tichet general prin mecanismul de double dispatch.
     *
     * @param ticket tichetul pentru care se calculeaza riscul
     * @return valoarea riscului sub forma de procent (0-100)
     */
    @Override
    public double calculate(final Ticket ticket) {
        return ticket.accept(this);
    }

    /**
     * Calculeaza riscul pentru un Bug pe baza frecventei si severitatii.
     *
     * @param bug obiectul de tip Bug vizitat
     * @return scorul de risc calculat procentual
     */
    @Override
    public double visit(final Bug bug) {
        double freq = MetricUtils.getFrequencyValue(bug.getFrequency());
        double sev = MetricUtils.getSeverityValue(bug.getSeverity());

        double rawRisk = freq * sev;

        return (rawRisk * PERCENTAGE_FACTOR) / MAX_BUG_RISK;
    }

    /**
     * Calculeaza riscul pentru o cerere de functionalitate noua (FeatureRequest).
     *
     * @final feature obiectul de tip FeatureRequest vizitat
     * @return scorul de risc calculat procentual
     */
    @Override
    public double visit(final FeatureRequest feature) {
        double biz = MetricUtils.getBusinessValue(feature.getBusinessValue());
        double demand = MetricUtils.getCustomerDemandValue(feature.getCustomerDemand());

        double rawRisk = biz + demand;

        return (rawRisk * PERCENTAGE_FACTOR) / MAX_FEATURE_RISK;
    }

    /**
     * Calculeaza riscul pentru un feedback de interfata (UiFeedback).
     *
     * @param ui obiectul de tip UiFeedback vizitat
     * @return scorul de risc calculat procentual
     */
    @Override
    public double visit(final UiFeedback ui) {
        double biz = MetricUtils.getBusinessValue(ui.getBusinessValue());
        int usability = ui.getUsabilityScore();

        double rawRisk = (UI_BASE_SCORE - usability) * biz;

        return (rawRisk * PERCENTAGE_FACTOR) / MAX_UI_RISK;
    }
}
