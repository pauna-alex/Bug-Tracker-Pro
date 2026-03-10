package main.user;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import main.milestone.Milestone;
import main.notification.Observer;
import main.search.SearchFilter;
import main.tickets.Ticket;

public abstract class User implements Observer {

    private String username;
    private String email;
    private String role;

    private List<String> notifications = new ArrayList<>();

    /**
     * @param username
     * @param email
     * @param role
     */
    public User(final String username, final String email, final String role) {
        this.username = username;
        this.email = email;
        this.role = role;
    }

    /**
     * @param notification
     */
    @Override
    public void update(final String notification) {
        this.notifications.add(notification);
    }

    /**
     * Returneaza notificarile curente si goleste lista din sistem.
     * @return copy
     */
    public List<String> getNotificationsAndClear() {
        List<String> copy = new ArrayList<>(this.notifications);
        this.notifications.clear();
        return copy;
    }

    /**
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @return role
     */
    public String getRole() {
        return role;
    }

    /**
     * @param ticket
     * @param output
     * @param timeStamp
     */
    public abstract void reportTicket(Ticket ticket, ObjectNode output, String timeStamp);

    /**
     * @return list
     */
    public abstract List<Ticket> getViewableTickets();

    /**
     * @param milestone
     * @param output
     * @param timeStamp
     */
    public abstract void createMilestone(Milestone milestone, ObjectNode output, String timeStamp);

    /**
     * @return list
     */
    public abstract List<Milestone> getViewableMilestones();

    /**
     * @param ticket
     * @param output
     * @param timeStamp
     */
    public abstract void putAssignTicket(Ticket ticket, ObjectNode output, String timeStamp);

    /**
     * @param ticket
     * @param comment
     * @param output
     * @param timeStamp
     */
    public abstract void addCommentToTicket(Ticket ticket, String comment,
                                            ObjectNode output, String timeStamp);

    /**
     * @param ticket
     * @param output
     * @param timeStamp
     */
    public abstract void changeStatus(Ticket ticket, ObjectNode output, String timeStamp);

    /**
     * @param ticket
     * @param output
     * @param timeStamp
     */
    public abstract void undoChangeStatus(Ticket ticket, ObjectNode output, String timeStamp);

    /**
     * Coordoneaza procesul de cautare redirectionand catre metodele specifice de tip.
     * @param filters
     * @param output
     */
    public void performSearch(final JsonNode filters, final ObjectNode output) {
        String type = (filters != null && filters.has("searchType"))
                      ? filters.get("searchType").asText()
                      : "TICKET";

        output.put("searchType", type);

        if ("DEVELOPER".equals(type)) {
            doSearchDevelopers(filters, output);
        } else {
            doSearchTickets(filters, output);
        }
    }

    /**
     * @param filters
     * @param output
     */
    public abstract void doSearchTickets(JsonNode filters, ObjectNode output);

    /**
     * @param filters
     * @param output
     */
    public abstract void doSearchDevelopers(JsonNode filters, ObjectNode output);

    /**
     * @param filters
     * @param json
     */
    public abstract void addRoleSpecificFilters(List<SearchFilter<Ticket>> filters, JsonNode json);

    /**
     * @return
     */
    public main.user.Developer toDeveloper() {
        return null;
    }

    /**
     * @param output
     * @param timestamp
     * @return
     */
    public abstract ObjectNode startTestingPhase(ObjectNode output, String timestamp);

    /**
     * @param output
     * @param timestamp
     * @return
     */
    public abstract ObjectNode viewAssignedTickets(ObjectNode output, String timestamp);

    /**
     * @param output
     * @return
     */
    public abstract ObjectNode viewNotifications(ObjectNode output);

    /**
     * @return
     */
    public boolean canGeneratePerformanceReport() {
        return false;
    }

    /**
     * @return
     */
    public boolean isReportableDeveloper() {
        return false;
    }

    /**
     * @return
     */
    public List<String> getSubordinateUsernames() {
        return new java.util.ArrayList<>();
    }

    /**
     * @param score
     */
    public void updatePerformanceScore(final double score) {
        // do nothing
    }

    /**
     * @param output
     * @param allTickets
     * @return
     */
    public abstract ObjectNode viewTicketHistory(ObjectNode output, List<Ticket> allTickets);

    /**
     * @return
     */
    public boolean canGenerateReports() {
        return false;
    }

    /**
     * @return
     */
    public String getManagerUsername() {
        return null;
    }

    /**
     * @return
     */
    public String getSeniority() {
        return null;
    }
}
