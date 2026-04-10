package io.github.lau6l.chatdiag.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight tick-based task scheduler. Used to delay dialog lines between server ticks.
 *
 * @see Schedulable
 */
public class Scheduler {
    private static final List<Pair<Long, Schedulable>> tasks = new ArrayList<>(4);
    private static int tickCount = 0;

    public static void initialize() {
        ServerTickEvents.START_SERVER_TICK.register((s) -> tick());
    }

    private static void tick() {
        tickCount++;
        if (tasks.isEmpty()) return;

        List<Pair<Long, Schedulable>> tasksToExecute = new ArrayList<>();
        tasks.removeIf(task -> {
            if (tickCount == task.getLeft()) {
                tasksToExecute.add(task);
                return true;
            } else return false;
        });
        tasksToExecute.forEach(task ->
                task.getRight().run());
    }

    /**
     * Schedules a task for later execution.
     *
     * @param task the task to schedule
     */
    public static void schedule(Schedulable task) {
        if (task.delay == 0) {
            task.run();
            return;
        }
        tasks.add(new Pair<>(task.delay + tickCount, task));
    }
}
