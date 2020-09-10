package fr.gondyb.datadogwebsitemonitor.util;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ExpiringArrayList<T> extends ArrayList<T> {

    private final long expirationDelay;

    public ExpiringArrayList(long expirationDelay) {
        super();
        this.expirationDelay = expirationDelay;
    }

    @Override
    public boolean add(T itemToAdd) {
        Timer timer = new Timer();
        ExpiringArrayList<T> ref = this;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ref.remove(itemToAdd);
            }
        }, expirationDelay);
        return super.add(itemToAdd);
    }
}
