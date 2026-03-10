package main.tickets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TicketAction {
    private String type;
    private String by;
    private String timestamp;

    private String fromStatus;
    private String toStatus;
    private String milestone;

    /**
     * @param type
     * @param by
     * @param timestamp
     */
    public TicketAction(final String type, final String by, final String timestamp) {
        this.type = type;
        this.by = by;
        this.timestamp = timestamp;
    }

    /**
     * @return toStatus
     */
    public String getToStatus() {
        return toStatus;
    }

    /**
     * @param from
     * @param to
     * @return this
     */
    public TicketAction setStatusChange(final String from, final String to) {
        this.fromStatus = from;
        this.toStatus = to;
        return this;
    }

    /**
     * @param milestone
     * @return this
     */
    public TicketAction setMilestone(final String milestone) {
        this.milestone = milestone;
        return this;
    }

    /**
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * @return by
     */
    public String getBy() {
        return by;
    }

    /**
     * @return timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Construieste nodul JSON in functie de tipul actiunii inregistrate.
     * @param mapper
     * @return
     */
    public ObjectNode toObjectNode(final ObjectMapper mapper) {
        ObjectNode node = mapper.createObjectNode();

        if ("STATUS_CHANGED".equals(this.type)) {
            node.put("from", this.fromStatus);
            node.put("to", this.toStatus);
            node.put("by", this.by);
            node.put("timestamp", this.timestamp);
            node.put("action", this.type);
        } else if ("ADDED_TO_MILESTONE".equals(this.type)) {
            node.put("milestone", this.milestone);
            node.put("by", this.by);
            node.put("timestamp", this.timestamp);
            node.put("action", this.type);
        } else if ("REMOVED_FROM_DEV".equals(this.type)) {
            node.put("from", this.fromStatus);
            node.put("timestamp", this.timestamp);
            node.put("action", this.type);
            node.put("by", this.by);
        } else {
            node.put("by", this.by);
            node.put("timestamp", this.timestamp);
            node.put("action", this.type);
        }

        return node;
    }
}
