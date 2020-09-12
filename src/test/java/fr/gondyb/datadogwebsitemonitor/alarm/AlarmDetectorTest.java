package fr.gondyb.datadogwebsitemonitor.alarm;

import com.google.common.eventbus.EventBus;
import fr.gondyb.datadogwebsitemonitor.alarm.event.AlarmTriggeredEvent;
import fr.gondyb.datadogwebsitemonitor.alarm.event.AvailabilityCalculatedEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@RunWith(MockitoJUnitRunner.class)
public class AlarmDetectorTest {

    @Test
    public void it_should_throw_an_alarm_event_if_availability_is_under_80() {
        // Arrange
        EventBus eventBus = Mockito.mock(EventBus.class);
        AlarmDetector alarmDetector = new AlarmDetector(eventBus);

        URI uri = URI.create("http://test.fr");

        AvailabilityCalculatedEvent availabilityCalculatedEvent = new AvailabilityCalculatedEvent(
                uri,
                70
        );

        // Act
        alarmDetector.availabilityCalculatedHandler(availabilityCalculatedEvent);

        // Assert
        verify(eventBus).post(isA(AlarmTriggeredEvent.class));
    }

    @Test
    public void it_should_not_throw_an_alarm_event_if_availability_is_over_80() {
        // Arrange
        EventBus eventBus = Mockito.mock(EventBus.class);
        AlarmDetector alarmDetector = new AlarmDetector(eventBus);

        URI uri = URI.create("http://test.fr");

        AvailabilityCalculatedEvent availabilityCalculatedEvent = new AvailabilityCalculatedEvent(
                uri,
                90
        );

        // Act
        alarmDetector.availabilityCalculatedHandler(availabilityCalculatedEvent);

        // Assert
        verifyNoInteractions(eventBus);
    }

    @Test
    public void it_should_throw_an_alarm_stopped_event_if_availability_is_over_80_after_being_triggered() {
        // Arrange
        EventBus eventBus = Mockito.mock(EventBus.class);
        AlarmDetector alarmDetector = new AlarmDetector(eventBus);

        URI uri = URI.create("http://test.fr");

        AvailabilityCalculatedEvent availabilityCalculatedEvent = new AvailabilityCalculatedEvent(
                uri,
                70
        );

        // Act
        alarmDetector.availabilityCalculatedHandler(availabilityCalculatedEvent);

        // Assert
        verify(eventBus).post(isA(AlarmTriggeredEvent.class));

        availabilityCalculatedEvent = new AvailabilityCalculatedEvent(
                uri,
                90
        );

        // Act
        alarmDetector.availabilityCalculatedHandler(availabilityCalculatedEvent);

        // Assert
        verify(eventBus).post(isA(AlarmTriggeredEvent.class));
    }
}