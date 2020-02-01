package edu.jeznach.po2.common.log;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class Logger {

    private static File outputFile;
    private static String LOG_PATH = "server/server.log";
    private static Writer output;
    private static Yaml yaml;

    private static String I = "ℹ";
    private static String W = "⚠";
    private static String E = "🚫";

    static { init(); }

    private static void init() {
        try {
            outputFile = new File(LOG_PATH);
            if (outputFile.createNewFile()) {
                System.out.println("Created log file: at " + outputFile.getAbsolutePath());
            }
            output = new FileWriter(outputFile);
            yaml = new Yaml();
        } catch (IOException e) {
            System.err.println("Fatal error: cannot initialize log file at " + outputFile.getAbsolutePath());
        }
    }

//    public void msg(Entry messageObject) {
//
//        String produce = yaml.dump(messageObject);
//    }

}
