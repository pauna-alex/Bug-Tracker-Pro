package main.command;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import main.milestone.Milestone;
import main.user.User;

/**
 * Comanda responsabila pentru crearea unui nou milestone in sistem.
 * Aceasta gestioneaza validarea prin intermediul utilizatorului si notificarea
 * dezvoltatorilor asignati.
 */
public final class CreateMilestone extends Command implements Exe {

    private final Milestone milestone;

    /**
     * Constructor pentru comanda CreateMilestone.
     *
     * @param commandType tipul comenzii executate
     * @param username numele utilizatorului care initiaza crearea
     * @param timeStamp data si ora la care se proceseaza comanda
     * @param milestone obiectul milestone care urmeaza sa fie creat
     */
    public CreateMilestone(final String commandType, final String username,
                           final String timeStamp, final Milestone milestone) {
        super(commandType, username, timeStamp);
        this.milestone = milestone;
    }

    /**
     * Executa procesul de creare a milestone-ului si trimite notificari.
     *
     * @return un ObjectNode continand eroarea in caz de esec, sau null pentru succes
     */
    @Override
    public ObjectNode execute() {
        User utilizator = main.DatabaseUser.getInstance().getUserByUsername(this.getUsername());
        ObjectNode rezultat = new ObjectMapper().createObjectNode();

        rezultat.put("command", this.getCommandType());
        rezultat.put("username", this.getUsername());
        rezultat.put("timestamp", this.getTimeStamp());

        utilizator.createMilestone(this.milestone, rezultat, this.getTimeStamp());

        if (rezultat.has("error")) {
            return rezultat;
        }

        trimiteNotificariCatreDezvoltatori();
        return null;
    }

    /**
     * Helper pentru trimiterea notificarilor catre toti dezvoltatorii
     * asignati noului milestone creat.
     */
    private void trimiteNotificariCatreDezvoltatori() {
        String mesajNotificare = "New milestone " + this.milestone.getName()
                     + " has been created with due date " + this.milestone.getDueDate() + ".";

        List<String> dezvoltatoriAsignati = this.milestone.getAssignedDevs();

        if (dezvoltatoriAsignati != null) {
            for (String numeUtilizator : dezvoltatoriAsignati) {
                main.DatabaseUser.getInstance().addNotification(numeUtilizator, mesajNotificare);
            }
        }
    }
}
