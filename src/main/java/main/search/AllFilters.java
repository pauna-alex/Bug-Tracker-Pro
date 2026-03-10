package main.search;

import java.util.List;

/**
 * Filtru compozit care asigura ca un element respecta toate criteriile din lista.
 *
 * @param <T> tipul elementului verificat
 */
public final class AllFilters<T> implements SearchFilter<T> {

    private List<SearchFilter<T>> listFilters;

    /**
     * Stabileste setul de criterii ce vor fi aplicate succesiv.
     *
     * @param filters setul de filtre de aplicat
     */
    public void setList(final List<SearchFilter<T>> filters) {
        this.listFilters = filters;
    }

    /**
     * Verifica daca elementul intruneste simultan toate conditiile.
     *
     * @param item elementul supus verificarii
     * @return adevarat daca nicio conditie nu este incalcata
     */
    @Override
    public boolean check(final T item) {
        if (listFilters == null) {
            return true;
        }
        for (SearchFilter<T> filter : listFilters) {
            if (!filter.check(item)) {
                return false;
            }
        }
        return true;
    }
}
