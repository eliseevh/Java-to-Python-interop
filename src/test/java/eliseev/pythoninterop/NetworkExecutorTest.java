package eliseev.pythoninterop;

import eliseev.pythoninterop.executor.*;

import java.io.IOException;

import static org.junit.Assert.fail;

public class NetworkExecutorTest extends BaseExecutorTest {
    @Override
    public void _initialize() {
        super._initialize();
        try {
            PythonServer.run(PYTHON_INTERPRETER_COMMAND, PythonScriptCreator.getDefaultNetworkPath());
            // sleep, чтобы сервер успел запуститься
            // Вероятно, можно лучше, но я не умею
            Thread.sleep(10);
        } catch (final IOException | InterruptedException e) {
            fail(e.getMessage());
        }
    }

    @Override
    protected PythonExecutor getExecutor() throws PythonExecutorException {
        return new NetworkPythonExecutor(null, PythonServer.port());
    }
}
