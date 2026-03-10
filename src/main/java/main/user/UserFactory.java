package main.user;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public final class UserFactory {

    private UserFactory() {
        // Constructor privat pentru a preveni instantierea clasei utilitare
    }

    /**
     * @param userNode
     * @return
     */
    public static User createUser(final JsonNode userNode) {

        String role = userNode.get("role").asText();

        switch (role) {
            case "DEVELOPER" -> {
                return new Developer(
                        userNode.get("username").asText(),
                        userNode.get("email").asText(),
                        role,
                        userNode.get("hireDate").asText(),
                        userNode.get("expertiseArea").asText(),
                        userNode.get("seniority").asText());
            }

            case "MANAGER" -> {
                JsonNode subordinatesNode = userNode.get("subordinates");

                List<String> subordinates = new ArrayList<>();
                if (subordinatesNode != null && subordinatesNode.isArray()) {
                    for (JsonNode sub : subordinatesNode) {
                        subordinates.add(sub.asText());
                    }
                }

                return new Manager(
                        userNode.get("username").asText(),
                        userNode.get("email").asText(),
                        role,
                        userNode.get("hireDate").asText(),
                        subordinates);
            }

            case "REPORTER" -> {
                return new Reporter(
                        userNode.get("username").asText(),
                        userNode.get("email").asText(),
                        role);
            }

            default -> throw new IllegalArgumentException("Unknown role received: " + role);
        }
    }
}
