package main;

import java.util.ArrayList;
import java.util.List;

import main.milestone.Milestone;

/**
 * Singleton care stocheaza toate milestone-urile din sistem.
 * */
public final class DatabaseMilestone {

    private static DatabaseMilestone instance;

    private List<Milestone> milestones;

    private DatabaseMilestone() {
        this.milestones = new ArrayList<>();
    }

    /**
     * @return instance
     */
    public static DatabaseMilestone getInstance() {
        if (instance == null) {
            instance = new DatabaseMilestone();
        }
        return instance;
    }

    /**
     * @return milestones
     */
    public List<Milestone> getMilestones() {
        return milestones;
    }

    /**
     * @param milestone
     */
    public void addMilestone(final Milestone milestone) {
        this.milestones.add(milestone);
    }

    /**
     */
    public void clearDatabase() {
        this.milestones.clear();
    }

    /**
     * Actualizeaza starea tuturor milestone-urilor folosind sistemul de update.
     * @param currentTimestamp
     */
    public void refreshAllMilestones(final String currentTimestamp) {
        for (Milestone m : this.milestones) {
            main.updates.Update.getInstance().executeUpdates(m, currentTimestamp);
        }
    }

    /**
     * @param name
     * @return milestone
     */
    public Milestone getMilestoneByName(final String name) {
        for (Milestone m : milestones) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }
}
