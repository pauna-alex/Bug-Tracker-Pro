package main.notification;

public interface Observer {

    /**
     * Metoda apelata de subiect pentru a transmite o notificare noua catre observator.
     *
     * @param notification mesajul sau continutul notificarii transmise
     */
    void update(String notification);
}
