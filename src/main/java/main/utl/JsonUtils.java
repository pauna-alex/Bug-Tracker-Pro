package main.utl;

import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import main.tickets.Ticket;
import main.tickets.TicketAction;

/**
 * */
public final class JsonUtils {

    private JsonUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * @param node
     * @param fieldName
     * @param list
     */
    public static void putStringArray(final ObjectNode node, final String fieldName,
                                      final List<String> list) {
        ArrayNode arrayNode = node.putArray(fieldName);
        if (list != null) {
            list.forEach(arrayNode::add);
        }
    }

    /**
     * @param node
     * @param fieldName
     * @param list
     */
    public static void putIntArray(final ObjectNode node, final String fieldName,
                                   final List<Integer> list) {
        ArrayNode arrayNode = node.putArray(fieldName);
        if (list != null) {
            list.forEach(arrayNode::add);
        }
    }

    /**
     * Construieste istoricul tichetelor sortat si filtrat in functie de rol.
     * @param output
     * @param visibleTickets
     * @param currentUsername
     * @param isDeveloper
     */
    public static void buildTicketHistory(final ObjectNode output,
                                          final List<Ticket> visibleTickets,
                                          final String currentUsername,
                                          final boolean isDeveloper) {
        ObjectMapper mapper = new ObjectMapper();

        visibleTickets.sort(Comparator.comparing(Ticket::getCreatedAt,
                Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparingInt(Ticket::getId));

        ArrayNode historyArray = output.putArray("ticketHistory");

        for (Ticket t : visibleTickets) {
            ObjectNode ticketNode = mapper.createObjectNode();
            ObjectNode temp = mapper.createObjectNode();
            t.toObjectNode(temp);

            ticketNode.put("id", t.getId());
            ticketNode.put("title", temp.get("title").asText());
            ticketNode.put("status", t.getStatus());

            ArrayNode actionsNode = ticketNode.putArray("actions");
            ArrayNode commentsNode = ticketNode.putArray("comments");

            boolean stopHistory = false;
            String stopTimestamp = null;

            for (TicketAction action : t.getHistory()) {
                if (stopHistory) {
                    break;
                }

                if (isDeveloper && "DE-ASSIGNED".equals(action.getType())
                        && currentUsername.equals(action.getBy())) {
                    stopHistory = true;
                    stopTimestamp = action.getTimestamp();
                    actionsNode.add(action.toObjectNode(mapper));
                    break;
                }
                actionsNode.add(action.toObjectNode(mapper));
            }

            if (temp.has("comments")) {
                for (var cNode : temp.get("comments")) {
                    if (stopHistory) {
                        String commentTime = cNode.get("createdAt").asText();
                        if (commentTime.compareTo(stopTimestamp) > 0) {
                            continue;
                        }
                    }
                    commentsNode.add(cNode);
                }
            }
            historyArray.add(ticketNode);
        }
    }
}
