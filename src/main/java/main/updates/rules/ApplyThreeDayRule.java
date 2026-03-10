package main.updates.rules;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import main.DatabaseTickets;
import main.milestone.Milestone;
import main.tickets.Ticket;
import main.updates.UpdateRule;

public final class ApplyThreeDayRule implements UpdateRule<Milestone> {

    private static final int DAYS_INTERVAL = 3;

    /**
     * Aplica regula de crestere a prioritatii la fiecare 3 zile daca milestone-ul nu este blocat.
     * @param milestone
     * @param currentTimestamp
     */
    @Override
    public void apply(final Milestone milestone, final String currentTimestamp) {
        if (!"COMPLETED".equals(milestone.getStatus())) {
            LocalDate current = LocalDate.parse(currentTimestamp);
            LocalDate created = LocalDate.parse(milestone.getCreatedAt());
            String lastDateStr = milestone.getLastUpdateDate() != null
                    ? milestone.getLastUpdateDate() : milestone.getCreatedAt();
            LocalDate lastUpdate = LocalDate.parse(lastDateStr);

            if (current.isBefore(lastUpdate)) {
                return;
            }

            long daysFromStart = ChronoUnit.DAYS.between(created, current);
            long daysFromStartLast = ChronoUnit.DAYS.between(created, lastUpdate);

            long intervalsNow = daysFromStart / DAYS_INTERVAL;
            long intervalsPrev = daysFromStartLast / DAYS_INTERVAL;

            long diff = intervalsNow - intervalsPrev;

            if (diff > 0) {
                if (!milestone.isBlocked()) {
                    for (int i = 0; i < diff; i++) {
                        for (int id : milestone.getTickets()) {
                            Ticket t = DatabaseTickets.getInstance().getTicketByID(id);
                            if (t != null && !"CLOSED".equals(t.getStatus())) {
                                t.increasePriority();
                                UpdatePrioritiesTicketBecauseOfDev priorityRule =
                                        new UpdatePrioritiesTicketBecauseOfDev();
                                priorityRule.apply(t, currentTimestamp);
                            }
                        }
                    }
                }
            }
            milestone.setLastUpdateDate(currentTimestamp);
        }
    }
}
