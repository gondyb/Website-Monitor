package fr.gondyb.datadogwebsitemonitor.alarm.event;

import lombok.Getter;

import java.net.URI;

/**
 * This immutable class is an event informing that the availability for a website was calculated.
 */
@Getter
public class AvailabilityCalculatedEvent {
    /**
     * The URI of the website concerned with the availability.
     */
    private final URI uri;

    /**
     * The availability percentage calculated.
     */
    private final double availability;

    /**
     * Class constructor
     *
     * @param uri          The URI of the website concerned with the availability
     * @param availability The availability percentage calculated
     */
    public AvailabilityCalculatedEvent(URI uri, double availability) {
        this.uri = uri;
        this.availability = availability;
    }
}
