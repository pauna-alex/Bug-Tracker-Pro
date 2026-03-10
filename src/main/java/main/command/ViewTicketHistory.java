package main.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import main.DatabaseTickets;
import main.DatabaseUser;
import main.user.User;

/**
 * Comanda responsabila pentru vizualizarea istoricului actiunilor efectuate asupra tichetelor.
 * Accesul la date este filtrat in functie de permisiunile utilizatorului curent.
 */
public final class ViewTicketHistory extends Command implements Exe {

    /**
     * Constructor pentru comanda ViewTicketHistory.
     *
     * @param commandType tipul comenzii executate
     * @param username numele utilizatorului care solicita istoricul
     * @param timeStamp momentul de timp la care este trimisa comanda
     */
    public ViewTicketHistory(final String commandType, final String username,
                             final String timeStamp) {
        super(commandType, username, timeStamp);
    }

    /**
     * Executa operatiunea de preluare a istoricului prin delegarea catre obiectul utilizator.
     *
     * @return un ObjectNode care contine istoricul tichetelor sau un mesaj de eroare
     */
    @Override
    public ObjectNode execute() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode output = mapper.createObjectNode();

        output.put("command", getCommandType());
        output.put("username", getUsername());
        output.put("timestamp", getTimeStamp());

        User utilizator = DatabaseUser.getInstance().getUserByUsername(getUsername());
        if (utilizator == null) {
            output.put("error", "User not found");
            return output;
        }

        return utilizator.viewTicketHistory(output, DatabaseTickets.getInstance().getTickets());
    }
}
