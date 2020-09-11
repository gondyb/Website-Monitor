package fr.gondyb.datadogwebsitemonitor.ui;

import com.google.common.eventbus.EventBus;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.input.KeyStroke;
import fr.gondyb.datadogwebsitemonitor.alarm.event.AlarmStoppedEvent;
import fr.gondyb.datadogwebsitemonitor.alarm.event.AlarmTriggeredEvent;
import fr.gondyb.datadogwebsitemonitor.statistics.event.StatisticsUpdatedEvent;
import fr.gondyb.datadogwebsitemonitor.ui.event.StartMonitorEvent;

import java.net.URI;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainWindow extends BasicWindow {

    private final Panel minuteStatsPanel;

    private final Panel hourlyStatsPanel;

    private final Table<String> minuteTable;

    private final Table<String> hourTable;

    private final Table<String> alertsTable;

    private final Panel alertsPanel = new Panel();

    private final Panel statsPanel = new Panel(new LinearLayout(Direction.VERTICAL));

    private final Map<URI, StatisticsUpdatedEvent> minuteStats = new HashMap<>();

    private final Map<URI, StatisticsUpdatedEvent> hourStats = new HashMap<>();

    private final EventBus eventBus;

    public MainWindow(EventBus eventBus) {
        this.eventBus = eventBus;

        Panel mainPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        minuteStatsPanel = new Panel();
        minuteTable = new Table<>("URL", "Min (ms)", "Max (ms)", "Avg (ms)");
        minuteStatsPanel.addComponent(minuteTable);

        hourlyStatsPanel = new Panel();
        hourTable = new Table<>("URL", "Min (ms)", "Max (ms)", "Avg (ms)");
        hourlyStatsPanel.addComponent(hourTable);

        statsPanel.addComponent(minuteStatsPanel.withBorder(Borders.singleLine("Last 10 minutes stats")));
        statsPanel.addComponent(hourlyStatsPanel.withBorder(Borders.singleLine("Last hour stats")));
        mainPanel.addComponent(statsPanel);

        alertsPanel.setLayoutManager(new LinearLayout());
        alertsTable = new Table<>("Alarms");
        alertsPanel.addComponent(alertsTable);

        mainPanel.addComponent(alertsPanel.withBorder(Borders.singleLine("Down alerts")));

        Panel tipsPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        tipsPanel.addComponent(new Label("Press q to exit"));
        tipsPanel.addComponent(new EmptySpace());
        tipsPanel.addComponent(new Label("Press a to add a new website"));

        Panel rootPanel = new Panel();
        rootPanel.addComponent(mainPanel);
        rootPanel.addComponent(tipsPanel);

        setComponent(rootPanel);
        setHints(Arrays.asList(Hint.FULL_SCREEN, Hint.NO_DECORATIONS));

        addWindowListener(new WindowListener() {
            @Override
            public void onResized(Window window, TerminalSize terminalSize, TerminalSize terminalSize1) {

            }

            @Override
            public void onMoved(Window window, TerminalPosition terminalPosition, TerminalPosition terminalPosition1) {

            }

            @Override
            public void onInput(Window window, KeyStroke keyStroke, AtomicBoolean atomicBoolean) {
                MainWindow r = (MainWindow) window;
                if (keyStroke.getCharacter() == null) {
                    return;
                }
                switch (keyStroke.getCharacter()) {
                    case 'q':
                        window.close();
                        break;
                    case 'a':
                        r.displayNewWebsiteForm();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onUnhandledInput(Window window, KeyStroke keyStroke, AtomicBoolean atomicBoolean) {

            }
        });
    }

    void displayNewWebsiteForm() {
        String websiteUriString = TextInputDialog.showDialog(
                getTextGUI(),
                "New website (1/2)",
                "Please enter the URL for the website to check",
                "http://localhost:"
        );

        if (websiteUriString == null || websiteUriString.isEmpty()) {
            return;
        }
        URI websiteUri = URI.create(websiteUriString);

        String websitePeriodString = TextInputDialog.showDialog(
                getTextGUI(),
                "New website (2/2)",
                "Please enter the check period in milliseconds for the website to check",
                "1000"
        );

        if (websitePeriodString == null || websitePeriodString.isEmpty()) {
            return;
        }
        long period = Long.parseLong(websitePeriodString);

        eventBus.post(new StartMonitorEvent(
                websiteUri,
                period
        ));
    }

    void onTerminalResize(TerminalSize terminalSize) {
        TerminalSize half = new TerminalSize(terminalSize.getColumns() / 2, terminalSize.getRows() - 1);
        TerminalSize quarter = new TerminalSize(half.getColumns(), (half.getRows() / 2) + 1);
        minuteStatsPanel.setPreferredSize(quarter);
        hourlyStatsPanel.setPreferredSize(quarter);
        statsPanel.setPreferredSize(half);
        alertsPanel.setPreferredSize(half);
    }

    void onAlarmTriggeredEvent(AlarmTriggeredEvent event) {
        Date currentDate = new Date();
        NumberFormat formatter = new DecimalFormat("#0.00");
        alertsTable.getTableModel().addRow(
                "Website " + event.getUri() + " is down.\t\tavailability: " + formatter.format(event.getAvailabilityPercentage()) + "%\ttime: " + currentDate
        );
    }

    void onAlarmStoppedEvent(AlarmStoppedEvent event) {
        Date currentDate = new Date();
        NumberFormat formatter = new DecimalFormat("#0.00");
        alertsTable.getTableModel().addRow(
                "Website " + event.getUri() + " recovered.\tavailability: " + formatter.format(event.getAvailabilityPercentage()) + "%\ttime: " + currentDate
        );
    }

    void onStatisticsUpdatedEvent(StatisticsUpdatedEvent event) {
        if (event.getSavedStatisticsDuration() == TimeUnit.MINUTES.toMillis(10)) {
            minuteStats.put(event.getWebsiteUri(), event);
        } else if (event.getSavedStatisticsDuration() == TimeUnit.HOURS.toMillis(1)) {
            hourStats.put(event.getWebsiteUri(), event);
        }

        minuteTable.getTableModel().clear();

        for (StatisticsUpdatedEvent siteEvent : minuteStats.values()) {
            minuteTable.getTableModel().addRow(
                    siteEvent.getWebsiteUri().toString(),
                    String.valueOf(siteEvent.getMinLatency()),
                    String.valueOf(siteEvent.getMaxLatency()),
                    String.valueOf(siteEvent.getAverageLatency())
            );
        }

        hourTable.getTableModel().clear();

        for (StatisticsUpdatedEvent siteEvent : hourStats.values()) {
            hourTable.getTableModel().addRow(
                    siteEvent.getWebsiteUri().toString(),
                    String.valueOf(siteEvent.getMinLatency()),
                    String.valueOf(siteEvent.getMaxLatency()),
                    String.valueOf(siteEvent.getAverageLatency())
            );
        }
    }


}
