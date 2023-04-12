package eliseev.pythoninterop;

import eliseev.pythoninterop.executor.*;
import eliseev.pythoninterop.executor.message.Message;
import eliseev.pythoninterop.executor.message.MessageType;

import java.util.Scanner;

/**
 * Program that can be run from command line that demonstrating the work of the {@link PythonExecutor}.
 */
public class Main {
    public static void main(final String[] args) {
        try (
                final PythonExecutor executor = new ProcessPythonExecutor("python3", "./src/python/python_process.py");
                final Scanner scanner = new Scanner(System.in)) {
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
