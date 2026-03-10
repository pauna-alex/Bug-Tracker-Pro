package main.tickets;

import main.DatabaseTickets;
import main.metrics.TicketVisitor;

public final class Bug extends Ticket {

    private String expectedBehavior;
    private String actualBehavior;
    private String frequency;
    private String severity;
    private String environment;
    private int errorCode;

    /**
     * @param type
     * @param title
     * @param businessPriority
     * @param status
     * @param expertiseArea
     * @param description
     * @param reportedBy
     * @param expectedBehavior
     * @param actualBehavior
     * @param frequency
     * @param severity
     * @param environment
     * @param errorCode
     */
    public Bug(final String type, final String title, final String businessPriority,
               final String status, final String expertiseArea, final String description,
               final String reportedBy, final String expectedBehavior, final String actualBehavior,
               final String frequency, final String severity, final String environment,
               final int errorCode) {
        super(type, title, businessPriority, status, expertiseArea, description, reportedBy);
        this.expectedBehavior = expectedBehavior;
        this.actualBehavior = actualBehavior;
        this.frequency = frequency;
        this.severity = severity;
        this.environment = environment;
        this.errorCode = errorCode;
    }

    /**
     * Seteaza starea initiala si inregistreaza bug-ul in baza de date.
     * @param output
     * @param timeStamp
     */
    @Override
    public void reportTicket(final com.fasterxml.jackson.databind.node.ObjectNode output,
                             final String timeStamp) {
        if (this.getReportedBy() == null || this.getReportedBy().isEmpty()) {
            this.setBusinessPriority("LOW");
        }
        this.setStatus("OPEN");
        this.setCreatedAt(timeStamp);
        DatabaseTickets.getInstance().addTicket(this);
    }

    /**
     * Creste prioritatea bug-ului conform ierarhiei.
     */
    @Override
    public void increasePriority() {
        String p = super.getBusinessPriority();
        if ("LOW".equals(p)) {
            this.setBusinessPriority("MEDIUM");
        } else if ("MEDIUM".equals(p)) {
            this.setBusinessPriority("HIGH");
        } else if ("HIGH".equals(p)) {
            this.setBusinessPriority("CRITICAL");
        }
    }

    /**
     * @return
     */
    @Override
    public boolean isAccessibleByJunior() {
        return true;
    }

    /**
     * @return
     */
    @Override
    public String getType() {
        return "BUG";
    }

    /**
     * @return
     */
    public String getFrequency() {
        return this.frequency;
    }

    /**
     * @return
     */
    public String getSeverity() {
        return this.severity;
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
