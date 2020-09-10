package fr.gondyb.datadogwebsitemonitor.alarmdetector;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import fr.gondyb.datadogwebsitemonitor.alarmdetector.event.AlarmStoppedEvent;
import fr.gondyb.datadogwebsitemonitor.alarmdetector.event.AlarmTriggeredEvent;
import fr.gondyb.datadogwebsitemonitor.util.ExpiringArrayList;
import fr.gondyb.datadogwebsitemonitor.watchdog.event.WebsiteDownEvent;
import fr.gondyb.datadogwebsitemonitor.watchdog.event.WebsiteUpEvent;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AlarmDetector {
    private static final int AVAILABILITY_THRESHOLD = 80;

    private static final long DEFAULT_DURATION = TimeUnit.MINUTES.toMillis(2);

    Map<URI, ExpiringArrayList<Long>> latencies;

    Map<URI, Boolean> triggeredAlarm;

    private final EventBus eventBus;

    public AlarmDetector(EventBus eventBus) {
        this.eventBus = eventBus;
        this.latencies = new HashMap<>();
        this.triggeredAlarm = new HashMap<>();
    }

    @Subscribe
    public void handlerNewUpLog(WebsiteUpEvent event) {
        ExpiringArrayList<Long> siteLatencies = latencies.getOrDefault(
                event.getUri(),
                new ExpiringArrayList<>(DEFAULT_DURATION)
        );

        siteLatencies.add(event.getResponseTime());
        latencies.put(event.getUri(), siteLatencies);
        detectAlarm(event.getUri());
    }
    @Subscribe
    public void handlerNewDownLog(WebsiteDownEvent event) {
        ExpiringArrayList<Long> siteLatencies = latencies.getOrDefault(
                event.getUri(),
                new ExpiringArrayList<>(DEFAULT_DURATION)
        );

        siteLatencies.add((long) -1);
        latencies.put(event.getUri(), siteLatencies);
        detectAlarm(event.getUri());
    }

    public void detectAlarm(URI uri) {
        ExpiringArrayList<Long> siteLatencies = latencies.get(uri);

        int downEvents = 0;
        int allEvents = siteLatencies.size();
        for (long latency : siteLatencies) {
            if (latency == -1) {
                downEvents++;
            }
        }

        double percentage = (double) (allEvents - downEvents) * 100 / allEvents;

        boolean alreadyTriggered = triggeredAlarm.getOrDefault(uri, false);

        if (percentage <= AVAILABILITY_THRESHOLD && !alreadyTriggered) {
            System.out.println("ALARM TRIGGERED (" + percentage + ") " + uri);
            eventBus.post(new AlarmTriggeredEvent(uri, percentage));
            triggeredAlarm.put(uri, true);
            return;
        }

        if (percentage > AVAILABILITY_THRESHOLD && alreadyTriggered) {
            System.out.println("ALARM STOPPED (" + percentage + ") " + uri);
            eventBus.post(new AlarmStoppedEvent(uri, percentage));
            triggeredAlarm.put(uri, false);
        }

    }
}
