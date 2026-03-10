package main.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import main.DatabaseTickets;
import main.tickets.Ticket;
import main.user.User;

/**
 * Comanda pentru adaugarea unui comentariu la un tichet existent.
 */
public final class AddComment extends Command implements Exe {

    private final int ticketID;
    private final String commentText;
    private static final int MIN_COMMENT_LENGTH = 10;

    /**
     * Constructor pentru comanda AddComment.
     *
     * @param commandType tipul comenzii executate
     * @param username numele utilizatorului care adauga comentariul
     * @param timeStamp momentul in care este trimisa comanda
     * @param ticketID identificatorul unic al tichetului
     * @param commentText textul propriu-zis al comentariului
     */
    public AddComment(final String commandType, final String username, final String timeStamp,
                      final int ticketID, final String commentText) {
        super(commandType, username, timeStamp);
        this.ticketID = ticketID;
        this.commentText = commentText;
    }

    /**
     * Executa logica de adaugare a unui comentariu.
     * Verifica validitatea tichetului si lungimea minima a comentariului.
     *
     * @return un ObjectNode cu eroarea daca executia esueaza, sau null daca reuseste
     */
    @Override
    public ObjectNode execute() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode output = mapper.createObjectNode();

        output.put("command", this.getCommandType());
        output.put("username", this.getUsername());
        output.put("timestamp", this.getTimeStamp());

        Ticket ticket = DatabaseTickets.getInstance().getTicketByID(this.ticketID);

        if (ticket == null) {
            return null;
        }

        if (ticket.getReportedBy().isEmpty() || ticket.getReportedBy().isBlank()) {
            output.put("error", "Comments are not allowed on anonymous tickets.");
            return output;
        }

        if (this.commentText.length() < MIN_COMMENT_LENGTH) {
            output.put("error", "Comment must be at least 10 characters long.");
            return output;
        }

        User tmpUser = main.DatabaseUser.getInstance().getUserByUsername(this.getUsername());

        tmpUser.addCommentToTicket(ticket, this.commentText, output, this.getTimeStamp());

        if (output.has("error")) {
            return output;
        }

        return null;
    }
}
