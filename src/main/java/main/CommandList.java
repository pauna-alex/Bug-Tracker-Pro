package main;

import java.util.ArrayList;
import java.util.List;

import main.command.Command;

/**
 * * Singleton pentru lista de comenzi.
 * */
public final class CommandList {

    private static CommandList instance;

    private List<Command> commands;

    private boolean stopExecution = false;

    private CommandList() {
        commands = new ArrayList<>();
    }

    /**
     * @return stopExecution
     */
    public boolean getStopExecution() {
        return stopExecution;
    }

    /**
     * @return instance
     */
    public static CommandList getInstance() {
        if (instance == null) {
            instance = new CommandList();
        }
        return instance;
    }

    /**
     *
     */
    public void requestStop() {
        this.stopExecution = true;
    }

    /**
     * @return timestamp
     */
    public String getTimeFirstCommand() {
        if (commands.isEmpty()) {
            return null;
        }
        return commands.get(0).getTimeStamp();
    }

    /**
     * @param command
     */
    public void addCommand(final Command command) {
        commands.add(command);
    }

    /**
     * @return commands
     */
    public List<Command> getCommands() {
        return commands;
    }

    /**
     * curataza lista de comenzi
     */
    public void clearCommands() {
        commands.clear();
        stopExecution = false;
    }

}
