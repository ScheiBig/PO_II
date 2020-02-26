package edu.jeznach.po2.common.gui;

import edu.jeznach.po2.common.util.GuiComponents;
import org.apache.batik.transcoder.TranscoderException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

import static javax.swing.SpringLayout.*;

public class OperationLabel extends JPanel {

    private static final String upIcon = "/edu/jeznach/po2/cloud-upload.svg";
    private static final String downIcon = "/edu/jeznach/po2/cloud-download.svg";
    private static final String idleIcon = "/edu/jeznach/po2/cloud-idle.svg";
    private static final String crashIcon = "/edu/jeznach/po2/cloud-crash.svg";

    @SuppressWarnings("FieldCanBeLocal")
    private SpringLayout layout;
    private JLabel icon;
    private JLabel title;
    private JLabel description;

    public OperationLabel() {
        this.setLayout(this.layout = new SpringLayout());

        this.add(this.icon = new JLabel());
        GuiComponents.setExactSize(this.icon, new Dimension(32, 32));

        this.add(this.title = new JLabel());
        GuiComponents.setExactSize(this.title, new Dimension(300, 32));
        this.title.setForeground(Color.BLACK);

        this.add(this.description = new JLabel());
        GuiComponents.setExactSize(this.description, new Dimension(320, 32));
        this.description.setForeground(Color.DARK_GRAY);

        this.layout.putConstraint(WEST, this.icon, 2, WEST, this);
        this.layout.putConstraint(NORTH, this.icon, 2, NORTH, this);
        this.layout.putConstraint(EAST, this.title, 2, EAST, this);
        this.layout.putConstraint(NORTH, this.title, 2, NORTH, this);
        this.layout.putConstraint(WEST, this.description, 2, WEST, this);
        this.layout.putConstraint(NORTH, this.description, 2, SOUTH, this.icon);
        this.layout.putConstraint(EAST, this.description, 3, EAST, this);
        this.layout.putConstraint(SOUTH, this.description, 3, SOUTH, this);

        this.idle();
//        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//        JPanel up = new JPanel();
//        up.setLayout(new BoxLayout(up, BoxLayout.X_AXIS));
//        this.add(up);
//        this.icon = new JLabel();
//        this.icon.setPreferredSize(new Dimension(32, 32));
//        try {
//            this.icon.setIcon(new SVGIcon(idleIcon, 32, 32));
//        } catch (TranscoderException e) {
//            e.printStackTrace();
//        }
//        up.add(icon);
//        up.add(Box.createHorizontalGlue());
//        this.title = new JLabel();
//        this.title.setForeground(Color.BLACK);
//        this.title.setPreferredSize(new Dimension(300, 32));
//        up.add(title);
//        this.description = new JLabel();
//        this.description.setForeground(Color.DARK_GRAY);
//        this.description.setPreferredSize(new Dimension(340, 80));
//        this.add(description);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(340, 70);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(340, 70);
    }

    public void up(@NotNull String title, @NotNull String description) {
        try {
            this.icon.setIcon(new SVGIcon(upIcon, 32, 32));
        } catch (TranscoderException e) {
            e.printStackTrace();
        }
        text(title, description);
        revalidate();
    }

    public void down(@NotNull String title, @NotNull String description) {
        try {
            this.icon.setIcon(new SVGIcon(downIcon, 32, 32));
        } catch (TranscoderException e) {
            e.printStackTrace();
        }
        text(title, description);
        revalidate();
    }

    public void idle(@NotNull String title, @NotNull String description) {
        try {
            this.icon.setIcon(new SVGIcon(idleIcon, 32, 32));
        } catch (TranscoderException e) {
            e.printStackTrace();
        }
        text(title, description);
        revalidate();
    }

    public void idle() {
        this.idle("Waiting", "...");
    }

    private void text(@NotNull String title, @NotNull String description) {
        this.title.setText(title);
        this.description.setText("<html>" + description + "</html>");
    }

    public void crash() {
        try {
            this.icon.setIcon(new SVGIcon(crashIcon, 32, 32));
        } catch (TranscoderException e) {
            e.printStackTrace();
        }
        text("Fatal error", "Connection dead on Thread, advised to check logs and/or restart application");
        revalidate();
    }
}
