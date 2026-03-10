package main.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import main.DatabaseUser;
import main.user.User;

/**
 * Comanda responsabila pentru afisarea tichetelor care sunt asignate
 * utilizatorului curent.
 */
public final class ViewAssignedTickets extends Command implements Exe {

    /**
     * Constructor pentru comanda ViewAssignedTickets.
     *
     * @param commandType tipul comenzii executate
     * @param username numele utilizatorului care solicita vizualizarea
     * @param timeStamp momentul de timp la care este trimisa comanda
     */
    public ViewAssignedTickets(final String commandType, final String username,
                               final String timeStamp) {
        super(commandType, username, timeStamp);
    }

    /**
     * Executa operatiunea de vizualizare a tichetelor asignate.
     * Rezultatul depinde de implementarea specifica a tipului de utilizator.
     *
     * @return un ObjectNode care contine lista tichetelor sau un mesaj de eroare
     */
    @Override
    public ObjectNode execute() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode output = mapper.createObjectNode();

        output.put("command", getCommandType());
        output.put("username", getUsername());
        output.put("timestamp", getTimeStamp());

        User user = DatabaseUser.getInstance().getUserByUsername(getUsername());

        if (user == null) {
            output.put("error", "User not found");
            return output;
        }

        return user.viewAssignedTickets(output, getTimeStamp());
    }
}
