package main.search.filtre;

import main.search.SearchFilter;
import main.tickets.Ticket;

/**
 * Filtru pentru selectarea tichetelor in functie de data crearii.
 * Permite gasirea sarcinilor care au fost inregistrate intr-o zi specifica.
 */
public final class CreatedAt implements SearchFilter<Ticket> {

    private final String date;

    /**
     * @param date data cautata
     */
    public CreatedAt(final String date) {
        this.date = date;
    }

    /**
     * Verifica daca data crearii sarcinii coincide cu data filtrului.
     *
     * @param t sarcina verificata
     * @return adevarat daca datele sunt identice
     */
    @Override
    public boolean check(final Ticket t) {
        return t.getCreatedAt().equals(date);
    }
}
