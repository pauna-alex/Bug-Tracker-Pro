package main.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import main.DatabaseUser;
import main.user.User;

/**
 * Comanda responsabila pentru efectuarea cautarilor de tichete sau utilizatori.
 * Utilizeaza filtre specifice primite in format JSON.
 */
public final class Search extends Command implements Exe {

    private final JsonNode filters;

    /**
     * Constructor pentru comanda Search.
     *
     * @param commandType tipul comenzii executate
     * @param username numele utilizatorului care initiaza cautarea
     * @param timeStamp momentul in care este trimisa comanda
     * @param filters setul de filtre aplicate pentru cautare
     */
    public Search(final String commandType, final String username,
                  final String timeStamp, final JsonNode filters) {
        super(commandType, username, timeStamp);
        this.filters = filters;
    }

    /**
     * Executa operatiunea de cautare prin delegarea catre obiectul utilizator.
     * Rezultatul cautarii este populat direct in obiectul de iesire.
     *
     * @return un ObjectNode care contine rezultatele cautarii sau o eroare
     */
    @Override
    public ObjectNode execute() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode output = mapper.createObjectNode();

        User user = DatabaseUser.getInstance().getUserByUsername(this.getUsername());

        output.put("command", "search");
        output.put("username", this.getUsername());
        output.put("timestamp", this.getTimeStamp());

        if (user == null) {
            output.put("error", "User not found");
            return output;
        }

        user.performSearch(this.filters, output);

        return output;
    }
}
