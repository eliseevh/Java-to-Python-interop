package eliseev.pythoninterop.executor;

import java.io.IOException;

public class PythonServer {
    private static final String PATH_TO_SCRIPT = "./src/python/python_network.py";
    private static final int PORT = 2323;
    private static boolean isRunning = false;
    private PythonServer() {
    }

    public static void run() throws IOException {
        if (!isRunning) {
            new ProcessBuilder("python3", PATH_TO_SCRIPT, Integer.toString(PORT)).start();
            isRunning = true;
        }
    }

    public static int port() {
        return PORT;
    }
}
