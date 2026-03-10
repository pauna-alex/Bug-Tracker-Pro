package main.tickets;

import main.DatabaseTickets;
import main.metrics.TicketVisitor;

public final class UiFeedback extends Ticket {

    private String uiElementId;
    private String businessValue;
    private int usabilityScore;
    private String screenshotUrl;
    private String suggestedFix;

    /**
     * @param type
     * @param title
     * @param businessPriority
     * @param status
     * @param expertiseArea
     * @param description
     * @param reportedBy
     * @param uiElementId
     * @param businessValue
     * @param usabilityScore
     * @param screenshotUrl
     * @param suggestedFix
     */
    public UiFeedback(final String type, final String title, final String businessPriority,
                      final String status, final String expertiseArea, final String description,
                      final String reportedBy, final String uiElementId, final String businessValue,
                      final int usabilityScore, final String screenshotUrl,
                      final String suggestedFix) {
        super(type, title, businessPriority, status, expertiseArea, description, reportedBy);
        this.uiElementId = uiElementId;
        this.businessValue = businessValue;
        this.usabilityScore = usabilityScore;
        this.screenshotUrl = screenshotUrl;
        this.suggestedFix = suggestedFix;
    }

    /**
     * @param output
     * @param timeStamp
     */
    @Override
    public void reportTicket(final com.fasterxml.jackson.databind.node.ObjectNode output,
                             final String timeStamp) {
        if (super.getReportedBy().isEmpty()) {
            output.put("error", "Anonymous reports are only allowed for tickets of type BUG.");
        } else {
            this.setCreatedAt(timeStamp);
            this.setStatus("OPEN");
            DatabaseTickets.getInstance().addTicket(this);
        }
    }

    /**
     * Creste prioritatea tichetului ierarhic.
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
        return "UI_FEEDBACK";
    }

    /**
     * @return
     */
    public String getBusinessValue() {
        return this.businessValue;
    }

    /**
     * @return
     */
    public int getUsabilityScore() {
        return this.usabilityScore;
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
