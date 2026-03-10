package main.milestone;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import main.DatabaseTickets;
import main.tickets.Ticket;
import main.utl.JsonUtils;

/**
 * Reprezinta un grup de sarcini cu un termen limita comun.
 * Gestioneaza progresul, dezvoltatorii alocati si starea de blocare.
 */
public final class Milestone {

    private final String name;
    private String dueDate;
    private final List<String> blockingFor;
    private final List<Integer> tickets;
    private final List<String> assignedDevs;

    private String createdBy;
    private String createdAt;
    private String lastUpdateDate;

    private String status;
    private boolean isBlocked;
    private int daysUntilDue;
    private int overdueBy;
    private final List<Integer> openTickets;
    private final List<Integer> closedTickets;
    private double completionPercentage;

    private boolean dueNotificationSent = false;

    /**
     * Constructor pentru inregistrarea unui nou obiect de tip Milestone.
     * @param name
     * @param dueDate
     * @param blockingFor
     * @param tickets
     * @param assignedDevs
     * @param createdBy
     * @param createdAt
     */
    public Milestone(final String name, final String dueDate, final List<String> blockingFor,
                     final List<Integer> tickets, final List<String> assignedDevs,
                     final String createdBy, final String createdAt) {
        this.name = name;
        this.dueDate = dueDate;
        this.blockingFor = blockingFor != null ? blockingFor : new ArrayList<>();
        this.tickets = tickets != null ? tickets : new ArrayList<>();
        this.assignedDevs = assignedDevs != null ? assignedDevs : new ArrayList<>();
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.lastUpdateDate = createdAt;
        this.openTickets = new ArrayList<>();
        this.closedTickets = new ArrayList<>();
        this.status = "ACTIVE";
    }

    /** @return */
    public String getName() {
        return name;
    }

    /** @return */
    public String getDueDate() {
        return dueDate;
    }

    /** @return */
    public List<Integer> getTickets() {
        return tickets;
    }

    /** @return */
    public List<String> getAssignedDevs() {
        return assignedDevs;
    }

    /** @return */
    public List<String> getBlockingFor() {
        return blockingFor;
    }

    /** @return */
    public String getStatus() {
        return status;
    }

    /** @return */
    public boolean isBlocked() {
        return isBlocked;
    }

    /** @return */
    public String getCreatedBy() {
        return createdBy;
    }

    /** @param author */
    public void setCreatedBy(final String author) {
        this.createdBy = author;
    }

    /** @return */
    public List<Integer> getOpentickets() {
        return openTickets;
    }

    /** @return */
    public List<Integer> getClosedtickets() {
        return closedTickets;
    }

    /** @param status */
    public void setStatus(final String status) {
        this.status = status;
    }

    /** @return */
    public boolean isDueNotificationSent() {
        return dueNotificationSent;
    }

    /** @param dueNotificationSent */
    public void setDueNotificationSent(final boolean dueNotificationSent) {
        this.dueNotificationSent = dueNotificationSent;
    }

    /** @return */
    public String getCreatedAt() {
        return createdAt;
    }

    /** @return */
    public String getLastUpdateDate() {
        return lastUpdateDate;
    }

    /** @param time */
    public void setLastUpdateDate(final String time) {
        this.lastUpdateDate = time;
    }

    /** @param percentage */
    public void setCompletionPercentage(final double percentage) {
        this.completionPercentage = percentage;
    }

    /** @param days */
    public void setDaysUntilDue(final int days) {
        this.daysUntilDue = days;
    }

    /** @param days */
    public void setOverdueBy(final int days) {
        this.overdueBy = days;
    }

    /** @param time */
    public void setCreatedAt(final String time) {
        this.createdAt = time;
        this.lastUpdateDate = time;
    }

    /** @param blocked */
    public void setBlocked(final boolean blocked) {
        this.isBlocked = blocked;
    }

    /**
     * Calculeaza data la care a fost inchisa ultima sarcina.
     * @return
     */
    public String getLastTicketClosedDate() {
        String maxDate = "";
        for (int id : this.tickets) {
            Ticket t = DatabaseTickets.getInstance().getTicketByID(id);
            if (t != null && "CLOSED".equals(t.getStatus())) {
                String closedAt = t.getClosedAt();
                if (closedAt == null || closedAt.isEmpty()) {
                    closedAt = t.getSolvedAt();
                }
                if (closedAt != null && !closedAt.isEmpty()) {
                    if (maxDate.isEmpty() || closedAt.compareTo(maxDate) > 0) {
                        maxDate = closedAt;
                    }
                }
            }
        }
        return maxDate;
    }

    /** @return */
    public boolean hasOpenTickets() {
        return !openTickets.isEmpty();
    }

    /**
     * Transforma datele procesului in format JSON.
     * @param mapper
     * @return
     */
    public ObjectNode toObjectNode(final ObjectMapper mapper) {
        ObjectNode node = mapper.createObjectNode();

        node.put("name", this.name);
        node.put("dueDate", this.dueDate);
        node.put("createdAt", this.createdAt);
        node.put("createdBy", this.createdBy);
        node.put("status", this.status);
        node.put("isBlocked", this.isBlocked);
        node.put("daysUntilDue", this.daysUntilDue);
        node.put("overdueBy", this.overdueBy);
        node.put("completionPercentage", this.completionPercentage);

        JsonUtils.putStringArray(node, "blockingFor", this.blockingFor);
        JsonUtils.putIntArray(node, "tickets", this.tickets);
        JsonUtils.putStringArray(node, "assignedDevs", this.assignedDevs);
        JsonUtils.putIntArray(node, "openTickets", this.openTickets);
        JsonUtils.putIntArray(node, "closedTickets", this.closedTickets);

        ArrayNode repartitionArray = node.putArray("repartition");
        List<ObjectNode> devRepList = new ArrayList<>();

        if (this.assignedDevs != null) {
            for (String devUsername : this.assignedDevs) {
                ObjectNode devNode = mapper.createObjectNode();
                devNode.put("developer", devUsername);
                List<Integer> userTicketIds = main.DatabaseTickets.getInstance()
                                              .getTicketsAssignedToUser(this.tickets, devUsername);
                JsonUtils.putIntArray(devNode, "assignedTickets", userTicketIds);
                devRepList.add(devNode);
            }
        }

        devRepList.sort((a, b) -> {
            int sizeA = a.get("assignedTickets").size();
            int sizeB = b.get("assignedTickets").size();
            if (sizeA != sizeB) {
                return Integer.compare(sizeA, sizeB);
            }
            return a.get("developer").asText().compareTo(b.get("developer").asText());
        });

        devRepList.forEach(repartitionArray::add);
        return node;
    }
}
