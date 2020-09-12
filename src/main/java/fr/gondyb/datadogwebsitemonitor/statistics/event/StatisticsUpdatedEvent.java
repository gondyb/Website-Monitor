package fr.gondyb.datadogwebsitemonitor.statistics.event;

import lombok.Data;

import java.net.URI;

@Data
public class StatisticsUpdatedEvent {
    private URI websiteUri;

    private long averageLatency;

    private long maxLatency;

    private long minLatency;

    private double availability;

    private long savedStatisticsDuration;

    public StatisticsUpdatedEvent(URI uri, long averageLatency, long maxLatency, long minLatency, double availability, long savedStatisticsDuration) {
        this.websiteUri = uri;
        this.averageLatency = averageLatency;
        this.maxLatency = maxLatency;
        this.minLatency = minLatency;
        this.availability = availability;
        this.savedStatisticsDuration = savedStatisticsDuration;
    }
}
