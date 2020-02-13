package edu.jeznach.po2.common.gui;

import edu.jeznach.po2.common.file.FileMapping;
import edu.jeznach.po2.common.file.SharedFileMapping;
import org.apache.batik.transcoder.TranscoderException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;

public class FileTree extends JPanel {

    public static final String directoryIcon = "/edu/jeznach/po2/folder.svg";
    public static final String fileIcon = "/edu/jeznach/po2/file.svg";
    public static final String userIcon = "/edu/jeznach/po2/user.svg";
    public static final String driveIcon = "/edu/jeznach/po2/drive.svg";
    public static final String shareIcon = "/edu/jeznach/po2/share.svg";

    @SuppressWarnings("unchecked")
    public FileTree(List<? extends FileMapping> files, String nodeName, Type mappingType, TreeSelectionListener listener) {
        setLayout(new BorderLayout());

        JTree tree;

        switch (mappingType) {
            case userDirectory:
                tree = new JTree(setUserMapping(((List<FileMapping>) files), nodeName));
                break;
            case sharedDirectory:
                tree = new JTree(setSharedMapping(((List<SharedFileMapping>) files), nodeName));
                break;
            case driveDirectory:
                tree = new JTree(setDriveMapping(((List<SharedFileMapping>) files), nodeName));
                break;
            default:
                tree = new JTree();
        }

        tree.addTreeSelectionListener(listener);
        tree.setCellRenderer(new IconLabelTreeCellRenderer());
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getViewport().add(tree);
        add(BorderLayout.CENTER, scrollPane);


    }

