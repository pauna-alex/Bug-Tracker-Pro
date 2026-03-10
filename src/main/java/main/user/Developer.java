package main.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import main.DatabaseMilestone;
import main.DatabaseTickets;
import main.milestone.Milestone;
import main.search.AllFilters;
import main.search.FilterBuilder;
import main.search.SearchFilter;
import main.search.filtre.AvailableForAssignmentFilter;
import main.search.filtre.DevAccess;
import main.tickets.Ticket;
import main.tickets.TicketAction;
import main.utl.JsonUtils;

public final class Developer extends User {

    private String hireDate;
    private String expertiseArea;
    private String seniority;
    private double performanceScore = 0.0;

    /**
     * @param username
     * @param email
     * @param role
     * @param hireDate
     * @param expertiseArea
     * @param seniority
     */
    public Developer(final String username, final String email, final String role,
                     final String hireDate, final String expertiseArea, final String seniority) {
        super(username, email, role);
        this.hireDate = hireDate;
        this.expertiseArea = expertiseArea;
        this.seniority = seniority;
    }

    /**
     * @param ticket
     * @param output
     * @param timeStamp
     */
    @Override
    public void reportTicket(final Ticket ticket, final ObjectNode output,
                             final String timeStamp) {
        output.put("error", "Developers cannot report tickets.");
    }

    /**
     * Returneaza tichetele OPEN din milestone-urile la care este asignat developerul.
     * @return
     */
    @Override
    public List<Ticket> getViewableTickets() {
        List<Ticket> result = new ArrayList<>();
        List<Milestone> allMilestones = main.DatabaseMilestone.getInstance().getMilestones();

        for (Milestone m : allMilestones) {
            if (m.getAssignedDevs() != null && m.getAssignedDevs().contains(this.getUsername())) {
                for (int ticketId : m.getTickets()) {
                    Ticket t = DatabaseTickets.getInstance().getTicketByID(ticketId);
                    if (t != null && "OPEN".equals(t.getStatus())) {
                        result.add(t);
                    }
                }
            }
        }
        return result;
    }

    /**
     * @param milestone
     * @param output
     * @param timeStamp
     */
    @Override
    public void createMilestone(final Milestone milestone, final ObjectNode output,
                                final String timeStamp) {
        output.put("error", "The user does not have permission to execute this command: "
                + "required role MANAGER; user role DEVELOPER.");
    }

    /**
     * @return
     */
    @Override
    public List<Milestone> getViewableMilestones() {
        List<Milestone> allMilestones = main.DatabaseMilestone.getInstance().getMilestones();
        List<Milestone> result = new ArrayList<>();
        for (Milestone m : allMilestones) {
            if (m.getAssignedDevs().contains(this.getUsername())) {
                result.add(m);
            }
        }
        return result;
    }

    /**
     * @return
     */
    @Override
    public String getSeniority() {
        return this.seniority;
    }

    /**
     * @return
     */
    public String getExpertiseArea() {
        return this.expertiseArea;
    }

    /**
     * @param ticket
     * @param output
     * @param timeStamp
     */
    @Override
    public void putAssignTicket(final Ticket ticket, final ObjectNode output,
                                final String timeStamp) {
        if (!hasExpertiseAccess(this.expertiseArea, ticket.getExpertiseArea())) {
            List<String> requiredRoles = getRolesThatCanAccess(ticket.getExpertiseArea());
            Collections.sort(requiredRoles);
            output.put("error", "Developer " + this.getUsername() + " cannot assign ticket "
                    + ticket.getId() + " due to expertise area. Required: "
                    + String.join(", ", requiredRoles) + "; Current: " + this.expertiseArea + ".");
            return;
        }

        if (!hasSeniorityAccess(this.seniority, ticket)) {
            List<String> requiredLevels = getLevelsThatCanAccess(ticket);
            Collections.sort(requiredLevels);
            output.put("error", "Developer " + this.getUsername() + " cannot assign ticket "
                    + ticket.getId() + " due to seniority level. Required: "
                    + String.join(", ", requiredLevels) + "; Current: " + this.seniority + ".");
        }
    }

    /**
     * Verifica accesul in functie de matricea de roluri si arii de expertiza.
     * @param devRole
     * @param ticketArea
     * @return
     */
    public boolean hasExpertiseAccess(final String devRole, final String ticketArea) {
        if (devRole == null) {
            return false;
        }
        if ("FULLSTACK".equals(devRole)) {
            return true;
        }
        switch (devRole) {
            case "FRONTEND": return "FRONTEND".equals(ticketArea) || "DESIGN".equals(ticketArea);
            case "BACKEND": return "BACKEND".equals(ticketArea) || "DB".equals(ticketArea);
            case "DEVOPS": return "DEVOPS".equals(ticketArea);
            case "DESIGN": return "DESIGN".equals(ticketArea) || "FRONTEND".equals(ticketArea);
            case "DB": return "DB".equals(ticketArea);
            default: return false;
        }
    }

