package eliseev.pythoninterop.executor.message;

import eliseev.pythoninterop.executor.PythonExecutorException;

/**
 * Thrown by {@link Message} when an error occurred.
 */
public class MessageException extends PythonExecutorException {
    /**
     * Creates an {@link MessageException} with specified error detail message.
     *
     * @param message error detail message
     */
    public MessageException(final String message) {
        super(message);
    }

    /**
     * Creates an {@link MessageException} with a cause.
     *
     * @param cause error cause
     */
    public MessageException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates an {@link PythonExecutorException} with specified error detail message and a cause.
     *
     * @param message error detail message
     * @param cause   error cause
     */
    public MessageException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
