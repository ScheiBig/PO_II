package edu.jeznach.po2.temp;

import edu.jeznach.po2.common.file.FileManager;
import edu.jeznach.po2.common.file.FileObserver;
import edu.jeznach.po2.common.gui.NotificationSender;
import edu.jeznach.po2.common.log.Log;
import edu.jeznach.po2.common.util.Pair;
import edu.jeznach.po2.server.file.DriveFileMapper;
import edu.jeznach.po2.server.file.DriveMapping;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws Exception {

//        File logFile = new File("C:/log.txt");
//        logFile.createNewFile();

//        try (Log log = new Log(logFile)) {
        try (Log log = new Log(null)) {
            log.fileCreated("test",
                            new File("src/main/resources/edu/jeznach/po2/conf.yml"),
                            (long) (69 * 1024 * 1024));
            log.fileDeleted("test",
                            new File("src/main/resources/edu/jeznach/po2/conf.yml"),
                            (long) (69 * 1024 * 1024));
            log.fileUpdated("test",
                            new File("src/main/resources/edu/jeznach/po2/conf.yml"),
                            ((long) (900)),
                            (long) (69 * 1024 * 1024));
            log.fileRenamed("test",
                            new File("src/main/resources/edu/jeznach/po2/conf.yml"),
                            new File("src/main/resources/edu/jeznach/po2/conf.yml").getAbsolutePath() + "69");
            log.fileUnshared("test",
                             new File("src/main/resources/edu/jeznach/po2/conf.yml"),
                             "experiment");
            NotificationSender sender = new NotificationSender(
                    new ImageIcon(Main.class.getResource("/edu/jeznach/po2/computer.png")).getImage(),
                    "Main",
                    log);
            sender.success("Hello there", "General Kenobiii!");

            Pair<DriveMapping, Boolean> structure = DriveFileMapper.provider.createStructure(new File("drive.yaml"),
                                                                                             new DriveMapping.InitParams(
                                                                                                     new File(
                                                                                                             "serversA/drive"),
                                                                                                     "drive.log"));
            DriveFileMapper mapper = new DriveFileMapper(
                    structure.key()
                    , new File("serversA/drive/drive.yaml"));
            System.out.println(structure.value());
//
////        System.out.println(new Yaml().dump(mapper.getMapping()));
            File f = new File("C:/GitHub/PO_II/serversA/drive/Examination/bek.txt");
            f.getParentFile()
             .mkdirs();
            Writer w = new FileWriter(f);
            w.write("Hello world!");
            w.flush();
            System.out.println(
                    mapper.attachFile(f, FileManager.getChecksum(f), "Examination")
            );
            File g = new File("C:/Github/PO_II/serversA/drive/Experiment/w/del.tt");
            System.out.println(
                    mapper.detachFile(g, "Experiment")
            );
            w.write("Goodbye world, im tired so i'm gonna eat dinner and take a nap");
            w.close();
            System.out.println(
                    mapper.updateFile(f, FileManager.getChecksum(f), "Examination")
            );
            System.out.println(
                    mapper.shareFile(f, "Examination", "Test")
            );
            synchronized (Main.class) {
                Main.class.wait(5000);
            }
            System.out.println(
                    "ready"
            );
            System.out.println(
                    mapper.unshareFile(f, "Examination", "Test")
            );
            JFrame frame = new JFrame("HelloWorldSwing");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            //Add the ubiquitous "Hello World" label.
            JLabel label = new JLabel("Hello World");
            frame.getContentPane().add(label);

            //Display the window.
            frame.setSize(new Dimension(400, 400));
            frame.setVisible(true);
            FileObserver fs = new FileObserver(Paths.get("C:/Github/PO_II/serversA"), log);
            fs.start();
            new Thread() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            FileObserver.FileEvent fileEvent = fs.popEvent();
                            SwingUtilities.invokeLater(() -> {
                                label.setText(fileEvent.toString());
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();

//        File fe = new File("C:\\GitHub\\PO_II\\servers\\drive\\drivee.yaml");
//        Writer ww = new FileWriter(fe);
//        ww.write("Hello");
//        ww.flush();
//        ww.close();
        }
    }
}
