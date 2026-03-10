package main;

import java.util.ArrayList;
import java.util.List;

import main.user.User;

/**
 * Singleton care stocheaza toti utilizatorii din sistem.
 * */
public final class DatabaseUser {

    private static DatabaseUser instance;

    private List<User> users;

    private DatabaseUser() {
        this.users = new ArrayList<>();
    }

    /**
     * @return instance
     */
    public static DatabaseUser getInstance() {
        if (instance == null) {
            instance = new DatabaseUser();
        }
        return instance;
    }

    /**
     * @param username
     * @return user
     */
    public User getUserByUsername(final String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    /**
     * @param user
     */
    public void addUser(final User user) {
        this.users.add(user);
    }

    /**
     * @return users
     */
    public List<User> getUsers() {
        return this.users;
    }

    /**
     */
    public void clearDatabase() {
        this.users.clear();
    }

    /**
     * Filtreaza utilizatorii si returneaza doar pe cei care pot fi convertiti in Developer.
     * @return list
     */
    public List<main.user.Developer> getAllDevelopers() {
        List<main.user.Developer> developers = new ArrayList<>();

        for (User user : users) {
            main.user.Developer dev = user.toDeveloper();
            if (dev != null) {
                developers.add(dev);
            }
        }

        return developers;
    }

    /**
     * @param username
     * @param message
     */
    public void addNotificationToUser(final String username, final String message) {
        User targetUser = getUserByUsername(username);

        if (targetUser != null) {
            targetUser.update(message);
        }
    }

    /**
     * Trimite o notificare catre user-ul specificat folosind metoda update a acestuia.
     * @param username
     * @param message
     */
    public void addNotification(final String username, final String message) {
        User user = this.getUserByUsername(username);

        if (user != null) {
            user.update(message);
        }
    }
}
