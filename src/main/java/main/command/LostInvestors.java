package main.command;

import com.fasterxml.jackson.databind.node.ObjectNode;

import main.CommandList;

/**
 * Comanda care semnaleaza pierderea investitorilor si solicita oprirea sistemului.
 */
public final class LostInvestors extends Command implements Exe {

    /**
     * Constructor pentru comanda LostInvestors.
     *
     * @param commandType tipul comenzii executate
     * @param username numele utilizatorului care a declansat comanda
     * @param timeStamp momentul in care a fost emisa comanda
     */
    public LostInvestors(final String commandType, final String username, final String timeStamp) {
        super(commandType, username, timeStamp);
    }

    /**
     * Executa actiunea de oprire a listei de comenzi.
     * Aceasta metoda este apelata atunci cand situatia proiectului determina
     * retragerea investitorilor.
     *
     * @return intotdeauna null, deoarece oprirea nu produce un output JSON specific
     */
    @Override
    public ObjectNode execute() {
        CommandList.getInstance().requestStop();
        return null;
    }
}
