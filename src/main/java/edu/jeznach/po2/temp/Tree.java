package edu.jeznach.po2.temp;

import edu.jeznach.po2.client.file.ClientFileMapper;
import edu.jeznach.po2.client.file.ClientMapping;
import edu.jeznach.po2.common.file.SharedFileMapping;
import edu.jeznach.po2.common.gui.FileTree;
import edu.jeznach.po2.server.file.DriveFileMapper;
import edu.jeznach.po2.server.file.DriveMapping;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

public class Tree {

    public static void main(String[] args) throws IOException {

        JFrame frame = new JFrame("FileTree");
        frame.setForeground(Color.black);
        frame.setBackground(Color.lightGray);
        Container cp = frame.getContentPane();

        DriveMapping structure = DriveFileMapper.provider.loadStructure(new File("C:\\GitHub\\PO_II\\serversA\\drive\\drive.yaml"));

        List<SharedFileMapping> files = structure.getUsers().stream()
                .map(u -> u.getFiles().stream()
                           .map(f -> new SharedFileMapping(f, u.getUsername()))
                           .collect(Collectors.toList()))
                .flatMap(List::stream)
                .collect(Collectors.toList())
;
        cp.add(new FileTree(files, "client", FileTree.Type.driveDirectory, null));

        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
