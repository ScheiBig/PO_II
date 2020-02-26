package edu.jeznach.po2.common.util;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 * Class providing wrapping extension to InputStream of reading lines of bytes. This
 * means that all methods of passed {@link InputStream} are delegated, byt additional
 * {@link BreakableInputStream#readLine()} method is provided, that returns all bytes
 * from current stream position to {@link System#lineSeparator()}. This is very useful
 * utility to occasionally read lines of bytes that might be {@code Strings} from otherwise
 * raw {@code InputStreams} (like reading some properties from top of file, to initialize
 * output that rest should be read to), which is impossible with classes like
 * {@link java.io.BufferedReader} which consume passed {@link InputStream} in not deterministic
 * quantities, which renders reading rest of {@link InputStream} (after
 * {@link System#lineSeparator() line separator}) unreliable.
 */
public class BreakableInputStream extends InputStream {

    private InputStream stream;

    /**
     * Creates new {@code BreakableInputStream} wrapper
     * @param stream the wrapped {@link InputStream}
     */
    public BreakableInputStream(InputStream stream) {
        this.stream = stream;
    }

    /**
     * Reads all of bytes of data from the input stream, from current position of stream,
     * to {@link System#lineSeparator() lineSeparator}. Resulting array does not contain
     * {@code lineSeparator} character(s). This method loops invocation of {@link #read()}
     * method, and should only be called occasionally, to read data necessary to further
     * {@link #read(byte[])}/{@link #read(byte[], int, int)} calls.
     *
     * @return byte array containing all bytes until systems {@link System#lineSeparator() lineSeparator}
     * @throws IOException if an I/O error occurs.
     */
    public byte[] readLine() throws IOException {
        String lineSeparator = System.lineSeparator();
//        StringBuilder buffer = new StringBuilder();
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        if (lineSeparator.length() == 1) {
            byte sep = lineSeparator.getBytes()[0];
            int cur = stream.read();
            while (cur != -1 && cur != sep) {
//                buffer.append(((char) cur));
                buf.write(cur);
                cur = stream.read();
            }
        } else if (lineSeparator.length() == 2) {
            byte sep0 = lineSeparator.getBytes()[0];
            byte sep1 = lineSeparator.getBytes()[1];
            int prev = -1;
            int cur = stream.read();
            while (cur != -1) {
//                buffer.append((char) cur);
                int temp = cur;
                prev = cur;
                cur = stream.read();
                if (!(prev == sep0 && cur == sep1)) buf.write(temp);
                else break;
            }

//            buffer.deleteCharAt(buffer.length() - 1);
        } else {
            throw new Error("Unknown lineSeparator sequence");
        }
//        return buffer.toString().getBytes();
        return buf.toByteArray();
    }

    @Override
    public int read() throws IOException {
        return stream.read();
    }

    @Override
    public int read(@NotNull byte[] b) throws IOException {
        return stream.read(b);
    }

    @Override
    public int read(@NotNull byte[] b, int off, int len) throws IOException {
        return stream.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return stream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return stream.available();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    @Override
    public void mark(int readlimit) {
        stream.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        stream.reset();
    }

    @Override
    public boolean markSupported() {
        return stream.markSupported();
    }
}