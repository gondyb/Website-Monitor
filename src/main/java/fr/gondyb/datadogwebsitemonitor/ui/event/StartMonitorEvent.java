package fr.gondyb.datadogwebsitemonitor.ui.event;

import lombok.Getter;

import java.net.URI;

@Getter
public class StartMonitorEvent {
    private final URI uri;

    private final long delay;

    public StartMonitorEvent(URI uri, long delay) {
        this.uri = uri;
        this.delay = delay;
    }
}
