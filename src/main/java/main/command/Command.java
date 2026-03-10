package main.command;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Clasa abstracta care serveste drept baza pentru toate comenzile din sistem.
 * Defineste atributele comune precum tipul comenzii, utilizatorul si timpul executiei.
 */
public abstract class Command implements Exe {

    private final String commandType;
    private final String username;
    private final String timeStamp;

    /**
     * Constructor pentru clasa Command.
     *
     * @param commandType tipul comenzii care urmeaza sa fie executata
     * @param username numele utilizatorului care a initiat comanda
     * @param timeStamp momentul de timp la care a fost creata comanda
     */
    public Command(final String commandType, final String username, final String timeStamp) {
        this.commandType = commandType;
        this.username = username;
        this.timeStamp = timeStamp;
    }

    /**
     * Returneaza tipul comenzii.
     *
     * @return tipul comenzii sub forma de String
     */
    public final String getCommandType() {
        return commandType;
    }

    /**
     * Returneaza amprenta de timp a comenzii.
     *
     * @return timpul executiei sub forma de String
     */
    public final String getTimeStamp() {
        return timeStamp;
    }

    /**
     * Returneaza numele utilizatorului care a emis comanda.
     *
     * @return numele de utilizator sub forma de String
     */
    public final String getUsername() {
        return username;
    }

    /**
     * Executa comanda curenta.
    */
    @Override
    public ObjectNode execute() {
        return null;
    }
}
