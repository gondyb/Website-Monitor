package fr.gondyb.datadog.website.monitor.alarm.event;

import lombok.Getter;

import java.net.URI;

/**
 * This immutable class is an event informing that the availability alert for a website was triggered.
 */
@Getter
public class AlarmTriggeredEvent {
    /**
     * The availability percentage when the alert was triggered.
     */
    private final double availabilityPercentage;

    /**
     * The URI of the website concerned with the alert.
     */
    private final URI uri;

    /**
     * Class constructor
     *
     * @param uri                    The URI of the website concerned with the alert
     * @param availabilityPercentage THe availability percentage when the alert was triggered
     */
    public AlarmTriggeredEvent(URI uri, double availabilityPercentage) {
        this.uri = uri;
        this.availabilityPercentage = availabilityPercentage;
    }
}
