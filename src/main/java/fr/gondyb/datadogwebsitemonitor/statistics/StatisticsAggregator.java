package fr.gondyb.datadogwebsitemonitor.statistics;

import com.google.common.eventbus.EventBus;
import fr.gondyb.datadogwebsitemonitor.alarm.event.AvailabilityCalculatedEvent;
import fr.gondyb.datadogwebsitemonitor.statistics.event.StatisticsUpdatedEvent;
import fr.gondyb.datadogwebsitemonitor.watchdog.event.WebsiteDownEvent;
import fr.gondyb.datadogwebsitemonitor.watchdog.event.WebsiteUpEvent;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class StatisticsAggregator {

    private final URI websiteUri;

    private final long savedStatisticsDuration;

    private final CircularFifoQueue<Long> latencies;

    private final CircularFifoQueue<Integer> responseCodes;

    private final Map<Integer, Integer> responseCodesHits;

    private final EventBus eventBus;

    private long minLatency = Long.MAX_VALUE;

    private long maxLatency = 0;

    private long avgLatency = -1;

    private long sumLatencies = 0;

    private double availability = -1;

    private final long pollingRate;

    public StatisticsAggregator(URI websiteUri, long pollingRate, long savedStatisticsDuration, long eventPushingRate, EventBus eventBus) {
        this.pollingRate = pollingRate;
        this.websiteUri = websiteUri;
        this.savedStatisticsDuration = savedStatisticsDuration;
        this.eventBus = eventBus;
        this.latencies = new CircularFifoQueue<>((int) (savedStatisticsDuration / pollingRate));
        responseCodes = new CircularFifoQueue<>((int) (savedStatisticsDuration / pollingRate));
        responseCodesHits = new HashMap<>();

        Timer updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                pushStatistics();
            }
        }, 0, eventPushingRate);
    }

    protected void handleWebsiteUpEvent(WebsiteUpEvent event) {
        computeTimeStatistics(event.getResponseTime());
        computeResponseCodesStatistics(event.getResponseCode());
    }

    protected void handleWebsiteDownEvent(WebsiteDownEvent event) {
        computeTimeStatistics(pollingRate);
    }

    protected void handleAvailabilityCalculatedEvent(AvailabilityCalculatedEvent event) {
        this.availability = event.getAvailability();
    }

    private void computeTimeStatistics(long newLatency) {
        if (latencies.isAtFullCapacity()) {
            long removedLatency = latencies.remove();
            if (removedLatency == minLatency) {
                minLatency = latencies.stream().mapToLong(v -> v).min().orElse(-1);
            }
            if (removedLatency == maxLatency) {
                maxLatency = latencies.stream().mapToLong(v -> v).max().orElse(-1);
            }

            sumLatencies -= removedLatency;
        }
        latencies.add(newLatency);

        minLatency = Math.min(minLatency, newLatency);
        maxLatency = Math.max(maxLatency, newLatency);

        sumLatencies += newLatency;
        avgLatency = sumLatencies / latencies.size();
    }

    private void computeResponseCodesStatistics(Integer responseCode) {
        if (responseCodes.isAtFullCapacity()) {
            Integer responseCodeToRemove = responseCodes.remove();

            Integer responseCodeCategory = responseCodeToRemove / 100;

            Integer previousCounter = responseCodesHits.get(responseCodeCategory);
            responseCodesHits.put(responseCodeCategory, previousCounter - 1);
        }

        Integer responseCodeCategory = responseCode / 100;
        Integer previousCounter = responseCodesHits.getOrDefault(responseCodeCategory, 0);
        responseCodesHits.put(responseCodeCategory, previousCounter + 1);
    }

    private void pushStatistics() {
        StatisticsUpdatedEvent event = new StatisticsUpdatedEvent(
                this.websiteUri,
                this.avgLatency,
                this.maxLatency,
                this.minLatency == Long.MAX_VALUE ? 0 : this.minLatency,
                this.availability,
                new HashMap<>(this.responseCodesHits),  // We need to create a new HashMap,
                // otherwise the pointer is passed,
                // and the window's data is updated continuously.
                this.savedStatisticsDuration
        );

        eventBus.post(event);
    }
}
