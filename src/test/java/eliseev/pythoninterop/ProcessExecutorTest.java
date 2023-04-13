package eliseev.pythoninterop;

import eliseev.pythoninterop.executor.ProcessPythonExecutor;
import eliseev.pythoninterop.executor.PythonExecutor;
import eliseev.pythoninterop.executor.PythonExecutorException;

public class ProcessExecutorTest extends BaseExecutorTest {

    @Override
    protected PythonExecutor getExecutor() throws PythonExecutorException {
        return new ProcessPythonExecutor(PYTHON_INTERPRETER_COMMAND, PATH_TO_PYTHON_SCRIPTS + "python_process.py");
    }
}
