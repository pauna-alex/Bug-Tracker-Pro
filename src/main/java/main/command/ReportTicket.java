package main.command;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import main.DatabaseTickets;
import main.DatabaseUser;
import main.tickets.Ticket;
import main.user.User;

/**
 * Comanda responsabila pentru raportarea unui nou tichet in sistem.
 * Verifica daca perioada de testare este activa inainte de a permite raportarea.
 */
public final class ReportTicket extends Command implements Exe {

    private final Ticket ticket;
    private static final int TESTING_PHASE_DURATION = 12;

    /**
     * Constructor pentru comanda ReportTicket.
     *
     * @param commandType tipul comenzii executate
     * @param username numele utilizatorului care raporteaza tichetul
     * @param timeStamp momentul in care este raportat tichetul
     * @param ticket obiectul tichet ce urmeaza a fi adaugat in baza de date
     */
    public ReportTicket(final String commandType, final String username,
                        final String timeStamp, final Ticket ticket) {
        super(commandType, username, timeStamp);
        this.ticket = ticket;
    }

    /**
     * Executa procesul de raportare a unui tichet.
     * Valideaza existenta utilizatorului si incadrarea in fereastra de testare.
     *
     * @return un ObjectNode cu eroarea survenita sau null daca operatiunea a reusit
     */
    @Override
    public ObjectNode execute() {
        ObjectNode output = new ObjectMapper().createObjectNode();

        output.put("command", this.getCommandType());
        output.put("username", this.getUsername());
        output.put("timestamp", this.getTimeStamp());

        User utilizatorCurent = DatabaseUser.getInstance().getUserByUsername(super.getUsername());

        if (utilizatorCurent == null) {
            output.put("error", "The user " + this.getUsername() + " does not exist.");
            return output;
        }

        if (esteInAfaraPerioadeiDeTestare()) {
            output.put("error", "Tickets can only be reported during testing phases.");
            return output;
        }

        utilizatorCurent.reportTicket(this.ticket, output, this.getTimeStamp());

        if (output.has("error")) {
            return output;
        }

        return null;
    }

    /**
     * Helper pentru verificarea ferestrei de timp in care este permisa raportarea.
     *
     * @return true daca data curenta depaseste durata fazei de testare
     */
    private boolean esteInAfaraPerioadeiDeTestare() {
        LocalDate startTestare = LocalDate.parse(DatabaseTickets.getInstance()
                .getStartTestingPhase());
        LocalDate dataCurenta = LocalDate.parse(this.getTimeStamp());
        LocalDate finalTestare = startTestare.plusDays(TESTING_PHASE_DURATION);

        return dataCurenta.isAfter(finalTestare);
    }
}
