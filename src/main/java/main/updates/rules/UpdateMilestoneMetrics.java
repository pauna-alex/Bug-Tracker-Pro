package main.updates.rules;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import main.milestone.Milestone;
import main.updates.UpdateRule;

public final class UpdateMilestoneMetrics implements UpdateRule<Milestone> {

    private static final double PERCENT_CONVERSION = 100.0;

    /**
     * Calculeaza procentul de completare si timpul ramas sau intarzierea pentru milestone.
     * @param milestone
     * @param currentTimestamp
     */
    @Override
    public void apply(final Milestone milestone, final String currentTimestamp) {
        int total = milestone.getTickets().size();
        if (total == 0) {
            milestone.setCompletionPercentage(0.0);
        } else {
            double ratio = (double) milestone.getClosedtickets().size() / total;
            milestone.setCompletionPercentage(Math.round(ratio * PERCENT_CONVERSION)
                    / PERCENT_CONVERSION);
        }

        LocalDate due = LocalDate.parse(milestone.getDueDate());
        LocalDate current = LocalDate.parse(currentTimestamp);

        if ("COMPLETED".equals(milestone.getStatus())) {
            String lastSolved = milestone.getLastTicketClosedDate();
            if (lastSolved != null && !lastSolved.isEmpty()) {
                current = LocalDate.parse(lastSolved);
            }
        }

        if (!current.isAfter(due)) {
            milestone.setDaysUntilDue((int) ChronoUnit.DAYS.between(current, due) + 1);
            milestone.setOverdueBy(0);
        } else {
            milestone.setDaysUntilDue(0);
            milestone.setOverdueBy((int) ChronoUnit.DAYS.between(due, current) + 1);
        }
    }
}
