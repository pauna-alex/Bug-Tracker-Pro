package main.search.filtre;

import main.search.SearchFilter;
import main.user.Developer;

/**
 * Filtru pentru verificarea ariei de expertiza a unui dezvoltator.
 * Comparatia se realizeaza ignorand diferenta dintre majuscule.
 */
public final class DevExpertise implements SearchFilter<Developer> {

    private final String expertise;

    /**
     * Constructor pentru filtrul de expertiza.
     *
     * @param expertise aria tehnologica cautata
     */
    public DevExpertise(final String expertise) {
        this.expertise = expertise;
    }

    /**
     * Verifica daca aria de expertiza a dezvoltatorului coincide cu cea cautata.
     *
     * @param d obiectul de tip Developer supus verificarii
     * @return true daca ariile coincid sau daca criteriul este nul, false altfel
     */
    @Override
    public boolean check(final Developer d) {
        if (this.expertise == null) {
            return true;
        }
        return d.getExpertiseArea().equalsIgnoreCase(this.expertise);
    }
}
