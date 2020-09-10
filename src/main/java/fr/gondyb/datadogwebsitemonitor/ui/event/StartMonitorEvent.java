package fr.gondyb.datadogwebsitemonitor.ui.event;

import lombok.Data;

import java.net.URI;

@Data
public class StartMonitorEvent {
    private URI uri;

    private long delay;

    public StartMonitorEvent(URI uri, long delay) {
        this.uri = uri;
        this.delay = delay;
    }
}
