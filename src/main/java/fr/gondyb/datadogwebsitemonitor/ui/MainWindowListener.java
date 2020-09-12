package fr.gondyb.datadogwebsitemonitor.ui;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowListener;
import com.googlecode.lanterna.input.KeyStroke;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class handles the {@link MainWindow} inputs.
 */
public class MainWindowListener implements WindowListener {
    @Override
    public void onResized(Window window, TerminalSize terminalSize, TerminalSize terminalSize1) {
        // Do nothing
    }

    @Override
    public void onMoved(Window window, TerminalPosition terminalPosition, TerminalPosition terminalPosition1) {
        // Do nothing
    }

    @Override
    public void onInput(Window window, KeyStroke keyStroke, AtomicBoolean atomicBoolean) {
        MainWindow mainWindow = (MainWindow) window;
        if (keyStroke.getCharacter() == null) {
            return;
        }
        switch (keyStroke.getCharacter()) {
            case 'q':
                window.close();
                break;
            case 'a':
                mainWindow.displayNewWebsiteForm();
                break;
            default:
                break;
        }
    }

    @Override
    public void onUnhandledInput(Window window, KeyStroke keyStroke, AtomicBoolean atomicBoolean) {
        // Do nothing
    }
}
