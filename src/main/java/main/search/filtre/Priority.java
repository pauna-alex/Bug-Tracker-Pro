package main.search.filtre;

import main.search.SearchFilter;
import main.tickets.Ticket;

/**
 * Filtru pentru verificarea prioritatii de business a unui tichet.
 */
public final class Priority implements SearchFilter<Ticket> {

    private final String priority;

    /**
     * Constructor pentru filtrul de prioritate.
     *
     * @param priority nivelul de prioritate cautat
     */
    public Priority(final String priority) {
        this.priority = priority;
    }

    /**
     * Verifica daca prioritatea tichetului coincide cu cea setata.
     *
     * @param ticket tichetul verificat
     * @return true daca prioritatile sunt identice (case-insensitive)
     */
    @Override
    public boolean check(final Ticket ticket) {
        if (ticket.getBusinessPriority() == null) {
            return false;
        }
        return ticket.getBusinessPriority().equalsIgnoreCase(this.priority);
    }
}
