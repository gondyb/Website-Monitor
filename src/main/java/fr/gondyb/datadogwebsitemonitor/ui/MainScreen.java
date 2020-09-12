package fr.gondyb.datadogwebsitemonitor.ui;

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
import fr.gondyb.datadogwebsitemonitor.alarm.event.AlarmStoppedEvent;
import fr.gondyb.datadogwebsitemonitor.alarm.event.AlarmTriggeredEvent;
import fr.gondyb.datadogwebsitemonitor.statistics.event.StatisticsUpdatedEvent;

import java.io.IOException;

public class MainScreen {

    private final MainWindow mainWindow;

    public MainScreen(EventBus eventBus) {
        mainWindow =  new MainWindow(eventBus);
    }

    public void start() throws IOException {
        Terminal terminal = new DefaultTerminalFactory().createTerminal();
        try (Screen screen = new TerminalScreen(terminal)) {
            screen.setCursorPosition(null);
            screen.startScreen();
            terminal.addResizeListener((terminal1, terminalSize) -> mainWindow.onTerminalResize(terminalSize));
            mainWindow.onTerminalResize(screen.getTerminalSize());
            MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(),
                    null, new EmptySpace(TextColor.ANSI.BLACK));

            gui.addWindowAndWait(mainWindow);
        } finally {
            terminal.close();
        }
    }

    @Subscribe
    public void handleAlarmTriggeredEvent(AlarmTriggeredEvent event) {
        mainWindow.onAlarmTriggeredEvent(event);
    }

    @Subscribe
    public void handleAlarmStoppedEvent(AlarmStoppedEvent event) {
        mainWindow.onAlarmStoppedEvent(event);
    }

    @Subscribe
    public void handlerStatisticsUpdated(StatisticsUpdatedEvent event) {
        mainWindow.onStatisticsUpdatedEvent(event);
    }

}
