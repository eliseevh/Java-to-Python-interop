package eliseev.pythoninterop.executor;

/**
 * Thrown by {@link PythonExecutor} when an error occurred.
 */
public class PythonExecutorException extends Exception {
    /**
     * Creates an {@link PythonExecutorException} with specified error detail message.
     *
     * @param message error detail message
     */
    public PythonExecutorException(final String message) {
        super(message);
    }

    /**
     * Creates an {@link PythonExecutorException} with a cause.
     *
     * @param cause error cause
     */
    public PythonExecutorException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates an {@link PythonExecutorException} with specified error detail message and a cause.
     *
     * @param message error detail message
     * @param cause   error cause
     */
    public PythonExecutorException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
