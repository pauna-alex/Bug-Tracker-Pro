package main.search.filtre;

import java.util.List;

import main.DatabaseMilestone;
import main.milestone.Milestone;
import main.search.SearchFilter;
import main.tickets.Ticket;
import main.user.Developer;

/**
 * Filtru care identifica tichetele ce pot fi preluate de un anumit dezvoltator.
 * Verifica disponibilitatea in functie de starea milestone-ului, expertiza
 * si nivelul de experienta al utilizatorului.
 */
public final class AvailableForAssignmentFilter implements SearchFilter<Ticket> {

    private final Developer developer;

    /**
     * @param developer utilizatorul pentru care se verifica disponibilitatea
     */
    public AvailableForAssignmentFilter(final Developer developer) {
        this.developer = developer;
    }

    /**
     * Verifica daca o sarcina poate fi asignata tinand cont de regulile de business.
     *
     * @param t sarcina evaluata
     * @return adevarat daca sarcina este eligibila pentru preluare
     */
    @Override
    public boolean check(final Ticket t) {
        if (!"OPEN".equalsIgnoreCase(t.getStatus())) {
            return false;
        }

        Milestone m = getMilestoneForTicket(t.getId());

        if (m == null) {
            return false;
        }

        if (!m.getAssignedDevs().contains(developer.getUsername())) {
            return false;
        }

        if (m.isBlocked()) {
            return false;
        }

        if (!developer.hasExpertiseAccess(developer.getExpertiseArea(), t.getExpertiseArea())) {
            return false;
        }

        return developer.hasSeniorityAccess(developer.getSeniority(), t);
    }

    /**
     * Identifica grupul de sarcini din care face parte tichetul cautat.
     *
     * @param ticketId identificatorul sarcinii
     * @return milestone-ul gasit sau null
     */
    private Milestone getMilestoneForTicket(final int ticketId) {
        List<Milestone> all = DatabaseMilestone.getInstance().getMilestones();

        for (Milestone m : all) {
            if (m.getTickets() != null && m.getTickets().contains(ticketId)) {
                return m;
            }
        }
        return null;
    }
}
