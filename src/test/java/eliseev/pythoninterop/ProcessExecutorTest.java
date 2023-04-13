package eliseev.pythoninterop;

import eliseev.pythoninterop.executor.ProcessPythonExecutor;
import eliseev.pythoninterop.executor.PythonExecutor;
import eliseev.pythoninterop.executor.PythonExecutorException;
import eliseev.pythoninterop.executor.message.Message;
import eliseev.pythoninterop.executor.message.MessageException;
import eliseev.pythoninterop.executor.message.MessageType;
import org.junit.Test;

import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class ProcessExecutorTest {
    private static final Random RANDOM = new Random(42);
    private static final String PYTHON_INTERPRETER_COMMAND = "python3";
    private static final String PATH_TO_PYTHON_SCRIPTS = "./src/python/";

    private static String generateRandomString() {
        final int length = RANDOM.nextInt(1000);
        final StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            final int codePoint = RANDOM.nextInt(1, Character.MAX_CODE_POINT + 1);
            final int type = Character.getType(codePoint);
            if (type == Character.PRIVATE_USE || type == Character.SURROGATE || type == Character.UNASSIGNED ||
                codePoint == '\n' || codePoint == '\r' || codePoint == '\'') {
                // Не генерируем переносы строк и ', потому что строки, содержащие их, будут некорректными
                // python-строками
                i--;
                continue;
            }

            result.appendCodePoint(codePoint);
        }
        return result.toString();
    }

    private static Message createMessage(final String type, final String text) {
        try {
            return new Message(type, text);
        } catch (final MessageException e) {
            fail(e.getMessage());
            throw new AssertionError("Unreachable");
        }
    }

    private static void testCorrectMessage(final PythonExecutor executor, final Message message) {
        try {
            assertEquals(executor.sendMessage(message).getType(), MessageType.OK);
        } catch (final PythonExecutorException e) {
            fail(e.getMessage());
        }
    }

    private static void testIncorrectMessage(final PythonExecutor executor, final Message message) {
        try {
            assertNotEquals(executor.sendMessage(message).getType(), MessageType.OK);
        } catch (final PythonExecutorException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void smallExpressions() {
        try (final PythonExecutor executor = new ProcessPythonExecutor(PYTHON_INTERPRETER_COMMAND,
                                                                       PATH_TO_PYTHON_SCRIPTS + "python_process.py")
        ) {
            testCorrectMessage(executor, createMessage("EXECUTE", "import math"));

            Stream.of("3 + 3", "[2, 3, 4]", "[i for i in range(1000) if i % 2 == i %" + " 3]", "len(range(2, 200, 5))",
                      "9/3", "math.sin(math.cos(math.e) - math.pi ** 2)")
                  .forEach(expression -> testCorrectMessage(executor, createMessage("EXPRESSION", expression)));
        } catch (final PythonExecutorException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void generatedStringExpressions() {
        try (final PythonExecutor executor = new ProcessPythonExecutor(PYTHON_INTERPRETER_COMMAND,
                                                                       PATH_TO_PYTHON_SCRIPTS + "python_process.py")
        ) {
            IntStream.range(0, 1000).forEach(idx -> testCorrectMessage(executor, createMessage("EXPRESSION", "'" +
                                                                                                             generateRandomString() +
                                                                                                             "'")));
        } catch (final PythonExecutorException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void generatedTexts() {
        try (final PythonExecutor executor = new ProcessPythonExecutor(PYTHON_INTERPRETER_COMMAND,
                                                                       PATH_TO_PYTHON_SCRIPTS + "python_process.py")
        ) {
            IntStream.range(0, 1000).forEach(idx -> {
                testCorrectMessage(executor, createMessage("TEXT", generateRandomString()));
            });
        } catch (final PythonExecutorException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void smallIncorrectExpressions() {
        try (final PythonExecutor executor = new ProcessPythonExecutor(PYTHON_INTERPRETER_COMMAND,
                                                                       PATH_TO_PYTHON_SCRIPTS + "python_process.py")
        ) {
            Stream.of("3 + ''", "10 // 0", "math.e", "[i for i in range(100)")
                  .forEach(expression -> testIncorrectMessage(executor, createMessage("EXPRESSION", expression)));
        } catch (final PythonExecutorException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void generatedIncorrectStringExpressions() {
        try (final PythonExecutor executor = new ProcessPythonExecutor(PYTHON_INTERPRETER_COMMAND,
                                                                       PATH_TO_PYTHON_SCRIPTS + "python_process.py")
        ) {
            IntStream.range(0, 1000).forEach(idx -> testIncorrectMessage(executor, createMessage("EXPRESSION", "'" +
                                                                                                               generateRandomString() +
                                                                                                               "''")));
        } catch (final PythonExecutorException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void generatedIncorrectImportExecutions() {
        try (final PythonExecutor executor = new ProcessPythonExecutor(PYTHON_INTERPRETER_COMMAND,
                                                                       PATH_TO_PYTHON_SCRIPTS + "python_process.py")
        ) {
            IntStream.range(0, 1000).forEach(idx -> testIncorrectMessage(executor, createMessage("EXECUTE", "import " +
                                                                                                            generateRandomString())));
        } catch (final PythonExecutorException e) {
            fail(e.getMessage());
        }
    }
}
