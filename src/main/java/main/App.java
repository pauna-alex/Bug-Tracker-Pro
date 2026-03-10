package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;

import main.command.Command;
import main.command.CommandFactory;
import main.user.User;
import main.user.UserFactory;

/**
 * main.App represents the main application logic that processes input commands,
 * generates outputs, and writes them to a file
 */
public final class App {
    private App() {
    }

    private static final String INPUT_USERS_FIELD = "input/database/users.json";

    private static final ObjectWriter WRITER =
        new ObjectMapper().writer().withDefaultPrettyPrinter();

    /**
     * Runs the application: reads commands from an input file,
     * processes them, generates results, and writes them to an output file
     *
     * @param inputPath  path to the input file containing commands
     * @param outputPath path to the file where results should be written
     */
    public static void run(final String inputPath, final String outputPath) {
        // feel free to change this if needed
        // however keep 'outputs' variable name to be used for writing
        List<ObjectNode> outputs = new ArrayList<>();

        /*
         * TODO 1 :
         * Load initial user data and commands. we strongly recommend using jackson
         * library.
         * you can use the reading from hw1 as a reference.
         * however you can use some of the more advanced features of
         * jackson library, available here: https://www.baeldung.com/jackson-annotations
         */


        DatabaseUser.getInstance().clearDatabase();
        DatabaseTickets.getInstance().clearDatabase();
        DatabaseMilestone.getInstance().clearDatabase();


        String path = INPUT_USERS_FIELD;
        File usersFile = new File(path);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode list;

        try {
            list = mapper.readTree(usersFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (JsonNode userNode : list) {
            User user = UserFactory.createUser(userNode);
            DatabaseUser.getInstance().addUser(user);
        }


        CommandList.getInstance().clearCommands();

        File inputFile = new File(inputPath);

        JsonNode listCommands;

        try {
            listCommands = mapper.readTree(inputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (JsonNode commandNode : listCommands) {
            Command command = CommandFactory.createCommand(commandNode);
            CommandList.getInstance().addCommand(command);
        }


        DatabaseTickets.getInstance().startTestingPhaseFirstTime();

        for (Command command : CommandList.getInstance().getCommands()) {
            if (CommandList.getInstance().getStopExecution()) {
                break;
            }

            String currentTimestamp = command.getTimeStamp();

            main.DatabaseMilestone.getInstance().refreshAllMilestones(currentTimestamp);

            ObjectNode output = command.execute();
            if (output != null) {
                outputs.add(output);
            }
        }

        // DO NOT CHANGE THIS SECTION IN ANY WAY
        try {
            File outputFile = new File(outputPath);
            outputFile.getParentFile().mkdirs();
            WRITER.withDefaultPrettyPrinter().writeValue(outputFile, outputs);
        } catch (IOException e) {
            System.out.println("error writing to output file: " + e.getMessage());
        }

    }
}
