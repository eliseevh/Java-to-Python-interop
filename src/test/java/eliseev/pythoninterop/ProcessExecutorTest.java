package eliseev.pythoninterop;

import eliseev.pythoninterop.executor.ProcessPythonExecutor;
import eliseev.pythoninterop.executor.PythonExecutor;
import eliseev.pythoninterop.executor.PythonExecutorException;
import eliseev.pythoninterop.executor.PythonScriptCreator;

public class ProcessExecutorTest extends BaseExecutorTest {

    @Override
    protected PythonExecutor getExecutor() throws PythonExecutorException {
        return new ProcessPythonExecutor(PYTHON_INTERPRETER_COMMAND, PythonScriptCreator.getDefaultProcessPath());
    }
}
