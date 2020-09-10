package fr.gondyb.datadogwebsitemonitor;

import com.google.common.eventbus.EventBus;
import fr.gondyb.datadogwebsitemonitor.alarmdetector.AlarmDetector;
import fr.gondyb.datadogwebsitemonitor.ui.MainWindow;
import fr.gondyb.datadogwebsitemonitor.ui.event.StartMonitorEvent;
import fr.gondyb.datadogwebsitemonitor.watchdog.WatchdogsManager;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws IOException {
        EventBus eventBus = new EventBus("default");
        WatchdogsManager watchdogsManager = new WatchdogsManager(eventBus);
        eventBus.register(watchdogsManager);

        StartMonitorEvent e1 = new StartMonitorEvent(
                URI.create("http://google.fr"),
                TimeUnit.SECONDS.toMillis(2)
        );

        eventBus.post(e1);

        StartMonitorEvent e2 = new StartMonitorEvent(
                URI.create("http://localhost:5000"),
                TimeUnit.SECONDS.toMillis(2)
        );

        eventBus.post(e2);

        AlarmDetector detector = new AlarmDetector(eventBus);
        eventBus.register(detector);

        MainWindow mainWindow = new MainWindow(eventBus);
        mainWindow.mainWindow();
    }
}
