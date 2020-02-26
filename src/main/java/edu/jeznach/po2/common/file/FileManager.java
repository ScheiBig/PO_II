package edu.jeznach.po2.common.file;

import edu.jeznach.po2.common.configuration.Configuration;
import edu.jeznach.po2.common.gui.OperationLabel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;
import java.util.Random;

public class FileManager {

    private static Random random = new Random();

    /**
     * Calculates checksum of {@code file}.
     * <br><br>
     * <p>This method should only be used in application startup, at it reads whole file
     * without doing anything with its contents except calculating checksum. When {@code file}
     * is going to be send/received, {@link DigestInputStream} or {@link DigestOutputStream}
     * should be used, as it can calculate checksum while processing file.
     * @param file the file that checksum should be calculated of
     * @return {@link String} containing checksum in hex format
     * @throws Exception when exception is thrown while calculating checksum
     */
    public static @NotNull String getChecksum(File file) throws Exception {
        byte[] b = createChecksum(file);
        StringBuilder result = new StringBuilder();

        for (byte value : b) {
            result.append(Integer.toString((value & 0xff) + 0x100, 16)
                                 .substring(1));
        }
        return result.toString();
    }

    public static @NotNull String getChecksum(byte[] bytes) {
        StringBuilder result = new StringBuilder();

        for (byte value : bytes) {
            result.append(Integer.toString((value & 0xff) + 0x100, 16)
                                 .substring(1));
        }
        return result.toString();
    }

