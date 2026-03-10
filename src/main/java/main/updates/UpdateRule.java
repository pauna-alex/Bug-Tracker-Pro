package main.updates;

/**
 * Interfata generica pentru definirea regulilor de actualizare automata.
 */
public interface UpdateRule<T> {
    /**
     * @param entity
     * @param currentTimestamp
     */
    void apply(T entity, String currentTimestamp);
}
