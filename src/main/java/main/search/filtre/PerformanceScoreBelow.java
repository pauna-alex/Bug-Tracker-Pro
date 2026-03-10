package main.search.filtre;

import main.search.SearchFilter;
import main.user.Developer;

/**
 * Filtru pentru identificarea dezvoltatorilor cu un scor de performanta
 * mai mic sau egal cu un prag specificat.
 */
public final class PerformanceScoreBelow implements SearchFilter<Developer> {

    private final double threshold;

    /**
     * Constructor pentru filtrul de performanta.
     *
     * @param threshold valoarea maxima a scorului cautat
     */
    public PerformanceScoreBelow(final double threshold) {
        this.threshold = threshold;
    }

    /**
     * Verifica daca scorul dezvoltatorului este sub sau egal cu pragul.
     *
     * @param developer obiectul de tip Developer verificat
     * @return true daca scorul este valid conform pragului
     */
    @Override
    public boolean check(final Developer developer) {
        return developer.getPerformanceScore() <= this.threshold;
    }
}
