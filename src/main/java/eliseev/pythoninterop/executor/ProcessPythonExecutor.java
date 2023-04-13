package eliseev.pythoninterop.executor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Creates python subprocess and interacts with it sending/receiving messages using its stdin/stdout.
 */
public class ProcessPythonExecutor extends AbstractPythonExecutor {
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

    @Override
    protected Writer getWriter() {
        return process.outputWriter();
    }

    @Override
    protected Reader getReader() {
        return process.inputReader();
    }

    /**
     * Checks if python subprocess is alive.
     * If it is dead, returns optional containing its error stream and exit code.
     *
     * @return empty optional, if subprocess is alive, and optional with information about subprocess, if it is dead
     */
    @Override
    protected Optional<String> canSendMessage() {
        return process.isAlive() ? Optional.empty() : Optional.of(
                "Cannot send message, because python process is dead. Process's error stream:" +
                process.errorReader().lines().collect(
                        Collectors.joining(System.lineSeparator() + "    ", System.lineSeparator() + "    ",
                                           System.lineSeparator())) + "Process's exit code: " + process.exitValue());
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
