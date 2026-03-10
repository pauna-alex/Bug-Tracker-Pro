package main.search;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import main.search.filtre.CreatedAfter;
import main.search.filtre.CreatedAt;
import main.search.filtre.CreatedBefore;
import main.search.filtre.DevExpertise;
import main.search.filtre.DevSeniority;
import main.search.filtre.ManagerSubordinates;
import main.search.filtre.PerformanceScoreAbove;
import main.search.filtre.PerformanceScoreBelow;
import main.search.filtre.Priority;
import main.search.filtre.Type;
import main.tickets.Ticket;
import main.user.Developer;
import main.user.Manager;
import main.user.User;

/**
 * Clasa utilitara pentru construirea listelor de filtre.
 */
public final class FilterBuilder {

    private FilterBuilder() {
    }

    /**
     * Construieste filtrele pentru tichete folosind polimorfismul utilizatorului.
     *
     * @param json datele de intrare
     * @param user utilizatorul curent
     * @return lista de filtre pentru tichete
     */
    public static List<SearchFilter<Ticket>> buildTicketFilters(
            final JsonNode json, final User user) {
        List<SearchFilter<Ticket>> list = new ArrayList<>();

        if (json != null) {
            if (json.has("businessPriority")) {
                list.add(new Priority(json.get("businessPriority").asText()));
            }
            if (json.has("type")) {
                list.add(new Type(json.get("type").asText()));
            }
            if (json.has("createdAt")) {
                list.add(new CreatedAt(json.get("createdAt").asText()));
            }
            if (json.has("createdAfter")) {
                list.add(new CreatedAfter(json.get("createdAfter").asText()));
            }
            if (json.has("createdBefore")) {
                list.add(new CreatedBefore(json.get("createdBefore").asText()));
            }
        }

        user.addRoleSpecificFilters(list, json);
        return list;
    }

    /**
     * Construieste filtrele pentru developeri, specifice accesului de manager.
     *
     * @param json datele de filtrare
     * @param manager managerul care face cautarea
     * @return lista de filtre pentru developeri
     */
    public static List<SearchFilter<Developer>> buildDeveloperFilters(
            final JsonNode json, final Manager manager) {
        List<SearchFilter<Developer>> list = new ArrayList<>();

        list.add(new ManagerSubordinates(manager));

        if (json == null) {
            return list;
        }

        if (json.has("expertiseArea")) {
            list.add(new DevExpertise(json.get("expertiseArea").asText()));
        }
        if (json.has("seniority")) {
            list.add(new DevSeniority(json.get("seniority").asText()));
        }
        if (json.has("performanceScoreAbove")) {
            list.add(new PerformanceScoreAbove(json.get("performanceScoreAbove").asDouble()));
        }
        if (json.has("performanceScoreBelow")) {
            list.add(new PerformanceScoreBelow(json.get("performanceScoreBelow").asDouble()));
        }

        return list;
    }
}
