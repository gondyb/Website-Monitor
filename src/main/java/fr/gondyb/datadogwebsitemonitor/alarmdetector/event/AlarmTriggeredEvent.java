package fr.gondyb.datadogwebsitemonitor.alarmdetector.event;

import lombok.Data;

import java.net.URI;

@Data
public class AlarmTriggeredEvent {
    private double availabilityPercentage;

    private URI uri;

    public AlarmTriggeredEvent(URI uri, double availabilityPercentage) {
        this.uri = uri;
        this.availabilityPercentage = availabilityPercentage;
    }
}
