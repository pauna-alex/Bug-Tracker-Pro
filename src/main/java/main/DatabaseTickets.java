package main;

import java.util.ArrayList;
import java.util.List;

import main.tickets.Ticket;

/**
 * * Singleton care stocheaza toate tichetele din sistem.
 * */
public final class DatabaseTickets {

    private static DatabaseTickets instance;

    private List<Ticket> tickets;
    private int ticketCounter;
    private String startTestingPhase;

    private DatabaseTickets() {
        this.tickets = new ArrayList<>();
        this.ticketCounter = -1;
    }

    /**
     * @return instance
     */
    public static DatabaseTickets getInstance() {
        if (instance == null) {
            instance = new DatabaseTickets();
        }
        return instance;
    }

    /**
     * @return tickets
     */
    public List<Ticket> getTickets() {
        return tickets;
    }

    /**
     */
    public void clearDatabase() {
        this.tickets.clear();
        this.ticketCounter = -1;
    }

    /**
     */
    public void startTestingPhaseFirstTime() {
        this.startTestingPhase = CommandList.getInstance().getTimeFirstCommand();
    }

    /**
     * @return startTestingPhase
     */
    public String getStartTestingPhase() {
        return this.startTestingPhase;
    }

    /**
     * @param username
     * @return list
     */
    public List<Ticket> getTicketsByUser(final String username) {
        List<Ticket> userTickets = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket.getReportedBy() != null && ticket.getReportedBy().equals(username)) {
                userTickets.add(ticket);
            }
        }
        return userTickets;
    }

    /**
     * Incrementeaza si returneaza urmatorul ID disponibil.
     * @return counter
     */
    public int incId() {
        this.ticketCounter += 1;
        return this.ticketCounter;
    }

    /**
     * @param ticket
     */
    public void addTicket(final Ticket ticket) {
        ticket.setId(this.incId());
        this.tickets.add(ticket);
    }

    /**
     * @param id
     * @return ticket
     */
    public Ticket getTicketByID(final int id) {
        for (Ticket ticket : tickets) {
            if (ticket.getId() == id) {
                return ticket;
            }
        }
        return null;
    }

    /**
     * Filtreaza tichetele care sunt in starea OPEN sau IN_PROGRESS.
     * @return list
     */
    public List<Ticket> getOpenInProgressTickets() {
        List<Ticket> result = new ArrayList<>();
        for (Ticket ticket : this.tickets) {
            if (ticket.getStatus().equals("OPEN") || ticket.getStatus().equals("IN_PROGRESS")) {
                result.add(ticket);
            }
        }
        return result;
    }

    /**
     * @return list
     */
    public List<Integer> getClosedTickets() {
        List<Integer> closedTickets = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket.getStatus().equals("CLOSED")) {
                closedTickets.add(ticket.getId());
            }
        }
        return closedTickets;
    }

    /**
     * @param startTestingPhase
     */
    public void setStartTestingPhase(final String startTestingPhase) {
        this.startTestingPhase = startTestingPhase;
    }

    /**
     * Filtreaza o lista de ID-uri returnand doar pe cele alocate unui user specific.
     * @param ticketIds
     * @param username
     * @return list
     */
    public List<Integer> getTicketsAssignedToUser(final List<Integer> ticketIds,
                                                  final String username) {
        List<Integer> result = new ArrayList<>();

        if (ticketIds == null || username == null) {
            return result;
        }

        for (int id : ticketIds) {
            Ticket t = getTicketByID(id);
            if (t != null && username.equals(t.getAssignedTo())) {
                result.add(id);
            }
        }
        return result;
    }
}
