package main.updates.rules;

import main.tickets.Ticket;
import main.updates.UpdateRule;

public final class UpdatePrioritiesTicketBecauseOfDev implements UpdateRule<Ticket> {

    /**
     * Verifica daca developerul alocat mai are senioritatea necesara pentru tichet.
     * @param ticket
     * @param currentTimestamp
     */
    @Override
    public void apply(final Ticket ticket, final String currentTimestamp) {

        if (ticket.getAssignedTo() != null && !ticket.getAssignedTo().isEmpty()) {
            String devUsername = ticket.getAssignedTo();
            main.user.User u = main.DatabaseUser.getInstance().getUserByUsername(devUsername);

            main.user.Developer dev = (main.user.Developer) u;

            if (!dev.hasSeniorityAccess(dev.getSeniority(), ticket)) {
                ticket.setStatus("OPEN");
                ticket.setAssignedTo(null);

                ticket.setAssignedAt("");

                main.tickets.TicketAction action = new main.tickets.TicketAction(
                        "REMOVED_FROM_DEV", "system", currentTimestamp);
                action.setStatusChange(devUsername, null);
                ticket.addAction(action);
            }
        }
    }
}
