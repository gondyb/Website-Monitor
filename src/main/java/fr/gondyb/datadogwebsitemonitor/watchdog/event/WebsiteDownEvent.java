package fr.gondyb.datadogwebsitemonitor.watchdog.event;

import lombok.Data;

import java.net.URI;

@Data
public class WebsiteDownEvent {
    private URI uri;

    public WebsiteDownEvent(URI uri) {
        this.uri = uri;
    }
}
