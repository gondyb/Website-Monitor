package fr.gondyb.datadogwebsitemonitor.alarmdetector;

import com.google.common.eventbus.EventBus;
import fr.gondyb.datadogwebsitemonitor.alarmdetector.event.AlarmStoppedEvent;
import fr.gondyb.datadogwebsitemonitor.alarmdetector.event.AlarmTriggeredEvent;
import fr.gondyb.datadogwebsitemonitor.watchdog.event.WebsiteDownEvent;
import fr.gondyb.datadogwebsitemonitor.watchdog.event.WebsiteUpEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AlarmDetectorTest {

    @Test
    public void it_should_throw_an_alarm_event_() {
        // Arrange
        EventBus eventBus = Mockito.mock(EventBus.class);
        AlarmDetector alarmDetector = new AlarmDetector(eventBus);

        URI uri = URI.create("http://test.fr");

        // Act
        for (int i = 0 ; i < 80 ; i++) {
            WebsiteUpEvent event = new WebsiteUpEvent(
                    (long) (Math.random() * 100),
                    200,
                    uri
            );
            alarmDetector.handlerNewUpLog(event);
        }

        for (int i = 0 ; i < 21 ; i++) {
            WebsiteDownEvent event = new WebsiteDownEvent(
                    uri
            );
            alarmDetector.handlerNewDownLog(event);
        }

        verify(eventBus).post(isA(AlarmTriggeredEvent.class));

        for (int i = 0 ; i < 200 ; i++) {
            WebsiteUpEvent event = new WebsiteUpEvent(
                    (long) (Math.random() * 100),
                    200,
                    uri
            );
            alarmDetector.handlerNewUpLog(event);
        }

        verify(eventBus).post(isA(AlarmStoppedEvent.class));
    }
}