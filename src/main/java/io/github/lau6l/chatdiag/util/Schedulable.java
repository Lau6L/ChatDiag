package io.github.lau6l.chatdiag.util;

import java.util.concurrent.CompletableFuture;

public class Schedulable {
    private final Runnable runnable;
    private final long delay;
    private boolean isCancelled;
    private final CompletableFuture<Boolean> future;

    public Schedulable(Runnable runnable, long delay) {
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
        isCancelled = true;
        return future.complete(false);
    }

    public CompletableFuture<Boolean> schedule() {
        Scheduler.schedule(this);
        return future;
    }

    public long delay() {
        return delay;
    }
}
