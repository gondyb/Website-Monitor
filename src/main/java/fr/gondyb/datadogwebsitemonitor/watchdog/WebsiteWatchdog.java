package fr.gondyb.datadogwebsitemonitor.watchdog;

import com.google.common.eventbus.EventBus;
import lombok.Data;

import java.net.URI;
import java.util.Timer;

@Data
public class WebsiteWatchdog {

    private URI websiteUrl;

    private long interval;

    private EventBus eventBus;

    private Timer timer;

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
