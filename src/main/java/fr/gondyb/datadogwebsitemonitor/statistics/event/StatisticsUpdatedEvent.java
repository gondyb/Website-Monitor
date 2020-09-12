package fr.gondyb.datadogwebsitemonitor.statistics.event;

import lombok.Data;

import java.net.URI;
import java.util.Map;

@Data
public class StatisticsUpdatedEvent {
    private URI websiteUri;

    private long averageLatency;

    private long maxLatency;

    private long minLatency;

    private double availability;

    private Map<Integer, Integer> responseCodeHits;

    private long savedStatisticsDuration;

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
