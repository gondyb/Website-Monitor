package fr.gondyb.datadogwebsitemonitor.watchdog.event;

import lombok.Data;

import java.net.URI;

@Data
public class WebsiteUpEvent {
    private long responseTime;

    private int responseCode;

    private URI uri;

    public WebsiteUpEvent(long responseTime, int responseCode, URI uri) {
        this.responseTime = responseTime;
        this.responseCode = responseCode;
        this.uri = uri;
    }
}
