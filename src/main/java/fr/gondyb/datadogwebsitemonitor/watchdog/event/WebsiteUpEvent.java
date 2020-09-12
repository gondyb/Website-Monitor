package fr.gondyb.datadogwebsitemonitor.watchdog.event;

import lombok.Getter;

import java.net.URI;

@Getter
public class WebsiteUpEvent {
    private final long responseTime;

    private final int responseCode;

    private final URI uri;

    public WebsiteUpEvent(long responseTime, int responseCode, URI uri) {
        this.responseTime = responseTime;
        this.responseCode = responseCode;
        this.uri = uri;
    }
}
