package main.updates.rules;

import main.DatabaseTickets;
import main.milestone.Milestone;
import main.tickets.Ticket;
import main.updates.UpdateRule;

public final class SyncTicketsStateWithMilestone implements UpdateRule<Milestone> {

    /**
     * Sincronizeaza listele de tichete deschise/inchise si actualizeaza statusul milestone-ului.
     * @param milestone
     * @param currentTimestamp
     */
    @Override
    public void apply(final Milestone milestone, final String currentTimestamp) {
        milestone.getOpentickets().clear();
        milestone.getClosedtickets().clear();
        boolean allClosed = true;

        if (milestone.getTickets().isEmpty()) {
            milestone.setStatus("ACTIVE");
            return;
        }

        for (int idx : milestone.getTickets()) {
            Ticket t = DatabaseTickets.getInstance().getTicketByID(idx);
            if (t != null) {
                if ("CLOSED".equals(t.getStatus())) {
                    milestone.getClosedtickets().add(idx);
                } else {
                    milestone.getOpentickets().add(idx);
                    allClosed = false;
                }
            }
        }
        milestone.setStatus(allClosed ? "COMPLETED" : "ACTIVE");
    }
}
