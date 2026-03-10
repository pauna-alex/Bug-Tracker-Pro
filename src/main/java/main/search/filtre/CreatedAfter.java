package main.search.filtre;

import main.search.SearchFilter;
import main.tickets.Ticket;

/**
 * Filtru pentru identificarea sarcinilor aparute dupa o anumita data.
 * Ajuta la monitorizarea tichetelor noi inregistrate in sistem.
 */
public final class CreatedAfter implements SearchFilter<Ticket> {

    private final String date;

    /**
     * @param date pragul calendaristic
     */
    public CreatedAfter(final String date) {
        this.date = date;
    }

    /**
     * Verifica daca sarcina a fost creata ulterior datei de referinta.
     *
     * @param t sarcina evaluata
     * @return adevarat daca data crearii este mai recenta
     */
    @Override
    public boolean check(final Ticket t) {
        return t.getCreatedAt().compareTo(date) > 0;
    }
}
