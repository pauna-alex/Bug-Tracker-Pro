package main.search.filtre;

import main.search.SearchFilter;
import main.tickets.Ticket;

/**
 * Filtru pentru identificarea tichetelor create inainte de o anumita data.
 */
public final class CreatedBefore implements SearchFilter<Ticket> {

    private final String date;

    /**
     * Constructor pentru filtrul cronologic.
     *
     * @param date data limita pentru comparatie
     */
    public CreatedBefore(final String date) {
        this.date = date;
    }

    /**
     * Verifica daca tichetul a fost creat inainte de data specificata.
     *
     * @param t tichetul verificat
     * @return true daca data crearii este anterioara celei din filtru
     */
    @Override
    public boolean check(final Ticket t) {
        if (t.getCreatedAt() == null || this.date == null) {
            return false;
        }
        return t.getCreatedAt().compareTo(this.date) < 0;
    }
}
