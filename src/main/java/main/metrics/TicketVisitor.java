package main.metrics;

import main.tickets.Bug;
import main.tickets.FeatureRequest;
import main.tickets.UiFeedback;

/**
 * Interfata pentru vizitarea diverselor tipuri de tichete.
 * Permite aplicarea unor calcule sau operatii specifice pe tichete fara
 * a schimba modul in care acestea sunt definite.
 */
public interface TicketVisitor {

    /**
     * Calculeaza indicatorul pentru o eroare de tip bug.
     *
     * @param bug eroarea analizata
     * @return rezultatul calculului
     */
    double visit(final Bug bug);

    /**
     * Calculeaza indicatorul pentru o cerere de functionalitate.
     *
     * @param featureRequest cererea analizata
     * @return rezultatul calculului
     */
    double visit(final FeatureRequest featureRequest);

    /**
     * Calculeaza indicatorul pentru un feedback de interfata.
     *
     * @param uiFeedback feedback-ul analizat
     * @return rezultatul calculului
     */
    double visit(final UiFeedback uiFeedback);
}
