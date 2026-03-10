package main.tickets;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import main.metrics.TicketVisitor;

public abstract class Ticket {

    private int id;
    private String type;
    private String title;
    private String businessPriority;
    private String status;
    private String expertiseArea;
    private String description;
    private String reportedBy;

    private String createdAt;
    private String assignedAt;
    private String solvedAt;
    private String assignedTo;
    private List<Comments> comments;

    private String ownedByMilestone;
    private List<TicketAction> history;
    private double performanceScore;
    private String assignee;

    /**
     * @param type
     * @param title
     * @param businessPriority
     * @param status
     * @param expertiseArea
     * @param description
     * @param reportedBy
     */
    public Ticket(final String type, final String title, final String businessPriority,
                  final String status, final String expertiseArea, final String description,
                  final String reportedBy) {
        this.type = type;
        this.title = title;
        this.businessPriority = businessPriority;
        this.status = status;
        this.expertiseArea = expertiseArea;
        this.description = description;
        this.reportedBy = reportedBy;

        this.comments = new ArrayList<>();
        this.history = new ArrayList<>();
    }

    /**
     * @return id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * @return type
     */
    public abstract String getType();

    /**
     * @return title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * @return description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @return status
     */
    public String getStatus() {
        return this.status;
    }

    /**
     * @param status
     */
    public void setStatus(final String status) {
        this.status = status;
    }

    /**
     * @return businessPriority
     */
    public String getBusinessPriority() {
        return this.businessPriority;
    }

    /**
     * @param businessPriority
     */
    public void setBusinessPriority(final String businessPriority) {
        this.businessPriority = businessPriority;
    }

    /**
     * @return expertiseArea
     */
    public String getExpertiseArea() {
        return this.expertiseArea;
    }

    /**
     * @return reportedBy
     */
    public String getReportedBy() {
        return reportedBy;
    }

    /**
     * @return assignee
     */
    public String getAssignee() {
        return this.assignee;
    }

    /**
     * @param assignee
     */
    public void setAssignee(final String assignee) {
        this.assignee = assignee;
    }

    /**
     * @return assignedTo
     */
    public String getAssignedTo() {
        return this.assignedTo;
    }

    /**
     * @param assignedTo
     */
    public void setAssignedTo(final String assignedTo) {
        this.assignedTo = assignedTo;
    }

    /**
     * @return createdAt
     */
    public String getCreatedAt() {
        return this.createdAt;
    }

    /**
     * @param createdAt
     */
    public void setCreatedAt(final String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return assignedAt
     */
    public String getAssignedAt() {
        return this.assignedAt;
    }

    /**
     * @param assignedAt
     */
    public void setAssignedAt(final String assignedAt) {
        this.assignedAt = assignedAt;
    }

    /**
     * @return solvedAt
     */
    public String getSolvedAt() {
        return this.solvedAt;
    }

    /**
     * @param solvedAt
     */
    public void setSolvedAt(final String solvedAt) {
        this.solvedAt = solvedAt;
    }

    /**
     * @return ownedByMilestone
     */
    public String getOwnedByMilestone() {
        return ownedByMilestone;
    }

    /**
     * @param name
     */
    public void setOwnedByMilestone(final String name) {
        this.ownedByMilestone = name;
    }

    /**
     * @return performanceScore
     */
    public double getPerformanceScore() {
        return performanceScore;
    }

    /**
     * @param performanceScore
     */
    public void setPerformanceScore(final double performanceScore) {
        this.performanceScore = performanceScore;
    }

    /**
     * @return isOwnedByMilestone
     */
    public int isOwnedByMilestone() {
        if (ownedByMilestone != null && !ownedByMilestone.isEmpty()) {
            return 1;
        }
        return 0;
    }

    /**
     * @param username
     * @param comment
     * @param output
     * @param timeStamp
     */
    public void addComment(final String username, final String comment,
                           final ObjectNode output, final String timeStamp) {
        Comments newComment = new Comments(username, comment, timeStamp);
        this.comments.add(newComment);
    }

    /**
     * @param username
     * @return removed
     */
    public boolean removeComment(final String username) {
        if (this.comments == null || this.comments.isEmpty()) {
            return false;
        }

        for (int i = comments.size() - 1; i >= 0; i--) {
            if (comments.get(i).getAuthor().equals(username)) {
                comments.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * @param action
     */
    public void addAction(final TicketAction action) {
        this.history.add(action);
    }

    /**
     * @return history
     */
    public List<TicketAction> getHistory() {
        return this.history;
    }

    /**
     * @return closedAt
     */
    public String getClosedAt() {
        if (this.history == null) {
            return null;
        }

        for (int i = this.history.size() - 1; i >= 0; i--) {
            TicketAction action = this.history.get(i);
            if ("STATUS_CHANGED".equals(action.getType()) && "CLOSED".equals(action.getToStatus())) {
                return action.getTimestamp();
            }
        }
        return null;
    }

    /**
     * @param output
     * @param timeStamp
     */
    public abstract void reportTicket(ObjectNode output, String timeStamp);

    /**
     *
     */
    public abstract void increasePriority();

    /**
     * @return accessible
     */
    public abstract boolean isAccessibleByJunior();

    /**
     * @param node
     * @return ObjectNode
     */
    public ObjectNode toObjectNode(final ObjectNode node) {

        node.put("id", this.id);
        node.put("type", this.type);
        node.put("title", this.title);
        node.put("businessPriority", this.businessPriority);
        node.put("status", this.status);

        node.put("createdAt", this.createdAt != null ? this.createdAt : "");
        node.put("assignedAt", this.assignedAt != null ? this.assignedAt : "");
        node.put("solvedAt", this.solvedAt != null ? this.solvedAt : "");
        node.put("assignedTo", this.assignedTo != null ? this.assignedTo : "");
        node.put("reportedBy", this.reportedBy != null ? this.reportedBy : "");

        ArrayNode commentsArray = node.putArray("comments");

        if (this.comments != null) {
            ObjectMapper mapper = new ObjectMapper();

            for (Comments c : this.comments) {
                commentsArray.add(c.toObjectNode(mapper));
            }
        }

        return node;
    }

    /**
     * @param visitor
     * @return score
     */
    public abstract double accept(TicketVisitor visitor);
}
