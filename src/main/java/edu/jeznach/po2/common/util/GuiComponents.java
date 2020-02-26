package edu.jeznach.po2.common.util;

import java.awt.*;

public class GuiComponents {

    public static void setExactSize(Component c, Dimension size) {
        c.setMinimumSize(size);
        c.setPreferredSize(size);
        c.setMaximumSize(size);
    }

    public static Image getImageFromClasspath(String classpathLocation) {
        return Toolkit.getDefaultToolkit().getImage(GuiComponents.class.getResource(classpathLocation));
    }
}