    /**
     * @param ticketArea
     * @return
     */
    public List<String> getRolesThatCanAccess(final String ticketArea) {
        List<String> roles = new ArrayList<>();
        String[] allRoles = {"FRONTEND", "BACKEND", "DEVOPS", "DESIGN", "DB", "FULLSTACK"};
        for (String role : allRoles) {
            if (hasExpertiseAccess(role, ticketArea)) {
                roles.add(role);
            }
        }
        return roles;
    }

    /**
     * Verifica accesul in functie de nivelul de senioritate si proprietatile tichetului.
     * @param devSeniority
     * @param ticket
     * @return
     */
    public boolean hasSeniorityAccess(final String devSeniority, final Ticket ticket) {
        if (devSeniority == null) {
            return false;
        }
        String priority = ticket.getBusinessPriority();
        switch (devSeniority) {
            case "JUNIOR":
                return ("LOW".equals(priority) || "MEDIUM".equals(priority))
                        && ticket.isAccessibleByJunior();
            case "MID":
                return !"CRITICAL".equals(priority);
            case "SENIOR":
                return true;
            default:
                return false;
        }
    }

    /**
     * @param ticket
     * @return
     */
    private List<String> getLevelsThatCanAccess(final Ticket ticket) {
        List<String> levels = new ArrayList<>();
        String[] allLevels = {"JUNIOR", "MID", "SENIOR"};
        for (String lvl : allLevels) {
            if (hasSeniorityAccess(lvl, ticket)) {
                levels.add(lvl);
            }
        }
        return levels;
    }

    /**
     * @param ticket
     * @param comment
     * @param output
     * @param timeStamp
     */
    @Override
    public void addCommentToTicket(final Ticket ticket, final String comment,
                                   final ObjectNode output, final String timeStamp) {
        if (ticket.getAssignedTo() == null || !ticket.getAssignedTo().equals(this.getUsername())) {
            output.put("error", "Ticket " + ticket.getId()
                    + " is not assigned to the developer " + this.getUsername() + ".");
        } else {
            ticket.addComment(this.getUsername(), comment, output, timeStamp);
        }
    }

    /**
     * Modifica statusul tichetului in mod secvential: IN_PROGRESS -> RESOLVED -> CLOSED.
     * @param ticket
     * @param output
     * @param timeStamp
     */
    @Override
    public void changeStatus(final Ticket ticket, final ObjectNode output,
                             final String timeStamp) {
        if (ticket.getAssignedTo() == null || !ticket.getAssignedTo().equals(this.getUsername())) {
            output.put("error", "Ticket " + ticket.getId()
                    + " is not assigned to developer " + this.getUsername() + ".");
            return;
        }

        String currentStatus = ticket.getStatus();
        String newStatus = null;

        if ("IN_PROGRESS".equals(currentStatus)) {
            newStatus = "RESOLVED";
            ticket.setStatus(newStatus);
            ticket.setSolvedAt(timeStamp);
        } else if ("RESOLVED".equals(currentStatus)) {
            newStatus = "CLOSED";
            ticket.setStatus(newStatus);
        }

        if (newStatus != null) {
            ticket.addAction(new TicketAction("STATUS_CHANGED", this.getUsername(), timeStamp)
                    .setStatusChange(currentStatus, newStatus));
        }
    }

    /**
     * @param ticket
     * @param output
     * @param timeStamp
     */
    @Override
    public void undoChangeStatus(final Ticket ticket, final ObjectNode output,
                                 final String timeStamp) {
        if (ticket.getAssignedTo() == null || !ticket.getAssignedTo().equals(this.getUsername())) {
            output.put("error", "Ticket " + ticket.getId()
                    + " is not assigned to developer " + this.getUsername() + ".");
            return;
        }

        String currentStatus = ticket.getStatus();
        String newStatus = null;

        if ("CLOSED".equals(currentStatus)) {
            newStatus = "RESOLVED";
            ticket.setStatus(newStatus);
        } else if ("RESOLVED".equals(currentStatus)) {
            newStatus = "IN_PROGRESS";
            ticket.setStatus(newStatus);
            ticket.setSolvedAt(null);
        }

        if (newStatus != null) {
            ticket.addAction(new TicketAction("STATUS_CHANGED", this.getUsername(), timeStamp)
                    .setStatusChange(currentStatus, newStatus));
        }
    }

    /**
     * @return
     */
    public double getPerformanceScore() {
        return performanceScore;
    }

    /**
     * @param score
     */
    public void setPerformanceScore(final double score) {
        this.performanceScore = score;
    }