    DefaultMutableTreeNode setUserMapping(@NotNull List<FileMapping> files, @NotNull String username) {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new TreeIconLabelObject(userIcon, username));
        for (FileMapping fileMapping : files) {
            final String[] components = fileMapping.getPathname().split(Pattern.quote(File.separator));
            String name = components[components.length-1];
            DefaultMutableTreeNode toAddNode = rootNode;
            DefaultMutableTreeNode previousNode = rootNode;
            for (int i = 0; i < components.length-1; i++) {
                final String component = components[i];
                // noinspection unchecked
                Optional<DefaultMutableTreeNode> occurrenceOptional =
                        Collections.list((Enumeration<DefaultMutableTreeNode>) previousNode.children())
                                   .stream()
                                   .filter(n -> ((TreeIconLabelObject) n.getUserObject()).label
                                           .equals(component))
                                   .findFirst();
                if (occurrenceOptional.isPresent()) {
                    toAddNode = previousNode = occurrenceOptional.get();
                } else {
                    DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(
                            new TreeIconLabelObject(directoryIcon, component));
                    previousNode.add(newChild);
                    toAddNode = previousNode = newChild;
                }
            }
            toAddNode.add(new DefaultMutableTreeNode(new TreeIconLabelObject(fileIcon, name)));
        }
        return rootNode;
    }

    DefaultMutableTreeNode setSharedMapping(@NotNull List<SharedFileMapping> files, @NotNull String rootName) {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new TreeIconLabelObject(shareIcon, rootName));
        for (SharedFileMapping fileMapping : files) {
            final String[] components = fileMapping.getPathname().split(Pattern.quote(File.separator));
            String name = components[components.length-1];
            //noinspection UnusedAssignment
            DefaultMutableTreeNode toAddNode = rootNode;
            DefaultMutableTreeNode previousNode = rootNode;
            //noinspection unchecked
            Optional<DefaultMutableTreeNode> ownerOptional =
                    Collections.list((Enumeration<DefaultMutableTreeNode>) rootNode.children())
                               .stream()
                               .filter(n -> ((TreeIconLabelObject) n.getUserObject()).label
                                       .equals(fileMapping.getOwner()))
                               .findFirst();
            if (ownerOptional.isPresent()) {
                toAddNode = previousNode = ownerOptional.get();
            } else {
                DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(
                        new TreeIconLabelObject(userIcon, fileMapping.getOwner()));
                previousNode.add(newChild);
                toAddNode = previousNode = newChild;
            }
            for (int i = 0; i < components.length-1; i++) {
                final String component = components[i];
                // noinspection unchecked
                Optional<DefaultMutableTreeNode> occurrenceOptional =
                        Collections.list((Enumeration<DefaultMutableTreeNode>) previousNode.children())
                                   .stream()
                                   .filter(n -> ((TreeIconLabelObject) n.getUserObject()).label
                                           .equals(component))
                                   .findFirst();
                if (occurrenceOptional.isPresent()) {
                    toAddNode = previousNode = occurrenceOptional.get();
                } else {
                    DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(
                            new TreeIconLabelObject(directoryIcon, component));
                    previousNode.add(newChild);
                    toAddNode = previousNode = newChild;
                }
            }
            toAddNode.add(new DefaultMutableTreeNode(new TreeIconLabelObject(fileIcon, name)));
        }
        return rootNode;
    }

    DefaultMutableTreeNode setDriveMapping(@NotNull List<SharedFileMapping> files, @NotNull String rootName) {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new TreeIconLabelObject(driveIcon, rootName));
        for (SharedFileMapping fileMapping : files) {
            final String[] components = fileMapping.getPathname().split(Pattern.quote(File.separator));
            String name = components[components.length-1];
            //noinspection UnusedAssignment
            DefaultMutableTreeNode toAddNode = rootNode;
            DefaultMutableTreeNode previousNode = rootNode;
            //noinspection unchecked
            Optional<DefaultMutableTreeNode> ownerOptional =
                    Collections.list((Enumeration<DefaultMutableTreeNode>) rootNode.children())
                               .stream()
                               .filter(n -> ((TreeIconLabelObject) n.getUserObject()).label
                                       .equals(fileMapping.getOwner()))
                               .findFirst();
            if (ownerOptional.isPresent()) {
                toAddNode = previousNode = ownerOptional.get();
            } else {
                DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(
                        new TreeIconLabelObject(userIcon, fileMapping.getOwner()));
                previousNode.add(newChild);
                toAddNode = previousNode = newChild;
            }
            for (int i = 0; i < components.length-1; i++) {
                final String component = components[i];
                // noinspection unchecked
                Optional<DefaultMutableTreeNode> occurrenceOptional =
                        Collections.list((Enumeration<DefaultMutableTreeNode>) previousNode.children())
                                   .stream()
                                   .filter(n -> ((TreeIconLabelObject) n.getUserObject()).label
                                           .equals(component))
                                   .findFirst();
                if (occurrenceOptional.isPresent()) {
                    toAddNode = previousNode = occurrenceOptional.get();
                } else {
                    DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(
                            new TreeIconLabelObject(directoryIcon, component));
                    previousNode.add(newChild);
                    toAddNode = previousNode = newChild;
                }
            }
            toAddNode.add(new DefaultMutableTreeNode(new TreeIconLabelObject(fileIcon, name)));
        }
        return rootNode;
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(300, 580);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(300, 580);
    }

    static class TreeIconLabelObject {

        final @NotNull String imagePath;
        final @NotNull String label;

        TreeIconLabelObject(@NotNull String imagePath, @NotNull String label) {
            this.imagePath = imagePath;
            this.label = label;
        }

        @Override
        public int hashCode() {
            return Objects.hash(label);
        }
    }

    static class IconLabelTreeCellRenderer implements TreeCellRenderer {

        private JLabel label;

        IconLabelTreeCellRenderer() { this.label = new JLabel(); }

        @Override
        public Component getTreeCellRendererComponent(JTree tree,
                                                      Object value,
                                                      boolean selected,
                                                      boolean expanded,
                                                      boolean leaf,
                                                      int row,
                                                      boolean hasFocus) {
            Object o = ((DefaultMutableTreeNode) value).getUserObject();
            if (o instanceof TreeIconLabelObject) {
                try {
                    label.setIcon(new SVGIcon(((TreeIconLabelObject) o).imagePath, 16, 16));
                } catch (TranscoderException e) {
                    e.printStackTrace();
                }
                label.setText(((TreeIconLabelObject) o).label);
            }
            return label;
        }
    }

    public enum Type {
        userDirectory,
        sharedDirectory,
        driveDirectory
    }
}
