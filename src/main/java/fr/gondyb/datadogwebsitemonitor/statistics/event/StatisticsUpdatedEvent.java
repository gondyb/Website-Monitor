package fr.gondyb.datadogwebsitemonitor.statistics.event;

import lombok.Getter;

import java.net.URI;
import java.util.Map;

/**
 * This immutable class is an event informing that new statistics were calculated for a given website over a given period.
 */
@Getter
public class StatisticsUpdatedEvent {
    /**
     * The URI of the website concerned with these statistics.
     */
    private final URI websiteUri;

    /**
     * The average latency in milliseconds for the website.
     */
    private final long averageLatency;

    /**
     * The maximum latency in milliseconds for the website.
     */
    private final long maxLatency;

    /**
     * The minimum latency in milliseconds for the website.
     */
    private final long minLatency;

    /**
     * The availability percentage for the website.
     */
    private final double availability;

    /**
     * The different response codes returned by the website, where Keys are response code classes. Values are number of hits during the {@code savedStatisticsDuration}.
     */
    private final Map<Integer, Integer> responseCodeHits;

    /**
     * THe duration in milliseconds during which these statistics have been aggregated.
     */
    private final long savedStatisticsDuration;

    /**
     * Class consstructor
     *
     * @param uri                     The URI of the website concerned with these statistics
     * @param averageLatency          The average latency in milliseconds for the website
     * @param maxLatency              The maximum latency in milliseconds for the website
     * @param minLatency              he minimum latency in milliseconds for the website
     * @param availability            The availability percentage for the website
     * @param responseCodeHits        The different response codes returned by the website, where Keys are response code classes. Values are number of hits during the {@code savedStatisticsDuration}
     * @param savedStatisticsDuration He duration in milliseconds during which these statistics have been aggregated
     */
    public StatisticsUpdatedEvent(URI uri, long averageLatency, long maxLatency, long minLatency, double availability, Map<Integer, Integer> responseCodeHits, long savedStatisticsDuration) {
        this.websiteUri = uri;
        this.averageLatency = averageLatency;
        this.maxLatency = maxLatency;
        this.minLatency = minLatency;
        this.availability = availability;
        this.responseCodeHits = responseCodeHits;
        this.savedStatisticsDuration = savedStatisticsDuration;
    }
}
