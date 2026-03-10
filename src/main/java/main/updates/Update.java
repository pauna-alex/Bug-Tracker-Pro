package main.updates;

import java.util.ArrayList;
import java.util.List;

import main.milestone.Milestone;
import main.updates.rules.ApplyThreeDayRule;
import main.updates.rules.CheckDueTomorrowRule;
import main.updates.rules.SyncTicketsStateWithMilestone;
import main.updates.rules.UpdateBlockingStatus;
import main.updates.rules.UpdateMilestoneMetrics;

/**
 * Gestioneaza actualizarea automata a milestone-urilor prin reguli predefinite.
 */
public final class Update {

    private static Update instance;
    private List<UpdateRule<Milestone>> rules;

    private Update() {
        this.rules = new ArrayList<>();

        this.rules.add(new SyncTicketsStateWithMilestone());
        this.rules.add(new CheckDueTomorrowRule());
        this.rules.add(new UpdateBlockingStatus());
        this.rules.add(new ApplyThreeDayRule());
        this.rules.add(new UpdateMilestoneMetrics());
    }

    /**
     * @return instance
     */
    public static Update getInstance() {
        if (instance == null) {
            instance = new Update();
        }
        return instance;
    }

    /**
     * Aplica secvential toate regulile de update asupra unui milestone.
     * @param milestone
     * @param currentTimestamp
     */
    public void executeUpdates(final Milestone milestone, final String currentTimestamp) {
        for (UpdateRule<Milestone> rule : this.rules) {
            rule.apply(milestone, currentTimestamp);
        }
    }
}
