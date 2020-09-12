package fr.gondyb.datadog.website.monitor;

import com.google.common.eventbus.EventBus;
import fr.gondyb.datadog.website.monitor.alarm.AlarmDetector;
import fr.gondyb.datadog.website.monitor.alarm.AvailabilityCalculator;
import fr.gondyb.datadog.website.monitor.statistics.StatisticsManager;
import fr.gondyb.datadog.website.monitor.ui.MainScreen;
import fr.gondyb.datadog.website.monitor.watchdog.WatchdogsManager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * This class handles the application start, and the initialisation of different modules.
 */
public class Main {
    public static void main(String[] args) throws IOException {

        int AVAILABILITY_THRESHOLD = 80; // Default availability threshold
        long AVAILABILITY_PERIOD = TimeUnit.MINUTES.toMillis(2); // Default availability check duration

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

        mainScreen.start(); // Start the UI
        System.exit(0); // Exit everything once its done
    }
}
