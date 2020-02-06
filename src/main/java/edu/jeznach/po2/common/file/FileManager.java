package edu.jeznach.po2.common.file;

import edu.jeznach.po2.common.configuration.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

public class FileManager {


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
    public static String getChecksum(File file) throws Exception {
        byte[] b = createChecksum(file);
        StringBuilder result = new StringBuilder();

        for (byte value : b) {
            result.append(Integer.toString((value & 0xff) + 0x100, 16)
                                 .substring(1));
        }
        return result.toString();
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
}
