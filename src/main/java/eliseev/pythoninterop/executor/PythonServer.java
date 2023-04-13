package eliseev.pythoninterop.executor;

import java.io.IOException;

public class PythonServer {
    private static int port = 2323;
    private static boolean isRunning = false;

    private PythonServer() {
    }

    public static void run(final String pythonInterpreterName, final String pathToServerScript) throws IOException {
        run(port, pythonInterpreterName, pathToServerScript);
    }

    public static void run(final int port, final String pythonInterpreterName, final String pathToServerScript)
            throws IOException {
        if (!isRunning) {
            PythonServer.port = port;
            new ProcessBuilder(pythonInterpreterName, pathToServerScript, Integer.toString(port)).start();
            isRunning = true;
        }
    }

    public static int port() {
        return port;
    }
}
