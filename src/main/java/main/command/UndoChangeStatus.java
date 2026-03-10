package main.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import main.DatabaseTickets;
import main.DatabaseUser;
import main.tickets.Ticket;
import main.user.User;

/**
 * Comanda responsabila pentru anularea ultimei schimbari de status a unui tichet.
 * Actiunea este delegata utilizatorului pentru a verifica permisiunile specifice.
 */
public final class UndoChangeStatus extends Command implements Exe {

    private final int ticketID;

    /**
     * Constructor pentru comanda UndoChangeStatus.
     *
     * @param commandType tipul comenzii executate
     * @param username numele utilizatorului care solicita anularea
     * @param timeStamp momentul la care se efectueaza operatiunea
     * @param ticketID identificatorul unic al tichetului vizat
     */
    public UndoChangeStatus(final String commandType, final String username,
                            final String timeStamp, final int ticketID) {
        super(commandType, username, timeStamp);
        this.ticketID = ticketID;
    }

    /**
     * Executa logica de anulare a schimbarii de status.
     * Verifica existenta utilizatorului si a tichetului inainte de a delega actiunea.
     *
     * @return un ObjectNode cu eroarea survenita sau null daca operatiunea a reusit
     */
    @Override
    public ObjectNode execute() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode output = mapper.createObjectNode();

        output.put("command", this.getCommandType());
        output.put("username", this.getUsername());
        output.put("timestamp", this.getTimeStamp());

        User user = DatabaseUser.getInstance().getUserByUsername(this.getUsername());
        if (user == null) {
            output.put("error", "User not found");
            return output;
        }

        Ticket ticket = DatabaseTickets.getInstance().getTicketByID(this.ticketID);
        if (ticket == null) {
            output.put("error", "Ticket " + ticketID + " not found.");
            return output;
        }

        user.undoChangeStatus(ticket, output, this.getTimeStamp());

        if (output.has("error")) {
            return output;
        }
        return null;
    }
}
