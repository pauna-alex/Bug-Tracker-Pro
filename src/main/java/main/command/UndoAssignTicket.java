package main.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import main.DatabaseTickets;
import main.tickets.Ticket;
import main.tickets.TicketAction;

/**
 * Comanda responsabila pentru anularea asignarii unui tichet.
 * Permite unui utilizator sa renunte la un tichet care se afla in lucru.
 */
public final class UndoAssignTicket extends Command implements Exe {

    private final int ticketID;

    /**
     * Constructor pentru comanda UndoAssignTicket.
     *
     * @param commandType tipul comenzii executate
     * @param username numele utilizatorului care doreste sa anuleze asignarea
     * @param timeStamp momentul in care este trimisa comanda
     * @param ticketID identificatorul unic al tichetului vizat
     */
    public UndoAssignTicket(final String commandType, final String username,
                            final String timeStamp, final int ticketID) {
        super(commandType, username, timeStamp);
        this.ticketID = ticketID;
    }

    /**
     * Executa operatiunea de de-asignare a tichetului.
     * Verifica daca tichetul este in starea corespunzatoare si daca
     * utilizatorul curent este cel caruia i-a fost repartizat tichetul.
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

        Ticket ticket = DatabaseTickets.getInstance().getTicketByID(this.ticketID);

        if (ticket == null) {
            return null;
        }

        if (!"IN_PROGRESS".equals(ticket.getStatus())) {
            output.put("error", "Only IN_PROGRESS tickets can be unassigned.");
            return output;
        }

        if (!this.getUsername().equals(ticket.getAssignedTo())) {
            output.put("error", "The user " + this.getUsername()
                    + " is not assigned to this ticket.");
            return output;
        }

        ticket.setAssignedTo("");
        ticket.setStatus("OPEN");
        ticket.setAssignedAt("");

        ticket.addAction(new TicketAction("DE-ASSIGNED", this.getUsername(), this.getTimeStamp()));
        return null;
    }
}

