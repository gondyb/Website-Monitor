package fr.gondyb.datadogwebsitemonitor.alarm.event;

import lombok.Getter;

import java.net.URI;

@Getter
public class AlarmStoppedEvent {
    private final double availabilityPercentage;

    private final URI uri;

    public AlarmStoppedEvent(URI uri, double availabilityPercentage) {
        this.uri = uri;
        this.availabilityPercentage = availabilityPercentage;
    }
}
