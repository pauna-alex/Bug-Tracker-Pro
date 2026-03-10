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
import main.search.filtre.Keyword;
import main.tickets.Ticket;
import main.tickets.TicketAction;
import main.utl.JsonUtils;

/**
 * */
public final class Manager extends User {

    private String hireDate;
    private List<String> subordinates;

    /**
     * @param username
     * @param email
     * @param role
     * @param hireDate
     * @param subordinates
     */
    public Manager(final String username, final String email, final String role,
                   final String hireDate, final List<String> subordinates) {
        super(username, email, role);
        this.hireDate = hireDate;
        this.subordinates = subordinates;
    }

    /**
     * @param ticket
     * @param output
     * @param timeStamp
     */
    @Override
    public void reportTicket(final Ticket ticket, final ObjectNode output,
                             final String timeStamp) {
        output.put("error", "Managers cannot report tickets.");
    }

    /**
     * @return
     */
    @Override
    public List<Ticket> getViewableTickets() {
        return DatabaseTickets.getInstance().getTickets();
    }

    /**
     * Creeaza un milestone verificand validitatea tichetelor si marcandu-le ca ocupate.
     * @param milestone
     * @param output
     * @param time
     */
    @Override
    public void createMilestone(final Milestone milestone, final ObjectNode output,
                                final String time) {

        for (int idx : milestone.getTickets()) {
            Ticket tmp = DatabaseTickets.getInstance().getTicketByID(idx);

            if (tmp == null) {
                output.put("error", "Ticket " + idx + " not found.");
                return;
            }

            if (tmp.isOwnedByMilestone() == 1) {
                output.put("error", "Tickets " + idx + " already assigned to milestone "
                        + tmp.getOwnedByMilestone() + ".");
                return;
            }
        }

        for (int idx : milestone.getTickets()) {
            Ticket tmp = DatabaseTickets.getInstance().getTicketByID(idx);
            if (tmp != null) {
                tmp.setOwnedByMilestone(milestone.getName());
                tmp.addAction(new TicketAction("ADDED_TO_MILESTONE", this.getUsername(), time)
                        .setMilestone(milestone.getName()));
            }
        }

        milestone.setCreatedBy(super.getUsername());
        milestone.setCreatedAt(time);

        main.updates.Update.getInstance().executeUpdates(milestone, time);
        DatabaseMilestone.getInstance().addMilestone(milestone);
    }

    /**
     * @return
     */
    @Override
    public List<Milestone> getViewableMilestones() {
        List<Milestone> allMilestones = DatabaseMilestone.getInstance().getMilestones();
        List<Milestone> result = new ArrayList<>();
        for (Milestone m : allMilestones) {
            if (this.getUsername().equals(m.getCreatedBy())) {
                result.add(m);
            }
        }
        return result;
    }

