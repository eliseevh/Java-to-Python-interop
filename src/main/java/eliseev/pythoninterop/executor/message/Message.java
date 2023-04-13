package eliseev.pythoninterop.executor.message;

import java.io.IOException;
import java.io.Reader;

/**
 * Message used in Java and Python process interaction.
 * Format for sending messages between processes:
 * "&lt;message type&gt;&lt;SEPARATOR&gt;&lt;text length&gt;&lt;SEPARATOR&gt;&lt;message text&gt;".
 * &lt;message type&gt; is one of {@link MessageType} enum variants, &lt;message text&gt; is the text of the message,
 * &lt;text length&gt; is the number of the characters in &lt;message text&gt;, &lt;SEPARATOR&gt; is the value of the
 * {@link Message#SEPARATOR} field.
 */
public class Message {
    private static final char SEPARATOR = '\n';
    private final MessageType type;
    private final String text;

    /**
     * Creates message with given type and text.
     *
     * @param type message type. Should be {@link String} representation of one of {@link MessageType} enum variants
     *             with optional whitespaces in begin and/or end
     * @param text message text
     * @throws MessageException if {@code type} is not a representation of one of {@link MessageType} enum variants
     */
    public Message(final String type, final String text) throws MessageException {
        try {
            this.type = MessageType.valueOf(type.stripIndent());
        } catch (final IllegalArgumentException e) {
            throw new MessageException("Unknown message type: " + type, e);
        }
        this.text = text;
    }

    /**
     * Creates a message reading it from {@code messageReader}. It must be in format defined in {@link Message}
     * documentation.
     *
     * @param messageReader reader to get a message from
     * @throws MessageException if an I/O error occurs or message format is incorrect.
     */
    public Message(final Reader messageReader) throws MessageException {
        final String type = readToSeparator(messageReader);
        try {
            this.type = MessageType.valueOf(type);
        } catch (final IllegalArgumentException e) {
            throw new MessageException("Incorrect message format: unknown message type: " + type, e);
        }
        final int messageLength;
        try {
            messageLength = Integer.parseInt(readToSeparator(messageReader));
        } catch (final NumberFormatException e) {
            throw new MessageException("Incorrect message format: not a number after type", e);
        }
        text = readExact(messageReader, messageLength);
    }

    private static String readToSeparator(final Reader reader) throws MessageException {
        final StringBuilder builder = new StringBuilder();
        try {
            readChar:
            while (true) {
                final int read = reader.read();
                if (read == -1) {
                    throw new MessageException("Unexpected eof");
                }
                if ((char) read == SEPARATOR) {
                    return builder.toString();
                }
                builder.append((char) read);
            }
        } catch (final IOException e) {
            throw new MessageException(e);
        }
    }

    private static String readExact(final Reader reader, final int characters) throws MessageException {
        final char[] buffer = new char[characters];
        int pos = 0;
        int read;
        try {
            while (pos < characters && (read = reader.read(buffer, pos, characters - pos)) != -1) {
                pos += read;
            }
        } catch (final IOException e) {
            throw new MessageException(e);
        }
        if (pos == characters) {
            return new String(buffer);
        }
        throw new MessageException("Unexpected eof");
    }

    /**
     * Gets type of the message.
     *
     * @return type of this message
     */
    public MessageType getType() {
        return type;
    }

    /**
     * Gets text of the message.
     *
     * @return text of this message
     */
    public String getText() {
        return text;
    }

    /**
     * Gets {@link String} representation of message in format, defined in {@link Message} documentation.
     *
     * @return message in format, defined in {@link Message} documentation
     */
    public String getMessage() {
        return type.toString() + SEPARATOR + text.codePointCount(0, text.length()) + SEPARATOR + text;
    }

    @Override
    public String toString() {
        if (text.length() != 0) {
            return type + " : " + text;
        } else {
            return type.toString();
        }
    }
}
