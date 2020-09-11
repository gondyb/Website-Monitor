package fr.gondyb.datadogwebsitemonitor.alarm;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import fr.gondyb.datadogwebsitemonitor.alarm.event.AlarmStoppedEvent;
import fr.gondyb.datadogwebsitemonitor.alarm.event.AlarmTriggeredEvent;
import fr.gondyb.datadogwebsitemonitor.ui.event.StartMonitorEvent;
import fr.gondyb.datadogwebsitemonitor.watchdog.event.WebsiteDownEvent;
import fr.gondyb.datadogwebsitemonitor.watchdog.event.WebsiteUpEvent;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class AlarmDetector {
    private static final int AVAILABILITY_THRESHOLD = 80;

    private final int historyDuration;

    Map<URI, CircularFifoQueue<Long>> latencies;

    Map<URI, Integer> upCounters;

    Map<URI, Integer> downCounters;

    Map<URI, Boolean> triggeredAlarm;

    private final EventBus eventBus;

    public AlarmDetector(int historyDuration, EventBus eventBus) {
        this.historyDuration = historyDuration;
        this.eventBus = eventBus;
        this.latencies = new HashMap<>();
        this.upCounters = new HashMap<>();
        this.downCounters = new HashMap<>();
        this.triggeredAlarm = new HashMap<>();
    }

    @Subscribe
    public void handleStartMonitoring(StartMonitorEvent event) {
        this.latencies.put(event.getUri(), new CircularFifoQueue<>((int) (historyDuration * 1000 / event.getDelay())));
    }

    @Subscribe
    public void handlerNewUpLog(WebsiteUpEvent event) {
        CircularFifoQueue<Long> siteLatencies = latencies.get(event.getUri());

        if (siteLatencies.isAtFullCapacity()) {
           removeLastCounter(event.getUri());
        }

        Integer upCounter = this.upCounters.getOrDefault(event.getUri(), 0);
        upCounter++;
        this.upCounters.put(event.getUri(), upCounter);

        siteLatencies.add(event.getResponseTime());
        latencies.put(event.getUri(), siteLatencies);

        detectAlarm(event.getUri());
    }
    @Subscribe
    public void handlerNewDownLog(WebsiteDownEvent event) {
        CircularFifoQueue<Long> siteLatencies = latencies.get(event.getUri());

        if (siteLatencies.isAtFullCapacity()) {
            removeLastCounter(event.getUri());
        }

        Integer downCounter = this.downCounters.getOrDefault(event.getUri(), 0);
        downCounter++;
        this.downCounters.put(event.getUri(), downCounter);

        siteLatencies.add((long) -1);
        latencies.put(event.getUri(), siteLatencies);

        detectAlarm(event.getUri());
    }

    private void removeLastCounter(URI uri) {
        CircularFifoQueue<Long> siteLatencies = latencies.getOrDefault(
                uri,
                new CircularFifoQueue<>()
        );
        Long removedLatency = siteLatencies.remove();
        if (removedLatency == -1) {
            Integer counter = downCounters.get(uri);
            counter -= 1;
            downCounters.put(uri, counter);
        } else {
            Integer counter = upCounters.get(uri);
            counter -= 1;
            upCounters.put(uri, counter);
        }
    }

    public void detectAlarm(URI uri) {
        Integer upCounter = upCounters.getOrDefault(uri, 0);
        Integer downCounter = downCounters.getOrDefault(uri, 0);

        double percentage = (double) upCounter * 100 / (upCounter + downCounter);

        boolean alreadyTriggered = triggeredAlarm.getOrDefault(uri, false);

        if (percentage <= AVAILABILITY_THRESHOLD && !alreadyTriggered) {
            eventBus.post(new AlarmTriggeredEvent(uri, percentage));
            triggeredAlarm.put(uri, true);
            return;
        }

        if (percentage > AVAILABILITY_THRESHOLD && alreadyTriggered) {
            eventBus.post(new AlarmStoppedEvent(uri, percentage));
            triggeredAlarm.put(uri, false);
        }

    }
}