    /**
     * @param filters
     * @param output
     */
    @Override
    public void doSearchTickets(final JsonNode filters, final ObjectNode output) {
        List<SearchFilter<Ticket>> criteriaList = FilterBuilder.buildTicketFilters(filters, this);
        AllFilters<Ticket> masterFilter = new AllFilters<>();
        masterFilter.setList(criteriaList);

        List<Ticket> allTickets = DatabaseTickets.getInstance().getTickets();
        List<Ticket> filteredResults = new ArrayList<>();

        for (Ticket t : allTickets) {
            if (masterFilter.check(t)) {
                filteredResults.add(t);
            }
        }

        filteredResults.sort(Comparator.comparing(Ticket::getCreatedAt)
                .thenComparing(Ticket::getId));

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode resultsArray = output.putArray("results");

        for (Ticket t : filteredResults) {
            ObjectNode tNode = mapper.createObjectNode();
            tNode.put("id", t.getId());
            tNode.put("type", t.getType());
            tNode.put("title", t.getTitle());
            tNode.put("businessPriority", t.getBusinessPriority());
            tNode.put("status", t.getStatus());
            tNode.put("createdAt", t.getCreatedAt());
            tNode.put("solvedAt", t.getSolvedAt() == null ? "" : t.getSolvedAt());
            tNode.put("reportedBy", t.getReportedBy());
            resultsArray.add(tNode);
        }
    }

    /**
     * @param filters
     * @param output
     */
    @Override
    public void doSearchDevelopers(final JsonNode filters, final ObjectNode output) {
        output.set("results", output.arrayNode());
    }

    /**
     * @param t
     * @return
     */
    public boolean isAssignedToMilestoneContaining(final Ticket t) {
        List<Milestone> allMilestones = DatabaseMilestone.getInstance().getMilestones();
        for (Milestone m : allMilestones) {
            if (m.getTickets() != null && m.getTickets().contains(t.getId())) {
                if (m.getAssignedDevs() != null
                        && m.getAssignedDevs().contains(this.getUsername())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param filters
     * @param json
     */
    @Override
    public void addRoleSpecificFilters(final List<SearchFilter<Ticket>> filters,
                                       final JsonNode json) {
        filters.add(new DevAccess(this));
        if (json != null && json.has("availableForAssignment")
                && json.get("availableForAssignment").asBoolean()) {
            filters.add(new AvailableForAssignmentFilter(this));
        }
    }

    /**
     * @return
     */
    @Override
    public Developer toDeveloper() {
        return this;
    }

    /**
     * @return
     */
    public String getHireDate() {
        return hireDate;
    }

    /**
     * @param output
     * @param timestamp
     * @return
     */
    @Override
    public ObjectNode startTestingPhase(final ObjectNode output, final String timestamp) {
        output.put("error", "The user does not have permission to execute this command: "
                + "required role MANAGER; user role DEVELOPER.");
        return output;
    }

    /**
     * Filtreaza si sorteaza tichetele asignate developerului curent.
     * @param output
     * @param timestamp
     * @return
     */
    @Override
    public ObjectNode viewAssignedTickets(final ObjectNode output, final String timestamp) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode ticketsArray = output.putArray("assignedTickets");

        List<Ticket> myTickets = new ArrayList<>();
        for (Ticket t : DatabaseTickets.getInstance().getTickets()) {
            if (this.getUsername().equals(t.getAssignedTo())) {
                myTickets.add(t);
            }
        }

        myTickets.sort((t1, t2) -> {
            int p1 = main.metrics.MetricUtils.getPriorityValue(t1.getBusinessPriority());
            int p2 = main.metrics.MetricUtils.getPriorityValue(t2.getBusinessPriority());
            if (p1 != p2) {
                return Integer.compare(p2, p1);
            }
            int dateComp = t1.getCreatedAt().compareTo(t2.getCreatedAt());
            if (dateComp != 0) {
                return dateComp;
            }
            return Integer.compare(t1.getId(), t2.getId());
        });

        for (Ticket t : myTickets) {
            ObjectNode ticketNode = mapper.createObjectNode();
            t.toObjectNode(ticketNode);
            ticketNode.remove("assignedTo");
            ticketNode.remove("solvedAt");
            ticketsArray.add(ticketNode);
        }
        return output;
    }

    /**
     * @param output
     * @return
     */
    @Override
    public ObjectNode viewNotifications(final ObjectNode output) {
        ArrayNode notifsArray = output.putArray("notifications");
        List<String> userNotifications = this.getNotificationsAndClear();
        for (String notif : userNotifications) {
            notifsArray.add(notif);
        }
        return output;
    }

    /**
     * Construieste istoricul pentru tichetele unde developerul a fost asignat.
     * @param output
     * @param allTickets
     * @return
     */
    @Override
    public ObjectNode viewTicketHistory(final ObjectNode output, final List<Ticket> allTickets) {
        List<Ticket> visibleTickets = allTickets.stream()
                .filter(t -> t.getHistory().stream()
                        .anyMatch(a -> "ASSIGNED".equals(a.getType())
                                && this.getUsername().equals(a.getBy())))
                .collect(java.util.stream.Collectors.toList());

        JsonUtils.buildTicketHistory(output, visibleTickets, this.getUsername(), true);
        return output;
    }

    /**
     * @param score
     */
    @Override
    public void updatePerformanceScore(final double score) {
        this.setPerformanceScore(score);
    }

    /**
     * @return
     */
    @Override
    public boolean isReportableDeveloper() {
        return true;
    }
}
