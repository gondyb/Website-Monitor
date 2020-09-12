package fr.gondyb.datadogwebsitemonitor.alarm.event;

import lombok.Getter;

import java.net.URI;

/**
 * This immutable class is an event informing that the availability alert for a website stopped.
 */
@Getter
public class AlarmStoppedEvent {
    /**
     * The availability percentage when the alert stopped.
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
     * @param availabilityPercentage THe availability percentage when the alert stopped
     */
    public AlarmStoppedEvent(URI uri, double availabilityPercentage) {
        this.uri = uri;
        this.availabilityPercentage = availabilityPercentage;
    }
}
