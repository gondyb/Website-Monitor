package fr.gondyb.datadog.website.monitor.ui;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import fr.gondyb.datadog.website.monitor.alarm.event.AlarmStoppedEvent;
import fr.gondyb.datadog.website.monitor.alarm.event.AlarmTriggeredEvent;
import fr.gondyb.datadog.website.monitor.statistics.event.StatisticsUpdatedEvent;

import java.io.IOException;

/**
 * This class handles the main UI. The library used for this UI is Lanterna.
 * It subscribes to different events to display monitoring statistics and alarms.
 */
public class MainScreen {

    /**
     * The main screen window.
     */
    private final MainWindow mainWindow;

    /**
     * Class Constructor
     *
     * @param eventBus The main EventBus
     */
    public MainScreen(EventBus eventBus) {
        mainWindow = new MainWindow(eventBus);
    }

    /**
     * This function starts the UI.
     *
     * @throws IOException When the program cannot create a terminal.
     */
    public void start() throws IOException {
        try (Terminal terminal = new DefaultTerminalFactory().createTerminal(); Screen screen = new TerminalScreen(terminal)) {
            screen.setCursorPosition(null);
            screen.startScreen();
            terminal.addResizeListener((terminal1, terminalSize) -> mainWindow.onTerminalResize(terminalSize));
            mainWindow.onTerminalResize(screen.getTerminalSize());
            MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(),
                    null, new EmptySpace(TextColor.ANSI.BLACK));

            gui.addWindowAndWait(mainWindow);
        }
    }

    /**
     * This function transmits an {@link AlarmTriggeredEvent} to the main window, to add an Alarm to be displayed.
     *
     * @param event The event to be transmitted
     */
    @Subscribe
    public void handleAlarmTriggeredEvent(AlarmTriggeredEvent event) {
        mainWindow.onAlarmTriggeredEvent(event);
    }

    /**
     * This function transmits an {@link AlarmStoppedEvent} to the main window, to add an Alarm dismissed to be
     * displayed.
     *
     * @param event The event to be transmitted
     */
    @Subscribe
    public void handleAlarmStoppedEvent(AlarmStoppedEvent event) {
        mainWindow.onAlarmStoppedEvent(event);
    }

    /**
     * This function transmits an {@link StatisticsUpdatedEvent} to the main window, to update monitoring statistics.
     *
     * @param event The event to be transmitted
     */
    @Subscribe
    public void handlerStatisticsUpdated(StatisticsUpdatedEvent event) {
        mainWindow.onStatisticsUpdatedEvent(event);
    }

}
