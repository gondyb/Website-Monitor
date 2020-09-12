package fr.gondyb.datadogwebsitemonitor.watchdog.event;

import lombok.Getter;

import java.net.URI;

/**
 * This immutable class is an event informing that the website with the provided URI is UP.
 */
@Getter
public class WebsiteUpEvent {
    /**
     * The response time in milliseconds for the response to arrive.
     */
    private final long responseTime;

    /**
     * The HTTP Response code of the response.
     */
    private final int responseCode;

    /**
     * The website URI.
     */
    private final URI uri;

    /**
     * Class constructor
     *
     * @param responseTime The response time in milliseconds for the response to arrive
     * @param responseCode The HTTP Response code of the response
     * @param uri          The website URI
     */
    public WebsiteUpEvent(long responseTime, int responseCode, URI uri) {
        this.responseTime = responseTime;
        this.responseCode = responseCode;
        this.uri = uri;
    }
}
