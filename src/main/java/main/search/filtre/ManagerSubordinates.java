package main.search.filtre;

import main.search.SearchFilter;
import main.user.Developer;
import main.user.Manager;

/**
 * Filtru pentru verificarea apartenentei unui dezvoltator la echipa unui manager.
 */
public final class ManagerSubordinates implements SearchFilter<Developer> {

    private final Manager manager;

    /**
     * Constructor pentru filtrul de subordonati.
     *
     * @param manager instanta managerului care detine lista de subordonati
     */
    public ManagerSubordinates(final Manager manager) {
        this.manager = manager;
    }

    /**
     * Verifica daca un dezvoltator este subordonat managerului curent.
     *
     * @param d dezvoltatorul verificat
     * @return true daca username-ul dezvoltatorului se afla in lista managerului
     */
    @Override
    public boolean check(final Developer d) {
        if (manager.getSubordinates() == null) {
            return false;
        }
        return manager.getSubordinates().contains(d.getUsername());
    }
}
