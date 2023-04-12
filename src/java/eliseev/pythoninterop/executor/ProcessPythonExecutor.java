package eliseev.pythoninterop.executor;

import eliseev.pythoninterop.executor.message.Message;

import java.io.IOException;
import java.io.Writer;
import java.util.stream.Collectors;

/**
 * Creates python subprocess and interacts with it sending/receiving messages using its stdin/stdout.
 */
public class ProcessPythonExecutor implements PythonExecutor {
    private final Process process;

    /**
     * Runs python subprocess by invoking {@code interpreterCommand} as an operating system command with {@code
     * scriptFilename} argument.
     *
     * @param interpreterCommand command to run python interpreter. Usually "python" or "python3"
     * @param scriptFilename     path to file with python script to be invoked
     * @throws PythonExecutorException if process cannot be run
     */
    public ProcessPythonExecutor(final String interpreterCommand, final String scriptFilename)
            throws PythonExecutorException {
        try {
            process = new ProcessBuilder(interpreterCommand, scriptFilename).start();
        } catch (final IOException e) {
            throw new PythonExecutorException("Cannot run python process: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     * Checks if python subprocess is alive. If not, exception is thrown.
     *
     * @throws PythonExecutorException if message cannot be sent or answer is in incorrect format or python subprocess
     *                                 is dead
     */
    @Override
    public Message sendMessage(final Message message) throws PythonExecutorException {
        if (!process.isAlive()) {
            throw new PythonExecutorException(
                    "Cannot send message, because python process is dead. Process's error stream:" +
                    process.errorReader().lines().collect(
                            Collectors.joining(System.lineSeparator() + "    ", System.lineSeparator() + "    ",
                                               System.lineSeparator())) + "Process's exit code: " +
                    process.exitValue());
        }
        try {
            final Writer writer = process.outputWriter();
            writer.write(message.getMessage());
            writer.flush();

            return new Message(process.inputReader());
        } catch (final IOException e) {
            throw new PythonExecutorException(e);
        }
    }

    /**
     * Tries to destroy a python subprocess. Method will wait for process to terminate.
     *
     * @throws PythonExecutorException if the current thread is interrupted by another thread while it is waiting for
     *                                 a process to terminate, then the wait is ended and an InterruptedException is
     *                                 thrown.
     */
    @Override
    public void close() throws PythonExecutorException {
        try {
            if (process.isAlive()) {
                process.destroy();
                process.waitFor();
            }
        } catch (final InterruptedException e) {
            throw new PythonExecutorException(e);
        }
    }
}
