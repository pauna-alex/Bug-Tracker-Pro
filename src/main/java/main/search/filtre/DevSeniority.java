package main.search.filtre;

import main.search.SearchFilter;
import main.user.Developer;

/**
 * Filtru pentru verificarea nivelului de senioritate al unui dezvoltator.
 * Comparatia se face ignorand diferenta dintre majuscule si minuscule.
 */
public final class DevSeniority implements SearchFilter<Developer> {

    private final String seniority;

    /**
     * Constructor pentru filtrul de senioritate.
     *
     * @param seniority nivelul de experienta cautat
     */
    public DevSeniority(final String seniority) {
        this.seniority = seniority;
    }

    /**
     * Verifica daca senioritatea dezvoltatorului coincide cu cea cautata.
     *
     * @param d obiectul de tip Developer supus verificarii
     * @return true daca nivelurile coincid sau daca filtrul este nul, false altfel
     */
    @Override
    public boolean check(final Developer d) {
        if (this.seniority == null) {
            return true;
        }
        return d.getSeniority().equalsIgnoreCase(this.seniority);
    }
}
