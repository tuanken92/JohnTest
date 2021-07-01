package com.example.common;

import android.app.Activity;
import android.widget.TextView;

import com.example.application.Application;
import com.example.rfid.RFIDController;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Created by qvfr34 on 2/16/2015.
 */
public class Inventorytimer {
    private static final int INV_UPDATE_INTERVAL = 500;
    private static Inventorytimer inventorytimer;
    private static long startedTime;
    private Activity activity;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> rrTimer;

    public static Inventorytimer getInstance() {
        if (inventorytimer == null)
            inventorytimer = new Inventorytimer();
        return inventorytimer;
    }

    public void startTimer() {
        if (isTimerRunning())
            stopTimer();
        startedTime = System.currentTimeMillis();
        if (scheduler == null) {
            scheduler = Executors.newScheduledThreadPool(1);
            final Runnable task = new Runnable() {
                public void run() {
                    //ReadRate = (No Of Tags Read / Inventory Duration)
                    RFIDController.mRRStartedTime += INV_UPDATE_INTERVAL;
                    if (RFIDController.mRRStartedTime == 0)
                        Application.TAG_READ_RATE = 0;
                    else
                        Application.TAG_READ_RATE = (int) (Application.TOTAL_TAGS / (RFIDController.mRRStartedTime / (float) 1000));
                    startedTime = System.currentTimeMillis();
                    updateUI();
                }
            };
            rrTimer = scheduler.scheduleAtFixedRate(task, INV_UPDATE_INTERVAL, INV_UPDATE_INTERVAL, MILLISECONDS);
        }
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void stopTimer() {
        if (rrTimer != null) {
            //Stop the timer
            rrTimer.cancel(true);
            scheduler.shutdown();
            //ReadRate = (No Of Tags Read / Inventory Duration)
            RFIDController.mRRStartedTime += (System.currentTimeMillis() - startedTime);
            if (RFIDController.mRRStartedTime == 0)
                Application.TAG_READ_RATE = 0;
            else
                Application.TAG_READ_RATE = (int) (Application.TOTAL_TAGS / (RFIDController.mRRStartedTime / (float) 1000));
        }
        rrTimer = null;
        scheduler = null;
        updateUI();
    }

    public boolean isTimerRunning() {
        if (rrTimer != null)
            return true;
        return false;
    }

    void updateUI() {

    }


}