    /**
     * @param output
     * @param timestamp
     * @return
     */
    @Override
    public ObjectNode startTestingPhase(final ObjectNode output, final String timestamp) {
        boolean hasActiveMilestones = false;
        for (Milestone m : DatabaseMilestone.getInstance().getMilestones()) {
            main.updates.Update.getInstance().executeUpdates(m, timestamp);
            if ("ACTIVE".equals(m.getStatus())) {
                hasActiveMilestones = true;
                break;
            }
        }

        if (hasActiveMilestones) {
            output.put("error", "Cannot start a new testing phase.");
            return output;
        }

        DatabaseTickets.getInstance().setStartTestingPhase(timestamp);
        return null;
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
                + "required role DEVELOPER; user role MANAGER.");
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
        output.put("error", "Managers cannot add comments to tickets.");
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
                + "required role DEVELOPER; user role MANAGER.");
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
                + "required role DEVELOPER; user role MANAGER.");
    }

    /**
     * Realizeaza cautarea avansata a tichetelor cu extragerea cuvintelor potrivite.
     * @param filters
     * @param output
     */
    @Override
    public void doSearchTickets(final JsonNode filters, final ObjectNode output) {
        List<SearchFilter<Ticket>> criteriaList = FilterBuilder.buildTicketFilters(filters, this);
        AllFilters<Ticket> masterFilter = new AllFilters<>();
        masterFilter.setList(criteriaList);

        List<Ticket> allTickets = DatabaseTickets.getInstance().getTickets();
        List<Ticket> results = new ArrayList<>();

        for (Ticket t : allTickets) {
            if (masterFilter.check(t)) {
                results.add(t);
            }
        }

        results.sort(Comparator.comparing(Ticket::getCreatedAt).thenComparing(Ticket::getId));

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode resultsArray = output.putArray("results");

        List<String> searchKeywords = new ArrayList<>();
        if (filters != null && filters.has("keywords")) {
            filters.get("keywords").forEach(k -> searchKeywords.add(k.asText().toLowerCase()));
        }

        for (Ticket t : results) {
            ObjectNode tNode = mapper.createObjectNode();
            tNode.put("id", t.getId());
            tNode.put("type", t.getType());
            tNode.put("title", t.getTitle());
            tNode.put("businessPriority", t.getBusinessPriority());
            tNode.put("status", t.getStatus());
            tNode.put("createdAt", t.getCreatedAt());
            tNode.put("solvedAt", t.getSolvedAt() == null ? "" : t.getSolvedAt());
            tNode.put("reportedBy", t.getReportedBy());

            ArrayNode matchingNode = tNode.putArray("matchingWords");
            if (!searchKeywords.isEmpty()) {
                java.util.Set<String> uniqueMatches = new java.util.HashSet<>();
                String title = t.getTitle() == null ? "" : t.getTitle();
                String desc = t.getDescription() == null ? "" : t.getDescription();
                String fullText = title + " " + desc;
                String[] words = fullText.split("[^a-zA-Z0-9]+");

                for (String key : searchKeywords) {
                    for (String word : words) {
                        if (word.toLowerCase().contains(key)) {
                            uniqueMatches.add(word.toLowerCase());
                        }
                    }
                }
                List<String> sortedMatches = new ArrayList<>(uniqueMatches);
                Collections.sort(sortedMatches);
                sortedMatches.forEach(matchingNode::add);
            }
            resultsArray.add(tNode);
        }
    }

    /**
     * @param filters
     * @param output
     */
    @Override
    public void doSearchDevelopers(final JsonNode filters, final ObjectNode output) {
        List<SearchFilter<main.user.Developer>> criteriaList =
                FilterBuilder.buildDeveloperFilters(filters, this);

        AllFilters<main.user.Developer> masterFilter = new AllFilters<>();
        masterFilter.setList(criteriaList);

        List<main.user.Developer> allDevs = main.DatabaseUser.getInstance().getAllDevelopers();
        List<main.user.Developer> results = new ArrayList<>();

        for (main.user.Developer dev : allDevs) {
            if (masterFilter.check(dev)) {
                results.add(dev);
            }
        }

        results.sort(Comparator.comparing(main.user.Developer::getUsername));

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode resultsArray = output.putArray("results");

        for (main.user.Developer dev : results) {
            ObjectNode devNode = mapper.createObjectNode();
            devNode.put("username", dev.getUsername());
            devNode.put("expertiseArea", dev.getExpertiseArea());
            devNode.put("seniority", dev.getSeniority());
            devNode.put("performanceScore", dev.getPerformanceScore());
            devNode.put("hireDate", dev.getHireDate());
            resultsArray.add(devNode);
        }
    }

    /**
     * @return
     */
    public List<String> getSubordinates() {
        return subordinates;
    }

    /**
     * @param filters
     * @param json
     */
    @Override
    public void addRoleSpecificFilters(final List<SearchFilter<Ticket>> filters,
                                       final JsonNode json) {
        if (json != null && json.has("keywords")) {
            List<String> keys = new ArrayList<>();
            json.get("keywords").forEach(k -> keys.add(k.asText()));
            filters.add(new Keyword(keys));
        }
    }

    /**
     * @param output
     * @param timestamp
     * @return
     */
    public ObjectNode viewAssignedTickets(final ObjectNode output, final String timestamp) {
        output.put("error", "The user does not have permission to execute this command: "
                + "required role DEVELOPER; user role MANAGER.");
        return output;
    }

    /**
     * @param output
     * @return
     */
    public ObjectNode viewNotifications(final ObjectNode output) {
        output.put("error", "The user does not have permission to execute this command: "
                + "required role DEVELOPER; user role MANAGER.");
        return output;
    }

    /**
     * Construieste istoricul pentru tichetele ce apartin milestone-urilor create de manager.
     * @param output
     * @param allTickets
     * @return
     */
    @Override
    public ObjectNode viewTicketHistory(final ObjectNode output, final List<Ticket> allTickets) {
        java.util.Set<Integer> myTicketIds = new java.util.HashSet<>();
        DatabaseMilestone.getInstance().getMilestones().stream()
                .filter(m -> m.getCreatedBy().equals(this.getUsername()))
                .forEach(m -> myTicketIds.addAll(m.getTickets()));

        List<Ticket> visibleTickets = allTickets.stream()
                .filter(t -> myTicketIds.contains(t.getId()))
                .collect(java.util.stream.Collectors.toList());

        JsonUtils.buildTicketHistory(output, visibleTickets, this.getUsername(), false);
        return output;
    }

    /**
     * @return
     */
    @Override
    public boolean canGeneratePerformanceReport() {
        return true;
    }

    /**
     * @return
     */
    @Override
    public List<String> getSubordinateUsernames() {
        return this.getSubordinates();
    }

    /**
     * @return
     */
    @Override
    public boolean canGenerateReports() {
        return true;
    }
}
