package main.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import main.DatabaseUser;
import main.user.User;

/**
 * Comanda responsabila pentru vizualizarea notificarilor primite de un utilizator.
 * Aceasta extrage notificarile din baza de date si le returneaza intr-un format JSON.
 */
public final class ViewNotifications extends Command implements Exe {

    /**
     * Constructor pentru comanda ViewNotifications.
     *
     * @param commandType tipul comenzii executate
     * @param username numele utilizatorului care solicita vizualizarea notificarilor
     * @param timeStamp momentul de timp la care este trimisa comanda
     */
    public ViewNotifications(final String commandType, final String username,
                             final String timeStamp) {
        super(commandType, username, timeStamp);
    }

    /**
     * Executa operatiunea de vizualizare a notificarilor prin delegarea actiunii
     * catre obiectul de tip utilizator identificat in sistem.
     *
     * @return un ObjectNode care contine lista de notificari sau o eroare daca user-ul nu exista
     */
    @Override
    public ObjectNode execute() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode output = mapper.createObjectNode();

        output.put("command", "viewNotifications");
        output.put("username", this.getUsername());
        output.put("timestamp", this.getTimeStamp());

        User utilizator = DatabaseUser.getInstance().getUserByUsername(this.getUsername());

        if (utilizator == null) {
            output.put("error", "User not found");
            return output;
        }

        return utilizator.viewNotifications(output);
    }
}
