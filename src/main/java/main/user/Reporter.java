package main.user;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import main.DatabaseTickets;
import main.search.SearchFilter;
import main.tickets.Ticket;

/**
 * */
public final class Reporter extends User {

    /**
     * @param username
     * @param email
     * @param role
     */
    public Reporter(final String username, final String email, final String role) {
        super(username, email, role);
    }

    /**
     * @param ticket
     * @param output
     * @param timeStamp
     */
    @Override
    public void reportTicket(final Ticket ticket, final ObjectNode output,
                             final String timeStamp) {
        ticket.reportTicket(output, timeStamp);
    }

    /**
     * @return
     */
    @Override
    public List<Ticket> getViewableTickets() {
        return DatabaseTickets.getInstance().getTicketsByUser(this.getUsername());
    }

    /**
     * @param milestone
     * @param output
     * @param timeStamp
     */
    @Override
    public void createMilestone(final main.milestone.Milestone milestone,
                                final ObjectNode output, final String timeStamp) {
        output.put("error", "The user does not have permission to execute this command: "
                + "required role MANAGER; user role REPORTER.");
    }

    /**
     * @return
     */
    @Override
    public List<main.milestone.Milestone> getViewableMilestones() {
        return new ArrayList<>();
    }

    /**
     * @param ticket
     * @param output
     * @param timeStamp
     */
    @Override
    public void putAssignTicket(final Ticket ticket, final ObjectNode output,
                                final String timeStamp) {
        output.put("error", "The user does not have permission to execute this command: "
                + "required role DEVELOPER; user role REPORTER.");
    }

    /**
     * Permite adaugarea unui comentariu doar daca tichetul este deschis si apartine reporterului.
     * @param ticket
     * @param comment
     * @param output
     * @param timeStamp
     */
    @Override
    public void addCommentToTicket(final Ticket ticket, final String comment,
                                   final ObjectNode output, final String timeStamp) {

        if (ticket.getStatus().equals("CLOSED")) {
            output.put("error", "Reporters cannot comment on CLOSED tickets.");
            return;
        }

        if (ticket.getReportedBy().equals(this.getUsername())) {
            ticket.addComment(this.getUsername(), comment, output, timeStamp);
        } else {
            output.put("error", "Reporter " + this.getUsername()
                    + " cannot comment on ticket " + ticket.getId() + ".");
        }
    }

    /**
     * @param ticket
     * @param output
     * @param timeStamp
     */
    @Override
    public void changeStatus(final Ticket ticket, final ObjectNode output,
                             final String timeStamp) {
        output.put("error", "The user does not have permission to execute this command: "
                + "required role DEVELOPER; user role REPORTER.");
    }

    /**
     * @param ticket
     * @param output
     * @param timeStamp
     */
    @Override
    public void undoChangeStatus(final Ticket ticket, final ObjectNode output,
                                 final String timeStamp) {
        output.put("error", "The user does not have permission to execute this command: "
                + "required role DEVELOPER; user role REPORTER.");
    }

    /**
     * @param filters
     * @param output
     */
    @Override
    public void doSearchTickets(final JsonNode filters, final ObjectNode output) {
        output.put("error", "The user does not have permission to execute this command: "
                + "required role DEVELOPER, MANAGER; user role REPORTER.");
    }

    /**
     * @param filters
     * @param output
     */
    @Override
    public void doSearchDevelopers(final JsonNode filters, final ObjectNode output) {
        output.put("error", "The user does not have permission to execute this command: "
                + "required role DEVELOPER, MANAGER; user role REPORTER.");
    }

    /**
     * @param filters
     * @param json
     */
    public void addRoleSpecificFilters(final List<SearchFilter<Ticket>> filters,
                                       final JsonNode json) {
    }

    /**
     * @param output
     * @param timestamp
     * @return
     */
    public ObjectNode startTestingPhase(final ObjectNode output, final String timestamp) {
        output.put("error", "The user does not have permission to execute this command: "
                + "required role MANAGER; user role REPORTER.");
        return output;
    }

    /**
     * @param output
     * @param timestamp
     * @return
     */
    public ObjectNode viewAssignedTickets(final ObjectNode output, final String timestamp) {
        output.put("error", "The user does not have permission to execute this command: "
                + "required role DEVELOPER; user role REPORTER.");
        return output;
    }

    /**
     * @param output
     * @return
     */
    public ObjectNode viewNotifications(final ObjectNode output) {
        output.put("error", "The user does not have permission to execute this command: "
                + "required role DEVELOPER; user role REPORTER.");
        return output;
    }

    /**
     * @param output
     * @param allTickets
     * @return
     */
    public ObjectNode viewTicketHistory(final ObjectNode output,
                                        final List<main.tickets.Ticket> allTickets) {
        output.put("error", "The user does not have permission to execute this command: "
                + "required role DEVELOPER, MANAGER; user role REPORTER.");
        return output;
    }
}
