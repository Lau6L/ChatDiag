package io.github.lau6l.chatdiag.util;

import java.util.concurrent.CompletableFuture;

public class SchedulableTask {
    private final Runnable runnable;
    private final long delay;
    private boolean isCancelled;
    private final CompletableFuture<Boolean> future;

    public SchedulableTask(Runnable runnable, long delay) {
        this.runnable = runnable;
        this.delay = delay < 0 ? 0 : delay;
        this.isCancelled = false;
        future = new CompletableFuture<>();
    }

    public void run() {
        if (!isCancelled) {
            runnable.run();
            future.complete(true);
            cancel();
        }
    }

    public boolean cancel() {
        boolean wasCancelled = isCancelled;
        isCancelled = true;
        future.complete(false);
        return !wasCancelled;
    }

    public CompletableFuture<Boolean> schedule() {
        Scheduler.schedule(this);
        return future;
    }

    public long delay() {
        return delay;
    }
}
