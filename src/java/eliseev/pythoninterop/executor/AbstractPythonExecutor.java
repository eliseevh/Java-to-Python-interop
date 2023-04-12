package eliseev.pythoninterop.executor;

import eliseev.pythoninterop.executor.message.Message;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Optional;

public abstract class AbstractPythonExecutor implements PythonExecutor {
    /**
     * Gets writer that can be used to send the message.
     *
     * @return writer
     * @throws IOException if an I/O error occurs when getting stream
     */
    protected abstract Writer getWriter() throws IOException;

    /**
     * Gets reader that can be used to read the answer to the message that was sent.
     *
     * @return reader
     * @throws IOException if an I/O error occurs when getting stream
     */
    protected abstract Reader getReader() throws IOException;

    /**
     * Checks if executor can send the message. If no, returns the {@link String} with details on reason why message
     * cannot be sent.
     *
     * @return empty optional, if message can be sent, and optional with some information why it cannot be sent, if so
     */
    protected abstract Optional<String> canSendMessage();

    /**
     * {@inheritDoc}
     * Calls {@link #canSendMessage()}, if non-empty optional is returned, exception is thrown.
     * Writes method to the result of the {@link #getWriter()} method call.
     * Reads answer from the result of the {@link #getReader()} method call.
     *
     * @throws PythonExecutorException if message cannot be sent or answer is in incorrect format or
     *                                 {@link #canSendMessage()} returned non-empty optional
     */
    @Override
    public Message sendMessage(final Message message) throws PythonExecutorException {
        final Optional<String> canSendMessage = canSendMessage();
        if (canSendMessage.isPresent()) {
            throw new PythonExecutorException(canSendMessage.get());
        }
        try {
            final Writer writer = getWriter();
            writer.write(message.getMessage());
            writer.flush();

            return new Message(getReader());
        } catch (final IOException e) {
            throw new PythonExecutorException(e);
        }
    }
}
