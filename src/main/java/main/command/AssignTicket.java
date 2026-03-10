package main.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import main.DatabaseMilestone;
import main.DatabaseTickets;
import main.DatabaseUser;
import main.milestone.Milestone;
import main.tickets.Ticket;
import main.tickets.TicketAction;
import main.user.User;

/**
 * Comanda pentru alocarea unui tichet unui dezvoltator.
 */
public final class AssignTicket extends Command  implements Exe {

    private final int ticketID;

    /**
     * Constructor pentru comanda AssignTicket.
     *
     * @param commandType tipul comenzii
     * @param username numele utilizatorului
     * @param timeStamp timpul executiei
     * @param ticketID id-ul tichetului
     */
    public AssignTicket(final String commandType, final String username,
                        final String timeStamp, final int ticketID) {
        super(commandType, username, timeStamp);
        this.ticketID = ticketID;
    }

    /**
     * Executa alocarea tichetului catre un utilizator.
     *
     * @return ObjectNode cu eroare sau null pentru succes
     */
    @Override
    public ObjectNode execute() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode output = mapper.createObjectNode();

        output.put("command", this.getCommandType());
        output.put("username", this.getUsername());
        output.put("timestamp", this.getTimeStamp());

        User user = DatabaseUser.getInstance().getUserByUsername(this.getUsername());
        Ticket ticket = DatabaseTickets.getInstance().getTicketByID(this.ticketID);

        if (ticket == null) {
            output.put("error", "Ticket " + ticketID + " not found.");
            return output;
        }

        user.putAssignTicket(ticket, output, this.getTimeStamp());

        if (output.has("error")) {
            return output;
        }

        if (!"OPEN".equals(ticket.getStatus())) {
            output.put("error", "Only OPEN tickets can be assigned.");
            return output;
        }

        String milestoneName = ticket.getOwnedByMilestone();
        Milestone m = DatabaseMilestone.getInstance().getMilestoneByName(milestoneName);

        if (m == null || !m.getAssignedDevs().contains(user.getUsername())) {
             output.put("error", "Developer " + user.getUsername()
                + " is not assigned to milestone "
                + (m != null ? m.getName() : milestoneName) + ".");
             return output;
        }

        if (m.isBlocked()) {
            output.put("error", "Cannot assign ticket " + ticketID
             + " from blocked milestone " + m.getName() + ".");
            return output;
        }

        String oldStatus = ticket.getStatus();

        ticket.setAssignedTo(user.getUsername());
        ticket.setStatus("IN_PROGRESS");
        ticket.setAssignedAt(this.getTimeStamp());

        ticket.addAction(new TicketAction("ASSIGNED",
                user.getUsername(), this.getTimeStamp()));
        ticket.addAction(new TicketAction("STATUS_CHANGED",
                user.getUsername(), this.getTimeStamp())
                .setStatusChange(oldStatus, "IN_PROGRESS"));

        return null;
    }
}
