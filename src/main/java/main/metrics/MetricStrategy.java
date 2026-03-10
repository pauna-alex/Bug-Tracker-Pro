package main.metrics;

import main.tickets.Ticket;

/**
 * Interfata pentru definirea formulelor de calcul ale indicatorilor de performanta.
 * Implementarile vor furniza logica necesara pentru a evalua aspecte precum
 * riscul sau impactul unui tichet.
 */
public interface MetricStrategy {

    /**
     * Calculeaza valoarea indicatorului pentru sarcina primita.
     *
     * @param ticket sarcina evaluata
     * @return rezultatul numeric al calculului
     */
    double calculate(Ticket ticket);
}
