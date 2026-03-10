package main.tickets;

import com.fasterxml.jackson.databind.JsonNode;

public final class TicketFactory {

    private TicketFactory() {
    }

    /**
     * Creeaza un obiect de tip Ticket in functie de tipul specificat in JSON.
     *
     * @param jsonNode datele de intrare sub forma de nod JSON
     * @return o instanta a unei subclase Ticket (Bug, FeatureRequest, etc.)
     * @throws IllegalArgumentException daca tipul de tichet este necunoscut
     */
    public static Ticket createTicket(final JsonNode jsonNode) {
        final String type = jsonNode.path("type").asText();
        final String status = "OPEN";

        switch (type) {
            case "BUG" -> {
                return new Bug(
                        type,
                        jsonNode.path("title").asText(),
                        jsonNode.path("businessPriority").asText(),
                        status,
                        jsonNode.path("expertiseArea").asText(),
                        jsonNode.path("description").asText(),
                        jsonNode.path("reportedBy").asText(),
                        jsonNode.path("expectedBehavior").asText(),
                        jsonNode.path("actualBehavior").asText(),
                        jsonNode.path("frequency").asText(),
                        jsonNode.path("severity").asText(),
                        jsonNode.path("environment").asText(),
                        jsonNode.path("errorCode").asInt()
                );
            }
            case "FEATURE_REQUEST" -> {
                return new FeatureRequest(
                        type,
                        jsonNode.path("title").asText(),
                        jsonNode.path("businessPriority").asText(),
                        status,
                        jsonNode.path("expertiseArea").asText(),
                        jsonNode.path("description").asText(),
                        jsonNode.path("reportedBy").asText(),
                        jsonNode.path("businessValue").asText(),
                        jsonNode.path("customerDemand").asText()
                );
            }
            case "UI_FEEDBACK" -> {
                return new UiFeedback(
                        type,
                        jsonNode.path("title").asText(),
                        jsonNode.path("businessPriority").asText(),
                        status,
                        jsonNode.path("expertiseArea").asText(),
                        jsonNode.path("description").asText(),
                        jsonNode.path("reportedBy").asText(),
                        jsonNode.path("uiElementId").asText(),
                        jsonNode.path("businessValue").asText(),
                        jsonNode.path("usabilityScore").asInt(),
                        jsonNode.path("screenshotUrl").asText(),
                        jsonNode.path("suggestedFix").asText()
                );
            }
            default -> throw new IllegalArgumentException("Unknown ticket type: " + type);
        }
    }
}
