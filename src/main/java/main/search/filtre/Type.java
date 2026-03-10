package main.search.filtre;

import main.search.SearchFilter;
import main.tickets.Ticket;

/**
 * Filtru pentru verificarea tipului unui tichet.
 */
public final class Type implements SearchFilter<Ticket> {

    private final String type;

    /**
     * Constructor pentru filtrul de tip.
     *
     * @param type tipul de tichet cautat
     */
    public Type(final String type) {
        this.type = type;
    }

    /**
     * Verifica daca tipul tichetului coincide cu cel configurat.
     *
     * @param ticket tichetul verificat
     * @return true daca tipurile sunt identice (case-insensitive)
     */
    @Override
    public boolean check(final Ticket ticket) {
        if (ticket.getType() == null) {
            return false;
        }
        return ticket.getType().equalsIgnoreCase(this.type);
    }
}
