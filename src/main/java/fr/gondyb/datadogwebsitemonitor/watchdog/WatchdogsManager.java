package fr.gondyb.datadogwebsitemonitor.watchdog;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import fr.gondyb.datadogwebsitemonitor.ui.MainWindow;
import fr.gondyb.datadogwebsitemonitor.ui.event.StartMonitorEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * This class manages the different {@link WebsiteWatchdog} that monitor websites.
 */
public class WatchdogsManager {
    /**
     * The list of currently started {@link WebsiteWatchdog}.
     */
    private final List<WebsiteWatchdog> watchdogs;

    /**
     * The global EventBus.
     */
    private final EventBus eventBus;

    /**
     * Class Constructor
     *
     * @param eventBus The global EventBus
     */
    public WatchdogsManager(EventBus eventBus) {
        this.eventBus = eventBus;
        this.watchdogs = new ArrayList<>();
    }

    /**
     * This function creates a new {@link WebsiteWatchdog} from a {@link StartMonitorEvent}.
     * It then starts the WatchDog.
     *
     * @param event The event to create the watchdog from.
     * @see MainWindow
     */
    @Subscribe
    public void addWatchdog(StartMonitorEvent event) {
        WebsiteWatchdog watchdog = new WebsiteWatchdog(
                event.getUri(),
                event.getDelay(),
                eventBus
        );

        watchdog.start();

        watchdogs.add(watchdog);
    }
}
