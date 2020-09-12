package fr.gondyb.datadogwebsitemonitor.statistics.event;

import lombok.Getter;

import java.net.URI;
import java.util.Map;

@Getter
public class StatisticsUpdatedEvent {
    private final URI websiteUri;

    private final long averageLatency;

    private final long maxLatency;

    private final long minLatency;

    private final double availability;

    private final Map<Integer, Integer> responseCodeHits;

    private final long savedStatisticsDuration;

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
