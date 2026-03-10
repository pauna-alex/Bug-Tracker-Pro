package main.command;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import main.DatabaseUser;
import main.tickets.Ticket;
import main.user.User;

/**
 * Comanda responsabila pentru vizualizarea tichetelor accesibile utilizatorului curent.
 * Tichetele sunt sortate dupa data crearii si apoi dupa identificatorul unic.
 */
public final class ViewTickets extends Command implements Exe {

    /**
     * Constructor pentru comanda ViewTickets.
     *
     * @param commandType tipul comenzii executate
     * @param username numele utilizatorului care doreste sa vada tichetele
     * @param timeStamp momentul in care este solicitata lista
     */
    public ViewTickets(final String commandType, final String username, final String timeStamp) {
        super(commandType, username, timeStamp);
    }

    /**
     * Executa operatiunea de listare a tichetelor vizibile.
     * Rezultatul este sortat cronologic si returnat sub forma de nod JSON.
     *
     * @return un ObjectNode care contine lista tichetelor vizibile
     */
    @Override
    public ObjectNode execute() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode output = mapper.createObjectNode();

        output.put("command", this.getCommandType());
        output.put("username", this.getUsername());
        output.put("timestamp", this.getTimeStamp());

        ArrayNode ticketsArray = output.putArray("tickets");

        User utilizatorCurent = DatabaseUser.getInstance().getUserByUsername(super.getUsername());

        if (utilizatorCurent == null) {
            return output;
        }

        List<Ticket> ticheteVizibile = utilizatorCurent.getViewableTickets();
        List<Ticket> ticheteSortate = new ArrayList<>(ticheteVizibile);

        ticheteSortate.sort(Comparator.comparing(Ticket::getCreatedAt,
                Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparingInt(Ticket::getId));

        for (Ticket t : ticheteSortate) {
            ObjectNode nodTichet = mapper.createObjectNode();
            ticketsArray.add(t.toObjectNode(nodTichet));
        }

        return output;
    }
}
