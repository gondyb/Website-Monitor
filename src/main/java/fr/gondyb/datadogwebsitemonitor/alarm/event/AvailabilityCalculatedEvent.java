package fr.gondyb.datadogwebsitemonitor.alarm.event;

import lombok.Getter;

import java.net.URI;

@Getter
public class AvailabilityCalculatedEvent {
    private final URI uri;

    private final double availability;

    public AvailabilityCalculatedEvent(URI uri, double availability) {
        this.uri = uri;
        this.availability = availability;
    }
}
