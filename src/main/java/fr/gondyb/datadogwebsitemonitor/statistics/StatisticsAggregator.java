package fr.gondyb.datadogwebsitemonitor.statistics;

import com.google.common.eventbus.EventBus;
import fr.gondyb.datadogwebsitemonitor.statistics.event.StatisticsUpdatedEvent;
import fr.gondyb.datadogwebsitemonitor.watchdog.event.WebsiteDownEvent;
import fr.gondyb.datadogwebsitemonitor.watchdog.event.WebsiteUpEvent;
import lombok.Data;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

@Data
public class StatisticsAggregator {

    private final URI websiteUri;

    private final long savedStatisticsDuration;

    private final long eventPushingRate;

    private final CircularFifoQueue<Long> latencies;

    private final EventBus eventBus;

    private long minLatency = Long.MAX_VALUE;

    private long maxLatency = 0;

    private long avgLatency = -1;

    private long sumLatencies = 0;

    private long pollingRate = 0;

    public StatisticsAggregator(URI websiteUri, long pollingRate, long savedStatisticsDuration, long eventPushingRate, EventBus eventBus) {
        this.pollingRate = pollingRate;
        this.websiteUri = websiteUri;
        this.savedStatisticsDuration = savedStatisticsDuration;
        this.eventPushingRate = eventPushingRate;
        this.eventBus = eventBus;
        this.latencies = new CircularFifoQueue<>((int) (savedStatisticsDuration / pollingRate));

        Timer updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                pushStatistics();
            }
        }, this.eventPushingRate, this.eventPushingRate);
    }

    protected void handleWebsiteUpEvent(WebsiteUpEvent event) {
        computeStatistics(event.getResponseTime());
    }

    protected void handleWebsiteDownEvent(WebsiteDownEvent event) {
        computeStatistics(pollingRate);
    }

    private void computeStatistics(long newLatency) {
        if (latencies.isAtFullCapacity()) {
            long removedLatency = latencies.remove();
            if (removedLatency == minLatency) {
                minLatency = Long.MAX_VALUE;
            }
            if (removedLatency == maxLatency) {
                maxLatency = 0;
            }

            sumLatencies -= removedLatency;
        }
        latencies.add(newLatency);

        minLatency = Math.min(minLatency, newLatency);
        maxLatency = Math.max(maxLatency, newLatency);

        sumLatencies += newLatency;
        avgLatency = sumLatencies / latencies.size();
    }

    private void pushStatistics() {
        StatisticsUpdatedEvent event = new StatisticsUpdatedEvent(
                this.websiteUri,
                avgLatency,
                maxLatency,
                minLatency,
                this.savedStatisticsDuration
        );

        eventBus.post(event);
    }
}
