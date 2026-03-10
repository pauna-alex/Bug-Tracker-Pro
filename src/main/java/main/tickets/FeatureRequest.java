package main.tickets;

import main.DatabaseTickets;
import main.metrics.TicketVisitor;

public final class FeatureRequest extends Ticket {

    private final String businessValue;
    private final String customerDemand;

    /**
     * @param type
     * @param title
     * @param businessPriority
     * @param status
     * @param expertiseArea
     * @param description
     * @param reportedBy
     * @param businessValue
     * @param customerDemand
     */
    public FeatureRequest(final String type, final String title, final String businessPriority,
                          final String status, final String expertiseArea,
                          final String description, final String reportedBy,
                          final String businessValue, final String customerDemand) {
        super(type, title, businessPriority, status, expertiseArea, description, reportedBy);
        this.businessValue = businessValue;
        this.customerDemand = customerDemand;
    }

    /** @return type */
    @Override
    public String getType() {
        return "FEATURE_REQUEST";
    }

    /** @return businessValue */
    public String getBusinessValue() {
        return this.businessValue;
    }

    /** @return customerDemand */
    public String getCustomerDemand() {
        return this.customerDemand;
    }

    /**
     * Inregistreaza tichetul daca autorul nu este anonim.
     * @param output
     * @param timeStamp
     */
    @Override
    public void reportTicket(final com.fasterxml.jackson.databind.node.ObjectNode output,
                             final String timeStamp) {
        if (super.getReportedBy().isEmpty()) {
            output.put("error", "Anonymous reports are only allowed for tickets of type BUG.");
        } else {
            this.setStatus("OPEN");
            this.setCreatedAt(timeStamp);
            DatabaseTickets.getInstance().addTicket(this);
        }
    }

    /**
     * Schimba prioritatea tichetului la nivelul urmator.
     */
    @Override
    public void increasePriority() {
        final String p = super.getBusinessPriority();
        if ("LOW".equals(p)) {
            this.setBusinessPriority("MEDIUM");
        } else if ("MEDIUM".equals(p)) {
            this.setBusinessPriority("HIGH");
        } else if ("HIGH".equals(p)) {
            this.setBusinessPriority("CRITICAL");
        }
    }

    /** @return */
    @Override
    public boolean isAccessibleByJunior() {
        return false;
    }

    /**
     * @param visitor
     * @return
     */
    @Override
    public double accept(final TicketVisitor visitor) {
        return visitor.visit(this);
    }
}
