package main.updates.rules;

import java.time.LocalDate;

import main.DatabaseTickets;
import main.milestone.Milestone;
import main.tickets.Ticket;
import main.updates.UpdateRule;

public final class CheckDueTomorrowRule implements UpdateRule<Milestone> {

    /**
     * Verifica daca milestone-ul expira maine si ridica prioritatea tichetelor la CRITICAL.
     * @param milestone
     * @param currentTimestamp
     */
    @Override
    public void apply(final Milestone milestone, final String currentTimestamp) {

        LocalDate current = LocalDate.parse(currentTimestamp);
        LocalDate due = LocalDate.parse(milestone.getDueDate());

    if (current.equals(due.minusDays(1))
        && !milestone.isDueNotificationSent()
        && !milestone.isBlocked()) {

            String msg = "Milestone " + milestone.getName()
                    + " is due tomorrow. All unresolved tickets are now CRITICAL.";

            for (String dev : milestone.getAssignedDevs()) {
                main.DatabaseUser.getInstance().addNotification(dev, msg);
            }

            for (int id : milestone.getOpentickets()) {
                Ticket t = DatabaseTickets.getInstance().getTicketByID(id);
                if (t != null) {
                    t.setBusinessPriority("CRITICAL");
                    UpdatePrioritiesTicketBecauseOfDev rule =
                         new UpdatePrioritiesTicketBecauseOfDev();
                    rule.apply(t, currentTimestamp);
                }
            }

            milestone.setDueNotificationSent(true);
        }
    }
}
