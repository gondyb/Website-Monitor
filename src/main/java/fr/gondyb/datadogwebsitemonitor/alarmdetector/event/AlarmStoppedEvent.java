package fr.gondyb.datadogwebsitemonitor.alarmdetector.event;

import lombok.Data;

import java.net.URI;

@Data
public class AlarmStoppedEvent {
    private double availabilityPercentage;

    private URI uri;

    public AlarmStoppedEvent(URI uri, double availabilityPercentage) {
        this.uri = uri;
        this.availabilityPercentage = availabilityPercentage;
    }
}
