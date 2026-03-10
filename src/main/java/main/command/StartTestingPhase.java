package main.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import main.user.User;

/**
 * Comanda responsabila pentru initierea unei noi faze de testare in sistem.
 * Actiunea depinde de permisiunile specifice ale utilizatorului care o invoca.
 */
public final class StartTestingPhase extends Command implements Exe {

    /**
     * Constructor pentru comanda StartTestingPhase.
     *
     * @param command numele comenzii executate
     * @param username numele utilizatorului care solicita inceperea fazei de testare
     * @param timestamp momentul de timp la care este emisa comanda
     */
    public StartTestingPhase(final String command, final String username, final String timestamp) {
        super(command, username, timestamp);
    }

    /**
     * Executa operatiunea de pornire a fazei de testare.
     * Delegarea executiei se face catre obiectul utilizator pentru a verifica permisiunile.
     *
     * @return un ObjectNode care contine rezultatul operatiunii sau un mesaj de eroare
     */
    @Override
    public ObjectNode execute() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode output = mapper.createObjectNode();

        output.put("command", this.getCommandType());
        output.put("username", this.getUsername());
        output.put("timestamp", this.getTimeStamp());

        User user = main.DatabaseUser.getInstance().getUserByUsername(this.getUsername());

        if (user == null) {
            output.put("error", "User not found.");
            return output;
        }

        return user.startTestingPhase(output, this.getTimeStamp());
    }
}
