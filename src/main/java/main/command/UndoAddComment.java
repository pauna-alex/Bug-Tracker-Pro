package main.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import main.DatabaseTickets;
import main.DatabaseUser;
import main.tickets.Ticket;
import main.user.User;

/**
 * Comanda responsabila pentru anularea adaugarii ultimului comentariu
 * postat de un utilizator la un anumit tichet.
 */
public final class UndoAddComment extends Command implements Exe {

    private final int ticketId;

    /**
     * Constructor pentru comanda UndoAddComment.
     *
     * @param commandType tipul comenzii executate
     * @param username numele utilizatorului care doreste sa stearga comentariul
     * @param timeStamp momentul in care este trimisa cererea de anulare
     * @param ticketId identificatorul unic al tichetului vizat
     */
    public UndoAddComment(final String commandType, final String username,
                           final String timeStamp, final int ticketId) {
        super(commandType, username, timeStamp);
        this.ticketId = ticketId;
    }

    /**
     * Executa operatiunea de eliminare a ultimului comentariu al utilizatorului.
     * Verifica daca tichetul permite comentarii inainte de a procesa stergerea.
     *
     * @return un ObjectNode cu eroarea in caz de esec, sau null daca operatiunea reuseste
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
            return null;
        }

        Ticket ticket = DatabaseTickets.getInstance().getTicketByID(this.ticketId);

        if (ticket == null) {
            return null;
        }

        if (ticket.getReportedBy() == null || ticket.getReportedBy().isEmpty()) {
            output.put("error", "Comments are not allowed on anonymous tickets.");
            return output;
        }

        ticket.removeComment(this.getUsername());

        return null;
    }
}
