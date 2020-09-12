package fr.gondyb.datadogwebsitemonitor.alarm.event;

import lombok.Getter;

import java.net.URI;

@Getter
public class AlarmTriggeredEvent {
    private final double availabilityPercentage;

    private final URI uri;

    public AlarmTriggeredEvent(URI uri, double availabilityPercentage) {
        this.uri = uri;
        this.availabilityPercentage = availabilityPercentage;
    }
}
