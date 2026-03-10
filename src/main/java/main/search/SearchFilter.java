package main.search;

/**
 * Interfata generica pentru definirea criteriilor de filtrare.
 *
 * @param <T> tipul obiectului care va fi verificat
 */
public interface SearchFilter<T> {

    /**
     * Verifica daca un element indeplineste criteriul definit de filtru.
     *
     * @param item obiectul de testat
     * @return true daca elementul trece de filtru, false altfel
     */
    boolean check(T item);
}
