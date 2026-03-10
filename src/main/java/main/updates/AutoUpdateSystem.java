package main.updates;

import java.util.ArrayList;
import java.util.List;

/**
 * Sistem de actualizare automata care gestioneaza si aplica o lista de reguli.
 */
public final class AutoUpdateSystem<T> {
    private List<UpdateRule<T>> rules;

    /**
     *
     */
    public AutoUpdateSystem() {
        this.rules = new ArrayList<>();
    }

    /**
     * @param rule
     */
    public void addRule(final UpdateRule<T> rule) {
        this.rules.add(rule);
    }
}
