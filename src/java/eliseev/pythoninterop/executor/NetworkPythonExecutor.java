package eliseev.pythoninterop.executor;

import java.io.*;
import java.net.Socket;
import java.util.Optional;

/**
 * Connects to the python process using TCP and interacts with it sending/receiving messages through that connection.
 */
public class NetworkPythonExecutor extends AbstractPythonExecutor {
    private final Socket socket;

    /**
     * Creates connection to the python process that can be used to send/receive messages.
     * If the specified host is null it is equivalent to specifying an address of the loopback interface.
     *
     * @param host the host name, or {@code null} for the loopback address
     * @param port the port number
     * @throws PythonExecutorException if cannot create connection
     */
    public NetworkPythonExecutor(final String host, final int port) throws PythonExecutorException {
        try {
            socket = new Socket(host, port);
        } catch (final IOException e) {
            throw new PythonExecutorException("Cannot connect to python process: ", e);
        }
    }

    /**
     * Gets writer associated with connection's output stream
     *
     * @return writer wrapper of connection's output stream
     * @throws IOException if an I/O error occurs when creating the output stream
     */
    @Override
    protected Writer getWriter() throws IOException {
        return new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    /**
     * Gets reader associated with connection's input stream
     *
     * @return reader wrapper of connection's input stream
     * @throws IOException if an I/O error occurs when creating the input stream or if the connection is closed
     */
    @Override
    protected Reader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    protected Optional<String> canSendMessage() {
        return socket.isClosed() ? Optional.of(
                "Cannot send message, because connection to " + "python process is closed.") : Optional.empty();
    }

    /**
     * Closes connection to the python process
     *
     * @throws PythonExecutorException if an I/O error occurs when closing the connection
     */
    @Override
    public void close() throws PythonExecutorException {
        try {
            socket.close();
        } catch (final IOException e) {
            throw new PythonExecutorException(e);
        }
    }
}
