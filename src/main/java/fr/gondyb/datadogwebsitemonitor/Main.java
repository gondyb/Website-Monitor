package fr.gondyb.datadogwebsitemonitor;

import com.google.common.eventbus.EventBus;
import fr.gondyb.datadogwebsitemonitor.alarm.AlarmDetector;
import fr.gondyb.datadogwebsitemonitor.statistics.StatisticsManager;
import fr.gondyb.datadogwebsitemonitor.ui.MainScreen;
import fr.gondyb.datadogwebsitemonitor.watchdog.WatchdogsManager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws IOException {
        EventBus eventBus = new EventBus("default");

        WatchdogsManager watchdogsManager = new WatchdogsManager(eventBus);
        eventBus.register(watchdogsManager);

        AlarmDetector detector = new AlarmDetector((int) TimeUnit.MINUTES.toSeconds(2), eventBus);
        eventBus.register(detector);

        StatisticsManager statisticsManager = new StatisticsManager(eventBus);
        eventBus.register(statisticsManager);

        MainScreen mainScreen = new MainScreen(eventBus);
        eventBus.register(mainScreen);

        mainScreen.start();
    }
}
