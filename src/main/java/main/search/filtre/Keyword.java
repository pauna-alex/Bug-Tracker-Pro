package main.search.filtre;

import java.util.List;

import main.search.SearchFilter;
import main.tickets.Ticket;

/**
 * Filtru pentru cautarea tichetelor pe baza unei liste de cuvinte cheie.
 * Cautarea se efectueaza atat in titlu, cat si in descrierea tichetului.
 */
public final class Keyword implements SearchFilter<Ticket> {

    private final List<String> keywords;

    /**
     * Constructor pentru filtrul de cuvinte cheie.
     *
     * @param keywords lista de termeni cautati
     */
    public Keyword(final List<String> keywords) {
        this.keywords = keywords;
    }

    /**
     * Verifica daca cel putin unul dintre cuvintele cheie se regaseste in tichet.
     *
     * @param t tichetul verificat
     * @return true daca exista o potrivire sau daca lista de keywords este goala
     */
    @Override
    public boolean check(final Ticket t) {
        if (keywords == null || keywords.isEmpty()) {
            return true;
        }

        String content = (t.getTitle() + " " + t.getDescription()).toLowerCase();

        for (String key : keywords) {
            if (content.contains(key.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
