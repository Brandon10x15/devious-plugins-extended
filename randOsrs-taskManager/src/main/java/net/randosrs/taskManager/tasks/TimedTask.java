package net.randosrs.taskManager.tasks;

import net.unethicalite.api.plugins.SubscribedPlugin;

import java.util.Timer;
import java.util.TimerTask;

import static net.randosrs.taskManager.randOsrsTaskManagerPlugin.log;

public class TimedTask {
    private final SubscribedPlugin plugin;
    private long millis;
    protected long millisLeft;
    private Runnable method;
    private Timer timer;
    private String name;
    private TimerTask task;
    private Timer timeLeftTimer;
    private Runnable updateLengthMethod;

    public TimedTask(SubscribedPlugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }
    public void resetTimeLeftTimer() {
        millisLeft = millis + 1 - 1;
        timeLeftTimer = new Timer();
        timeLeftTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                millisLeft -= 1000;
                if(millisLeft <= 0) {
                    cancel();
                }
            }
        }, 0, 1000);
    }

    public void resetTimer(long millis) {
        if(this.timer != null) {
            this.timer.cancel();
        }
        this.millis = millis;
        this.millisLeft = millis + 1 - 1;
        if(millis < 1) {
            return;
        }
        this.timer = new Timer();
        this.task = new TimerTask() {
            @Override
            public void run() {
                method.run();
            }
        };
        timer.schedule(task, millis);
        resetTimeLeftTimer();
    }

    public void stopTask() {
        log("Stopping task: " + name);
        if(this.timer != null) {
            this.timer.cancel();
        }
    }

    public boolean isActive() {
        return millisLeft > 0;
    }

    public long getMillis() {
        return millis;
    }

    public void setMillis(long millis) {
        this.millis = millis;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public TimerTask getTask() {
        return task;
    }

    public void setTask(TimerTask task) {
        this.task = task;
    }

    public Runnable getMethod() {
        return method;
    }

    public void setMethod(Runnable method) {
        this.method = () -> {
            log("Running method for: " + name);
            method.run();
        };
    }

    public long getMillisLeft() {
        return millisLeft;
    }

    public void setUpdateLengthMethod(Runnable updateLengthMethod) {
        this.updateLengthMethod = () -> {
            log("Running updateLength for: " + name);
            updateLengthMethod.run();
        };
    }

    public String getName() {
        return name;
    }

    public void updateLength() {
        updateLengthMethod.run();
    }

    public void resetTask() {
        log("Resetting task: " + name);
        updateLength();
        if(millis > 0) {
            resetTimer(millis);
        }
    }
}
