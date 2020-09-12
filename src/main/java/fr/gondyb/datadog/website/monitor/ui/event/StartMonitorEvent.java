package fr.gondyb.datadog.website.monitor.ui.event;

import lombok.Getter;

import java.net.URI;

/**
 * This immutable class is an event informing that a website needs to be monitored.
 */
@Getter
public class StartMonitorEvent {
    /**
     * The URI of the website to monitor.
     */
    private final URI uri;

    /**
     * The delay in milliseconds at which the website will be checked. (Ex: every 1000ms).
     */
    private final long delay;

    public StartMonitorEvent(URI uri, long delay) {
        this.uri = uri;
        this.delay = delay;
    }
}
