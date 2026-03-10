package main.search.filtre;

import main.search.SearchFilter;
import main.tickets.Ticket;
import main.user.Developer;

/**
 * Filtru pentru verificarea dreptului de acces al unui dezvoltator la un tichet.
 * Permite accesul doar daca tichetul este deschis si apartine unui milestone
 * la care dezvoltatorul este asignat.
 */
public final class DevAccess implements SearchFilter<Ticket> {

    private final Developer developer;

    /**
     * Constructor pentru filtrul de acces al dezvoltatorului.
     *
     * @param developer instanta dezvoltatorului pentru care se verifica accesul
     */
    public DevAccess(final Developer developer) {
        this.developer = developer;
    }

    /**
     * Verifica daca tichetul este accesibil pentru dezvoltator.
     *
     * @param t tichetul verificat
     * @return true daca statusul este OPEN si tichetul face parte din milestones-urile dev-ului
     */
    @Override
    public boolean check(final Ticket t) {
        if (!"OPEN".equalsIgnoreCase(t.getStatus())) {
            return false;
        }

        return developer.isAssignedToMilestoneContaining(t);
    }
}
