package main.command;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import main.DatabaseMilestone;
import main.DatabaseTickets;
import main.DatabaseUser;
import main.milestone.Milestone;
import main.tickets.Ticket;
import main.user.User;

/**
 * Comanda responsabila pentru schimbarea starii unui tichet si gestionarea
 * efectelor secundare asupra milestone-urilor dependente.
 */
public final class ChangeStatus extends Command implements Exe {

    private final int ticketID;

    /**
     * Constructor pentru comanda ChangeStatus.
     *
     * @param commandType tipul comenzii executate
     * @param username numele utilizatorului care schimba statusul
     * @param timeStamp data si ora operatiunii
     * @param ticketID identificatorul unic al tichetului vizat
     */
    public ChangeStatus(final String commandType, final String username,
                        final String timeStamp, final int ticketID) {
        super(commandType, username, timeStamp);
        this.ticketID = ticketID;
    }

    /**
     * Executa schimbarea de status si declanseaza verificarile pentru
     * deblocarea eventualelor milestone-uri legate de acest tichet.
     *
     * @return un ObjectNode cu detalii despre eroare sau null in caz de succes
     */
    @Override
    public ObjectNode execute() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode output = mapper.createObjectNode();

        output.put("command", getCommandType());
        output.put("username", getUsername());
        output.put("timestamp", getTimeStamp());

        User utilizatorCurent = DatabaseUser.getInstance().getUserByUsername(getUsername());
        Ticket tichetVizat = DatabaseTickets.getInstance().getTicketByID(this.ticketID);

        if (tichetVizat == null) {
            output.put("error", "Ticket " + ticketID + " not found.");
            return output;
        }

        utilizatorCurent.changeStatus(tichetVizat, output, getTimeStamp());

        if (output.has("error")) {
            return output;
        }

        if ("CLOSED".equals(tichetVizat.getStatus())) {
            gestioneazaDeblocariSuccesive(tichetVizat);
        }

        return null;
    }

    /**
     * Identifica milestone-ul parinte al tichetului inchis.
     *
     * @param tichetInchis obiectul tichetului care tocmai a fost finalizat
     */
    private void gestioneazaDeblocariSuccesive(final Ticket tichetInchis) {
        List<Milestone> toateMilestoneurile = DatabaseMilestone.getInstance().getMilestones();
        Milestone milestoneParinte = gasesteMilestoneDupaTichet(tichetInchis.getId(),
                toateMilestoneurile);

        if (milestoneParinte == null) {
            return;
        }

        main.updates.Update.getInstance().executeUpdates(milestoneParinte, getTimeStamp());

        if ("COMPLETED".equals(milestoneParinte.getStatus())) {
            notificaSiDeblocheazaUrmatoareleMilestoneuri(milestoneParinte,
                    toateMilestoneurile, tichetInchis.getId());
        }
    }

    /**
     * Cauta in lista de milestone-uri pe cel care contine un anumit id de tichet.
     *
     * @param idTichet identificatorul tichetului cautat
     * @param milestoneuri lista de milestone-uri in care se face cautarea
     * @return milestone-ul gasit sau null daca tichetul nu apartine niciunuia
     */
    private Milestone gasesteMilestoneDupaTichet(final int idTichet,
                                                  final List<Milestone> milestoneuri) {
        for (Milestone milestone : milestoneuri) {
            if (milestone.getTickets() != null && milestone.getTickets().contains(idTichet)) {
                return milestone;
            }
        }
        return null;
    }

    /**
     * Notifica si incearca deblocarea milestone-urilor dependente.
     *
     * @param milestoneSursa milestone-ul care a fost finalizat
     * @param toateMilestoneurile lista globala de milestone-uri
     * @param idTichetDeclansator id-ul tichetului care a dus la aceasta deblocare
     */
    private void notificaSiDeblocheazaUrmatoareleMilestoneuri(final Milestone milestoneSursa,
                                                              final List<Milestone> toateM,
                                                              final int idTichetDeclansator) {
        List<String> numeMilestoneuriAsteptate = milestoneSursa.getBlockingFor();
        if (numeMilestoneuriAsteptate == null) {
            return;
        }

        for (String numeMilestone : numeMilestoneuriAsteptate) {
            Milestone milestoneDependent = cautaMilestoneDupaNume(numeMilestone, toateM);

            if (milestoneDependent != null) {
                main.updates.Update.getInstance().executeUpdates(milestoneDependent,
                        getTimeStamp());

                if (!milestoneDependent.isBlocked()) {
                    trimiteNotificareDeblocare(milestoneDependent, idTichetDeclansator);
                }
            }
        }
    }

    /**
     * Gestioneaza procesul de notificare pentru un milestone deblocat.
     *
     * @param msDeblocat obiectul milestone-ului care a scapat de blocaj
     * @param idTichet id-ul tichetului finalizat care a permis deblocarea
     */
    private void trimiteNotificareDeblocare(final Milestone msDeblocat, final int idTichet) {
        LocalDate dataCurenta = LocalDate.parse(getTimeStamp());
        LocalDate dataLimita = LocalDate.parse(msDeblocat.getDueDate());

        boolean esteIntarziat = dataCurenta.isAfter(dataLimita);
        String mesajNotificare;

        if (esteIntarziat) {
            mesajNotificare = "Milestone " + msDeblocat.getName()
                + " was unblocked after due date. All active tickets are now CRITICAL.";

            marcheazaTicheteCaFiindCritice(msDeblocat);
        } else {
            mesajNotificare = "Milestone " + msDeblocat.getName()
                + " is now unblocked as ticket " + idTichet + " has been CLOSED.";
        }

        for (String numeUtilizator : msDeblocat.getAssignedDevs()) {
            DatabaseUser.getInstance().addNotification(numeUtilizator, mesajNotificare);
        }
    }

    /**
     * Seteaza prioritatea tuturor tichetelor neinchise la CRITICAL.
     *
     * @param milestone obiectul milestone ale carui tichete trebuie actualizate
     */
    private void marcheazaTicheteCaFiindCritice(final Milestone milestone) {
        for (Integer idTichet : milestone.getTickets()) {
            Ticket tichet = DatabaseTickets.getInstance().getTicketByID(idTichet);
            if (tichet != null && !"CLOSED".equals(tichet.getStatus())) {
                tichet.setBusinessPriority("CRITICAL");
            }
        }
    }

    /**
     * Helper pentru gasirea unui milestone intr-o lista folosind numele.
     *
     * @param nume numele milestone-ului cautat
     * @param milestoneuri lista in care se face cautarea
     * @return obiectul Milestone sau null daca nu este gasit
     */
    private Milestone cautaMilestoneDupaNume(final String nume,
                                             final List<Milestone> milestoneuri) {
        return milestoneuri.stream()
                .filter(m -> m.getName().equals(nume))
                .findFirst()
                .orElse(null);
    }
}
