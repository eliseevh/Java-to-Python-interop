package eliseev.pythoninterop;

import eliseev.pythoninterop.executor.PythonExecutor;
import eliseev.pythoninterop.executor.PythonExecutorException;
import eliseev.pythoninterop.executor.message.Message;
import eliseev.pythoninterop.executor.message.MessageException;
import eliseev.pythoninterop.executor.message.MessageType;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class BaseExecutorTest {
    protected static final String PYTHON_INTERPRETER_COMMAND = "python3";

    private static final int SUM_SIZE_OF_GENERATED_TESTS = 100000;

    private static final int SMALL_SIZE = 100;

    private static final int BIG_SIZE = 5000;

    private static Random random;

    private static String generateRandomString(final int size) {
        final int length = random.nextInt(size);
        final StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            final int codePoint = random.nextInt(1, Character.MAX_CODE_POINT + 1);
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
    public void _initialize() {
        random = new Random(42);
    }

    protected abstract PythonExecutor getExecutor() throws PythonExecutorException;

    @Test
    public void smallExpressions() {
        try (final PythonExecutor executor = getExecutor()
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
    public void smallGeneratedStringExpressions() {
        try (final PythonExecutor executor = getExecutor()
        ) {
            IntStream.range(0, SUM_SIZE_OF_GENERATED_TESTS / SMALL_SIZE).forEach(idx -> {
                testCorrectMessage(executor, createMessage("EXPRESSION", "'" + generateRandomString(SMALL_SIZE) + "'"));
            });
        } catch (final PythonExecutorException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void bigGeneratedStringExpressions() {
        try (final PythonExecutor executor = getExecutor()
        ) {
            IntStream.range(0, SUM_SIZE_OF_GENERATED_TESTS / BIG_SIZE).forEach(idx -> {
                testCorrectMessage(executor, createMessage("EXPRESSION", "'" + generateRandomString(BIG_SIZE) + "'"));
            });
        } catch (final PythonExecutorException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void smallGeneratedTexts() {
        try (final PythonExecutor executor = getExecutor()
        ) {
            IntStream.range(0, SUM_SIZE_OF_GENERATED_TESTS / SMALL_SIZE).forEach(idx -> {
                testCorrectMessage(executor, createMessage("TEXT", generateRandomString(SMALL_SIZE)));
            });
        } catch (final PythonExecutorException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void bigGeneratedTexts() {
        try (final PythonExecutor executor = getExecutor()
        ) {
            IntStream.range(0, SUM_SIZE_OF_GENERATED_TESTS / BIG_SIZE).forEach(idx -> {
                testCorrectMessage(executor, createMessage("TEXT", generateRandomString(BIG_SIZE)));
            });
        } catch (final PythonExecutorException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void smallIncorrectExpressions() {
        try (final PythonExecutor executor = getExecutor()
        ) {
            Stream.of("3 + ''", "10 // 0", "math.e", "[i for i in range(100)")
                  .forEach(expression -> testIncorrectMessage(executor, createMessage("EXPRESSION", expression)));
        } catch (final PythonExecutorException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void smallGeneratedIncorrectStringExpressions() {
        try (final PythonExecutor executor = getExecutor()
        ) {
            IntStream.range(0, SUM_SIZE_OF_GENERATED_TESTS / SMALL_SIZE).forEach(idx -> {
                testIncorrectMessage(executor,
                                     createMessage("EXPRESSION", "'" + generateRandomString(SMALL_SIZE) + "''"));
            });
        } catch (final PythonExecutorException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void bigGeneratedIncorrectStringExpressions() {
        try (final PythonExecutor executor = getExecutor()
        ) {
            IntStream.range(0, SUM_SIZE_OF_GENERATED_TESTS / BIG_SIZE).forEach(idx -> {
                testIncorrectMessage(executor,
                                     createMessage("EXPRESSION", "'" + generateRandomString(BIG_SIZE) + "''"));
            });
        } catch (final PythonExecutorException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void smallGeneratedIncorrectImportExecutions() {
        try (final PythonExecutor executor = getExecutor()
        ) {
            IntStream.range(0, SUM_SIZE_OF_GENERATED_TESTS / SMALL_SIZE).forEach(idx -> {
                testIncorrectMessage(executor,
                                     createMessage("EXECUTE", "import" + " " + generateRandomString(SMALL_SIZE)));
            });
        } catch (final PythonExecutorException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void bigGeneratedIncorrectImportExecutions() {
        try (final PythonExecutor executor = getExecutor()
        ) {
            IntStream.range(0, SUM_SIZE_OF_GENERATED_TESTS / BIG_SIZE).forEach(idx -> {
                testIncorrectMessage(executor,
                                     createMessage("EXECUTE", "import" + " " + generateRandomString(BIG_SIZE)));
            });
        } catch (final PythonExecutorException e) {
            fail(e.getMessage());
        }
    }
}
