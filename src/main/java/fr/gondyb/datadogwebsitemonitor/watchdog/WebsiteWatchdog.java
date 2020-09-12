package fr.gondyb.datadogwebsitemonitor.watchdog;

import com.google.common.eventbus.EventBus;

import java.net.URI;
import java.util.Timer;

/**
 * This class periodically triggers the {@link CheckWebsiteTask} to check a website availability.
 */
public class WebsiteWatchdog {

    /**
     * The website URI.
     */
    private final URI websiteUrl;

    /**
     * The interval for which the timer should check availability.
     */
    private final long interval;

    /**
     * The global EventBus
     */
    private final EventBus eventBus;

    /**
     * The timer periodically calling the {@link CheckWebsiteTask}
     */
    private final Timer timer;

    /**
     * Class constructor
     *
     * @param websiteUrl The website URI
     * @param interval   The interval for which the timer should check availability
     * @param eventBus   The global EventBus
     */
    public WebsiteWatchdog(URI websiteUrl, long interval, EventBus eventBus) {
        this.websiteUrl = websiteUrl;
        this.interval = interval;
        this.eventBus = eventBus;

        timer = new Timer();
    }

    /**
     * This function starts the periodic task.
     */
    public void start() {
        timer.scheduleAtFixedRate(
                new CheckWebsiteTask(websiteUrl, interval, eventBus),
                0,
                interval
        );
    }
}
