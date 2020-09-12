package fr.gondyb.datadogwebsitemonitor.alarm.event;

import lombok.Data;

import java.net.URI;

@Data
public class AvailabilityCalculatedEvent {
    private URI uri;

    private double availability;

    public AvailabilityCalculatedEvent(URI uri, double availability) {
        this.uri = uri;
        this.availability = availability;
    }
}
