package fr.gondyb.datadog.website.monitor.watchdog.event;

import lombok.Getter;

import java.net.URI;

/**
 * This immutable class is an event informing that the website with provided URI is down.
 */
@Getter
public class WebsiteDownEvent {
    /**
     * The URI of the down website.
     */
    private final URI uri;

    /**
     * Class constructor
     *
     * @param uri The URI of the down website.
     */
    public WebsiteDownEvent(URI uri) {
        this.uri = uri;
    }
}
