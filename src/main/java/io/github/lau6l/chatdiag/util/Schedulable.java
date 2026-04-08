package io.github.lau6l.chatdiag.util;

import java.util.concurrent.CompletableFuture;

/**
 * Represents a task to be scheduled by {@link Scheduler}.
 * <p>
 * Each task completes a future with {@code true} when it runs successfully, or
 * {@code false} when canceled.
 *
 * @see Scheduler
 */
public class Schedulable {
    private final Runnable runnable;
    public final long delay;
    private boolean isCancelled;
    private final CompletableFuture<Boolean> future;

    public Schedulable(Runnable runnable, long delay) {
        this.runnable = runnable;
        this.delay = delay < 0 ? 0 : delay;
        this.isCancelled = false;
        future = new CompletableFuture<>();
    }

    /**
     * Runs this task immediately if it hasn't been canceled, and completes its future.
     */
    public void run() {
        if (!isCancelled) {
            runnable.run();
            future.complete(true);
            cancel();
        }
    }

    /**
     * Cancels this task.
     *
     * @return {@code true} if the task was not previously canceled; {@code false} otherwise.
     */
    public boolean cancel() {
        isCancelled = true;
        return future.complete(false);
    }

    /**
     * Adds this task to {@link Scheduler}, queuing it to be run.
     *
     * @return a future that resolves when the task is run or canceled
     */
    public CompletableFuture<Boolean> schedule() {
        Scheduler.schedule(this);
        return future;
    }
}
