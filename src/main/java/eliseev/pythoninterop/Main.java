package eliseev.pythoninterop;

import eliseev.pythoninterop.executor.*;
import eliseev.pythoninterop.executor.message.Message;
import eliseev.pythoninterop.executor.message.MessageType;

import java.io.IOException;
import java.util.Scanner;
import java.util.Set;

/**
 * Program that can be run from command line that demonstrating the work of the {@link PythonExecutor}.
 */
public class Main {
    private static final String PYTHON_INTERPRETER_NAME = "python3";

    public static void main(final String[] args) {
        if (args.length == 0 || !Set.of("process", "network").contains(args[0])) {
            System.err.println("At least one argument must be specified: mode, and its value must be one of " +
                               "\"process\" or \"network\"");
            return;
        }
        try {
            PythonScriptCreator.createPythonScriptsInDefaultDirectory();
        } catch (final IOException e) {
            System.err.println("Cannot create python scripts. Trying to work without creating");
        }
        if (args[0].equals("network")) {
            if (args.length >= 2 && args[1].equals("--run")) {
                final int port;
                if (args.length == 3) {
                    port = Integer.parseInt(args[2]);
                } else {
                    port = PythonServer.port();
                }
                try {
                    PythonServer.run(port, PYTHON_INTERPRETER_NAME, PythonScriptCreator.getDefaultNetworkPath());
                } catch (final IOException e) {
                    System.err.println("Cannot run python server: " + e.getMessage());
                    return;
                }
            }
        }
        try (final PythonExecutor executor = args[0].equals("process") ? new ProcessPythonExecutor(
                PYTHON_INTERPRETER_NAME, PythonScriptCreator.getDefaultProcessPath()) : new NetworkPythonExecutor(null,
                                                                                                       PythonServer.port());
             final Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Enter message type:(or press Ctrl+D to stop)");
            while (scanner.hasNextLine()) {
                final String messageType = scanner.nextLine();
                System.out.println("Enter message text:");
                if (scanner.hasNextLine()) {
                    final String messageText = scanner.nextLine() + System.lineSeparator();
                    final Message result = executor.sendMessage(new Message(messageType, messageText));
                    if (result.getType() != MessageType.OK) {
                        System.err.println("Not an OK answer:");
                        System.err.println(result);
                    } else {
                        System.out.println(result);
                    }
                } else {
                    System.err.println("Unexpected end of user input");
                    break;
                }
                System.out.println("Enter message type:(or press Ctrl+D to stop)");
            }
        } catch (final PythonExecutorException e) {
            System.out.println("Stopped because of error: " + e.getMessage());
        }
    }
}
