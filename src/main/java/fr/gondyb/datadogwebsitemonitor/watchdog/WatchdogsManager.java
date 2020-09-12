package fr.gondyb.datadogwebsitemonitor.watchdog;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import fr.gondyb.datadogwebsitemonitor.ui.event.StartMonitorEvent;

import java.util.ArrayList;
import java.util.List;

public class WatchdogsManager {
    private final List<WebsiteWatchdog> watchdogs;

    private final EventBus eventBus;

    public WatchdogsManager(EventBus eventBus) {
        this.eventBus = eventBus;
        this.watchdogs = new ArrayList<>();
    }

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
