package fr.gondyb.datadogwebsitemonitor.watchdog.event;

import lombok.Getter;

import java.net.URI;

@Getter
public class WebsiteDownEvent {
    private final URI uri;

    public WebsiteDownEvent(URI uri) {
        this.uri = uri;
    }
}