    @SuppressWarnings("DuplicateThrows")
    public static String receiveFile(OperationLabel label,
                                   String name,
                                   File file,
                                   InputStream byteIn,
                                   int size,
                                   boolean slow)
            throws FileNotFoundException, IOException, NoSuchAlgorithmException {
        OutputStream fileStream;

        //noinspection ResultOfMethodCallIgnored
        file.getParentFile().mkdirs();
        //noinspection ResultOfMethodCallIgnored
        file.createNewFile();
        fileStream = new FileOutputStream(file);
        int read = 0;
        int left = size;
        int readBytes = 0;
        short s = Configuration.BUFFER_SIZE;
        byte[] buffer = new byte[s];
        MessageDigest md;
        md = MessageDigest.getInstance(Configuration.CHECKSUM_ALGORITHM);
        DigestInputStream dis = new DigestInputStream(byteIn, md);
        Optional<OperationLabel> l = Optional.ofNullable(label);
        if (size > 0) l.ifPresent(ol -> SwingUtilities.invokeLater(() -> ol.down(name  + " 0%", file.getAbsolutePath())));
        while ((readBytes = dis.read(buffer, 0, Math.min(s, left))) > 0) {
            read += readBytes;
            left -= readBytes;
            fileStream.write(buffer, 0, readBytes);
            final double cur = 100.0 *  read / size;
            l.ifPresent(ol -> SwingUtilities.invokeLater(() -> ol.up(name + String.format(" %.2f", cur) + "%", file.getAbsolutePath())));
            if (slow) {
                try {
                    Thread.sleep(random.nextInt((
                                                        Configuration.ARTIFICIAL_INPUT_LAG.value() -
                                                        Configuration.ARTIFICIAL_INPUT_LAG.key()) + 1) + Configuration.ARTIFICIAL_INPUT_LAG.key());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        fileStream.close();
        return FileManager.getChecksum(md.digest());
    }

    public static String receiveString(InputStream byteIn,
                                       int size,
                                       boolean slow)
            throws IOException {
        OutputStream stream;

        stream = new ByteArrayOutputStream();
        int left = size;
        int readBytes = 0;
        short s = Configuration.BUFFER_SIZE;
        byte[] buffer = new byte[s];
        while ((readBytes = byteIn.read(buffer, 0, Math.min(s, left))) > 0) {
            left -= readBytes;
            stream.write(buffer, 0, readBytes);
            if (slow) {
                try {
                    Thread.sleep(random.nextInt((
                                                        Configuration.ARTIFICIAL_INPUT_LAG.value() -
                                                        Configuration.ARTIFICIAL_INPUT_LAG.key()) + 1) + Configuration.ARTIFICIAL_INPUT_LAG.key());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        String ret = stream.toString();
        stream.close();
        return ret;
    }

    @SuppressWarnings({"DuplicateThrows"})
    public static void sendFile(OperationLabel label,
                                String name,
                                File file,
                                OutputStream byteOut,
                                int size,
                                boolean slow)
            throws FileNotFoundException, IOException, NoSuchAlgorithmException {
        InputStream fileStream;

        //noinspection ResultOfMethodCallIgnored
        file.getParentFile().mkdirs();
        //noinspection ResultOfMethodCallIgnored
        file.createNewFile();
        fileStream = new FileInputStream(file);

        String absolutePath = file.getAbsolutePath();
        String nonTempPath = absolutePath.endsWith(Configuration.TEMP_EXTENSION) ?
                absolutePath.substring(0, absolutePath.length() - Configuration.TEMP_EXTENSION.length()) :
                absolutePath;

        int read = 0;
        int readBytes = 0;
        short s = Configuration.BUFFER_SIZE;
        byte[] buffer = new byte[s];
        MessageDigest md;
        md = MessageDigest.getInstance(Configuration.CHECKSUM_ALGORITHM);
        DigestInputStream dis = new DigestInputStream(fileStream, md);
        Optional<OperationLabel> l = Optional.ofNullable(label);
        if (size > 0) l.ifPresent(ol -> SwingUtilities.invokeLater(() -> ol.up(name  + " 0%", nonTempPath)));
        while ((readBytes = dis.read(buffer)) > 0) {
            read += readBytes;
            byteOut.write(buffer, 0, readBytes);
            byteOut.flush();
            final double cur = 100.0 *  read / size;
            l.ifPresent(ol -> SwingUtilities.invokeLater(() -> ol.up(name + String.format(" %.2f", cur) + "%", nonTempPath)));
            if (slow) {
                try {
                    Thread.sleep(random.nextInt((
                            Configuration.ARTIFICIAL_INPUT_LAG.value() -
                            Configuration.ARTIFICIAL_INPUT_LAG.key()) + 1) + Configuration.ARTIFICIAL_INPUT_LAG.key());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        dis.close();
        fileStream.close();
    }

    public static void sendString(String string,
                                  OutputStream byteOut,
                                  boolean slow)
            throws IOException {
        InputStream stream;

        stream = new ByteArrayInputStream(string.getBytes());
        int size = string.length();

        int read = 0;
        int readBytes = 0;
        short s = Configuration.BUFFER_SIZE;
        byte[] buffer = new byte[s];
        while ((readBytes = stream.read(buffer)) > 0) {
            read += readBytes;
            byteOut.write(buffer, 0, readBytes);
            byteOut.flush();
            final double cur = 100.0 *  read / size;
            if (slow) {
                try {
                    Thread.sleep(random.nextInt((
                                                        Configuration.ARTIFICIAL_INPUT_LAG.value() -
                                                        Configuration.ARTIFICIAL_INPUT_LAG.key()) + 1) + Configuration.ARTIFICIAL_INPUT_LAG.key());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        stream.close();
    }

    /** Read the object from a Base64 string. */
    public static Object fromString(String s) throws IOException, ClassNotFoundException {
        byte[] data = Base64.getDecoder().decode(s);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream( data));
        Object o  = ois.readObject();
        ois.close();
        return o;
    }

    /** Write the object to a Base64 string. */
    public static String toString( Serializable o ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public static String getRelativePath(String from, String path) {
        Path root = Paths.get(from);
        Path child = Paths.get(path);
        return root.relativize(child).toString();
    }

    private static byte[] createChecksum(File file) throws Exception {
        InputStream fis =  new FileInputStream(file);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance(Configuration.CHECKSUM_ALGORITHM);
        int numRead;
        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }

//    public static class Cleanup extends SimpleFileVisitor<Path> {
//
//        @Override
//        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//            if (attrs.isRegularFile()) {
//                if (file.toString().endsWith(Configuration.TEMP_EXTENSION)) {
//                    Files.delete(file);
//                }
//            }
//        }
//    }
}
