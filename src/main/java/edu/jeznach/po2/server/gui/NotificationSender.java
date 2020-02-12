package edu.jeznach.po2.server.gui;

import edu.jeznach.po2.common.configuration.Configuration;
import edu.jeznach.po2.common.log.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * {@code NotificationSender} that is preconfigured for server
 * @see edu.jeznach.po2.common.gui.NotificationSender
 */
public class NotificationSender extends edu.jeznach.po2.common.gui.NotificationSender {

    /**
     * Creates a new instance of {@code NotificationSender} and sets its mode according to
     * {@link SystemTray#isSupported()} result.
     * @param log the log that should be used to save notifications
     */
    public NotificationSender(@Nullable Log log) {
        super(new ImageIcon(Configuration.SERVER_ICON_PATH).getImage(), "PO_II Server", log);
    }

    /**
     * Creates a new instance of {@code NotificationSender} and sets its mode according to
     * {@link SystemTray#isSupported()} result.
     * @param trayIconToolTip the string to append to preconfigured tooltip
     * @param log the log that should be used to save notifications
     */
    public NotificationSender(@NotNull String trayIconToolTip, @Nullable Log log) {
        super(new ImageIcon(Configuration.SERVER_ICON_PATH).getImage(), "PO_II Server: " + trayIconToolTip, log);
    }
}
