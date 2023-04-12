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
    public static void main(final String[] args) {
        if (args.length == 0 || !Set.of("process", "network").contains(args[0])) {
            System.err.println("At least one argument must be specified: mode, and its value must be one of " +
                               "\"process\" or \"network\"");
            return;
        }
        if (args[0].equals("network")) {
            if (args.length >= 2 && args[1].equals("--run")) {
                try {
                    PythonServer.run();
                } catch (final IOException e) {
                    System.err.println("Cannot run python server: " + e.getMessage());
                    return;
                }
            }
        }
        try (
                final PythonExecutor executor = args[0].equals("process") ? new ProcessPythonExecutor("python3",
                                                                                                      "./src/python" +
                                                                                                      "/python_process" +
                                                                                                      ".py") :
                        new NetworkPythonExecutor(
                        null, PythonServer.port()); final Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                final String messageType = scanner.nextLine();
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
                    System.err.println("Unexpected eof in user input");
                    break;
                }
            }
        } catch (final PythonExecutorException e) {
            System.out.println("Stopped because of error: " + e.getMessage());
        }
    }
}
