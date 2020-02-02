package edu.jeznach.po2.gui;

import edu.jeznach.po2.util.CollectionAssembler;
import edu.jeznach.po2.util.Pair;
import edu.jeznach.po2.util.TriFunction;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Used for sending notifications for user.
 * {@code NotificationSender} can run in two modes - <i>Console</i> or <i>Console & SystemTray</i>.
 * Used mode is solely dependent on {@link SystemTray} availability - if while constructing
 * a {@code NotificationSender} object {@link SystemTray#isSupported()} returnes {@code false},
 * then object is launched in <i>Console</i> mode.
 * <p>Every instance of NotificationSender will create its own
 * S{@link TrayIcon}, an by default this icon will be there until application is terminated.
 * If during execution created instance is no longer needed, {@link #disposeTrayIcon()} should
 * be called, to remove icon from{@link SystemTray}.
 * <br><br>
 * <p>All public method of this class are thread-safe.
 */
public class NotificationSender {

    private SystemTray tray;
    private TrayIcon trayIcon;
    private TriFunction<String, String, TrayIcon.MessageType, Void> currentSender;

    private enum IconName {
        S, I, W, E
    }
    private static Map<IconName, String> icons = CollectionAssembler.map(IconName.class, String.class,
                                                                  Pair.of(IconName.S, "✅"),
                                                                  Pair.of(IconName.I, "ℹ"),
                                                                  Pair.of(IconName.W, "⚠"),
                                                                  Pair.of(IconName.E, "🛑"));

    /**
     * Creates a new instance of {@code NotificationSender} and sets its mode according to
     * {@link SystemTray#isSupported()} result.
     * @param trayIconImage the image that will be used as {@link TrayIcon} icon
     * @param trayIconToolTip the tooltip that will be displayed bu {@link TrayIcon}
     */
    public NotificationSender(@NotNull Image trayIconImage, @NotNull String trayIconToolTip) {
        if (SystemTray.isSupported()) {
            tray = SystemTray.getSystemTray();
            trayIcon = new TrayIcon(trayIconImage, trayIconToolTip);
            trayIcon.setImageAutoSize(true);
            try { tray.add(trayIcon); } catch (AWTException ignored) { }
            this.currentSender = this.sendToConsoleAndTray;
        } else {
            this.currentSender = this.sendToConsole;
        }
    }

    /**
     * Sends <i>success</i> notification.
     * @param title the title of notification
     * @param description the description of notification
     */
    public synchronized void success(String title, String description) {
        currentSender.apply(title, description, TrayIcon.MessageType.NONE);
    }

    /**
     * Sends <i>information</i> notification.
     * @param title the title of notification
     * @param description the description of notification
     */
    public synchronized void information(String title, String description) {
        currentSender.apply(title, description, TrayIcon.MessageType.INFO);
    }

    /**
     * Sends <i>warning</i> notification.
     * @param title the title of notification
     * @param description the description of notification
     */
    public synchronized void warning(String title, String description) {
        currentSender.apply(title, description, TrayIcon.MessageType.WARNING);
    }

    /**
     * Sends <i>error</i> notification.
     * @param title the title of notification
     * @param description the description of notification
     */
    public synchronized void error(String title, String description) {
        currentSender.apply(title, description, TrayIcon.MessageType.ERROR);
    }


    private TriFunction<String, String, TrayIcon.MessageType, Void> sendToConsole =
            (title, description, messageType) -> {
        switch (messageType) {
            case ERROR:
                System.err.println(formatMessage(title, description, IconName.E));
                break;
            case WARNING:
                System.err.println(formatMessage(title, description, IconName.W));
                break;
            case INFO:
                System.out.println(formatMessage(title, description, IconName.I));
                break;
            case NONE:
                System.out.println(formatMessage(title, description, IconName.S));
                break;
        }
        return null;
    };

    private TriFunction<String, String, TrayIcon.MessageType, Void> sendToConsoleAndTray =
            (title, description, messageType) -> {
        if (messageType != TrayIcon.MessageType.NONE)
            trayIcon.displayMessage(title, description, messageType);
        else
            trayIcon.displayMessage(title, description, TrayIcon.MessageType.INFO);
        sendToConsole.apply(title, description, messageType);
        return null;
    };

    private static String formatMessage(String title, String description, IconName iconName) {
        String ret = "";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        ret += dtf.format(now) + " -> {\n";
        ret += icons.getOrDefault(iconName, "❓") + " ";
        ret += title + ";\n";
        ret += description + ";\n}";
        return ret;
    }

    /**
     * Removes {@link TrayIcon} created by this {@code NotificationSender} from {@link SystemTray}
     * and switches mode to <i>Console</i>.
     * <br><br>
     * <p>Effects of this call are irreversible and it should be used primarily prior to
     * disposing this object before application finishes execution.
     */
    public synchronized void disposeTrayIcon() {
        this.tray.remove(this.trayIcon);
        this.currentSender = this.sendToConsole;
    }
}
