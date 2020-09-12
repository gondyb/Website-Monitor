package fr.gondyb.datadogwebsitemonitor;

import com.google.common.eventbus.EventBus;
import fr.gondyb.datadogwebsitemonitor.alarm.AlarmDetector;
import fr.gondyb.datadogwebsitemonitor.alarm.AvailabilityCalculator;
import fr.gondyb.datadogwebsitemonitor.statistics.StatisticsManager;
import fr.gondyb.datadogwebsitemonitor.ui.MainScreen;
import fr.gondyb.datadogwebsitemonitor.watchdog.WatchdogsManager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws IOException {

        int AVAILABILITY_THRESHOLD = 80;
        long AVAILABILITY_PERIOD = TimeUnit.MINUTES.toMillis(2);

        EventBus eventBus = new EventBus("default");

        WatchdogsManager watchdogsManager = new WatchdogsManager(eventBus);
        eventBus.register(watchdogsManager);

        AvailabilityCalculator availabilityCalculator = new AvailabilityCalculator(
                AVAILABILITY_PERIOD,
                eventBus
        );
        eventBus.register(availabilityCalculator);

        AlarmDetector detector = new AlarmDetector(AVAILABILITY_THRESHOLD, eventBus);
        eventBus.register(detector);

        StatisticsManager statisticsManager = new StatisticsManager(eventBus);
        eventBus.register(statisticsManager);

        MainScreen mainScreen = new MainScreen(eventBus);
        eventBus.register(mainScreen);

        mainScreen.start();
        System.exit(0);
    }
}
