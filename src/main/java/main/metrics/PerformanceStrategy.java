package main.metrics;

import java.util.List;

import main.tickets.Ticket;

/**
 * Interfata pentru definirea algoritmilor de evaluare a activitatii.
 * Implementarile vor furniza formule de calcul bazate pe volumul de
 * sarcini finalizate de catre utilizatori.
 */
public interface PerformanceStrategy {

    /**
     * Calculeaza punctajul de performanta folosind lista sarcinilor incheiate.
     *
     * @param closedTickets lista sarcinilor finalizate si inchise
     * @return rezultatul numeric al punctajului
     */
    double calculateScore(List<Ticket> closedTickets);
}
