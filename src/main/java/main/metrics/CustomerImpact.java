package main.metrics;

import main.tickets.Bug;
import main.tickets.FeatureRequest;
import main.tickets.Ticket;
import main.tickets.UiFeedback;

/**
 * Strategie pentru evaluarea efectului produs asupra clientului.
 * Foloseste un mecanism de vizitare pentru a alege formula de calcul
 * potrivita fiecarui tip de tichet in parte.
 */
public final class CustomerImpact implements MetricStrategy, TicketVisitor {

    private static final double FACTOR_PROCENTUAL = 100.0;
    private static final double SCOR_MAXIM_BUG = 48.0;

    /**
     * Calculeaza impactul prin redirectionarea catre metoda specifica tipului.
     *
     * @param ticket tichetul evaluat
     * @return valoarea impactului
     */
    @Override
    public double calculate(final Ticket ticket) {
        return ticket.accept(this);
    }

    /**
     * Evalueaza un Bug in functie de cat de des apare, importanta si gravitate.
     *
     * @param bug eroarea analizata
     * @return scorul transformat in procente
     */
    @Override
    public double visit(final Bug bug) {
        double frecventa = MetricUtils.getFrequencyValue(bug.getFrequency());
        double prioritate = MetricUtils.getPriorityValue(bug.getBusinessPriority());
        double severitate = MetricUtils.getSeverityValue(bug.getSeverity());

        return (frecventa * prioritate * severitate * FACTOR_PROCENTUAL) / SCOR_MAXIM_BUG;
    }

    /**
     * Evalueaza o cerere de functionalitate noua.
     *
     * @param feature functionalitatea propusa
     * @return impactul bazat pe importanta si cerere
     */
    @Override
    public double visit(final FeatureRequest feature) {
        double business = MetricUtils.getBusinessValue(feature.getBusinessValue());
        double cerere = MetricUtils.getCustomerDemandValue(feature.getCustomerDemand());

        return business * cerere;
    }

    /**
     * Evalueaza un feedback legat de interfata.
     *
     * @param ui feedback-ul primit
     * @return impactul bazat pe importanta si usurinta in utilizare
     */
    @Override
    public double visit(final UiFeedback ui) {
        double business = MetricUtils.getBusinessValue(ui.getBusinessValue());
        double uzabilitate = ui.getUsabilityScore();

        return business * uzabilitate;
    }
}
