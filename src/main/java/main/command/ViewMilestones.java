package main.command;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import main.DatabaseUser;
import main.milestone.Milestone;
import main.user.User;

/**
 * Comanda responsabila pentru afisarea milestone-urilor vizibile pentru utilizatorul curent.
 * Milestone-urile sunt sortate dupa data limita si nume.
 */
public final class ViewMilestones extends Command implements Exe {

    /**
     * Constructor pentru comanda ViewMilestones.
     *
     * @param commandType tipul comenzii executate
     * @param username numele utilizatorului care solicita vizualizarea
     * @param timeStamp momentul de timp la care este trimisa comanda
     */
    public ViewMilestones(final String commandType, final String username, final String timeStamp) {
        super(commandType, username, timeStamp);
    }

    /**
     * Executa operatiunea de listare a milestone-urilor.
     * Realizeaza actualizari automate pentru fiecare milestone inainte de afisare.
     *
     * @return un ObjectNode care contine lista sortata a milestone-urilor vizibile
     */
    @Override
    public ObjectNode execute() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode output = mapper.createObjectNode();

        output.put("command", this.getCommandType());
        output.put("username", this.getUsername());
        output.put("timestamp", this.getTimeStamp());

        User utilizator = DatabaseUser.getInstance().getUserByUsername(this.getUsername());

        if (utilizator == null) {
            return output;
        }

        List<Milestone> milestoneuriVizibile = utilizator.getViewableMilestones();
        List<Milestone> listaSortata = new ArrayList<>(milestoneuriVizibile);

        listaSortata.sort(Comparator.comparing(Milestone::getDueDate)
                .thenComparing(Milestone::getName));

        ArrayNode milestonesArray = output.putArray("milestones");
        for (Milestone m : listaSortata) {
            main.updates.Update.getInstance().executeUpdates(m, this.getTimeStamp());
            milestonesArray.add(m.toObjectNode(mapper));
        }

        return output;
    }
}
