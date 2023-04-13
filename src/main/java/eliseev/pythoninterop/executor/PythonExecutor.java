package eliseev.pythoninterop.executor;

import eliseev.pythoninterop.executor.message.Message;

/**
 * Interacts with python process by sending {@link Message messages}.
 */
public interface PythonExecutor extends AutoCloseable {
    /**
     * Sends a message to python process and returns an answer.
     * @param message message to be sent
     * @return python process answer
     * @throws PythonExecutorException if message cannot be sent or answer is in incorrect format
     */
    Message sendMessage(Message message) throws PythonExecutorException;

    /**
     * @throws PythonExecutorException if an error occurred during closing
     */
    @Override
    void close() throws PythonExecutorException;
}
