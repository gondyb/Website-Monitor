package fr.gondyb.datadogwebsitemonitor.watchdog;

import com.google.common.eventbus.EventBus;

import java.net.URI;
import java.util.Timer;

public class WebsiteWatchdog {

    private final URI websiteUrl;

    private final long interval;

    private final EventBus eventBus;

    private final Timer timer;

    public WebsiteWatchdog(URI websiteUrl, long interval, EventBus eventBus) {
        this.websiteUrl = websiteUrl;
        this.interval = interval;
        this.eventBus = eventBus;

        timer = new Timer();
    }

    public void start() {
        timer.scheduleAtFixedRate(
                new CheckWebsiteTask(websiteUrl, interval, eventBus),
                0,
                interval
        );
    }
}
