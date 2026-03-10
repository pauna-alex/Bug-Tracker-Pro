package main.updates.rules;

import java.util.List;

import main.DatabaseMilestone;
import main.milestone.Milestone;
import main.updates.UpdateRule;

public final class UpdateBlockingStatus implements UpdateRule<Milestone> {

    /**
     * Verifica daca milestone-ul curent este blocat de alte milestone-uri neterminate.
     * @param milestone
     * @param currentTimestamp
     */
    @Override
    public void apply(final Milestone milestone, final String currentTimestamp) {
        milestone.setBlocked(false);
        List<Milestone> allMilestones = DatabaseMilestone.getInstance().getMilestones();
        if (allMilestones == null) {
            return;
        }

        for (Milestone m : allMilestones) {
            if (m.getBlockingFor() != null && m.getBlockingFor().contains(milestone.getName())) {
                SyncTicketsStateWithMilestone syncRule = new SyncTicketsStateWithMilestone();
                syncRule.apply(m, currentTimestamp);
                if (m.hasOpenTickets()) {
                    milestone.setBlocked(true);
                    break;
                }
            }
        }
    }
}
