package main.command;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Interfata care defineste contractul pentru executia unei comenzi in sistem.
 * Orice clasa care reprezinta o actiune executabila trebuie sa implementeze aceasta interfata.
 */
public interface Exe {

    /**
     * Metoda responsabila pentru rularea logicii specifice fiecarui tip de comanda.
     *
     * @return un obiect de tip ObjectNode care contine rezultatul executiei comenzii
     */
    ObjectNode execute();
}
