package edu.jeznach.po2.client.gui;

import edu.jeznach.po2.common.configuration.Configuration;
import edu.jeznach.po2.common.log.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * {@code NotificationSender} that is preconfigured for client
 * @see edu.jeznach.po2.common.gui.NotificationSender
 */
public class NotificationSender extends edu.jeznach.po2.common.gui.NotificationSender {

    /**
     * Creates a new instance of {@code NotificationSender} and sets its mode according to
     * {@link SystemTray#isSupported()} result.
     * @param log the log that should be used to save notifications
     */
    public NotificationSender(@Nullable Log log) {
        super(new ImageIcon(Configuration.CLIENT_ICON_PATH).getImage(), "PO_II Client", log);
    }

    /**
     * Creates a new instance of {@code NotificationSender} and sets its mode according to
     * {@link SystemTray#isSupported()} result.
     * @param log the log that should be used to save notifications
     */
    public NotificationSender(@NotNull String trayIconToolTip, @Nullable Log log) {
        super(new ImageIcon(Configuration.CLIENT_ICON_PATH).getImage(), "PO_II Client: " + trayIconToolTip, log);
    }
}
