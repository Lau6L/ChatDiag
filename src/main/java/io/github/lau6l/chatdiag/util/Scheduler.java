package io.github.lau6l.chatdiag.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Scheduler {
    private static final List<Pair<Long, SchedulableTask>> tasks = new ArrayList<>(4);
    private static int tickCount = 0;

    public static void initialize() {
        ServerTickEvents.START_SERVER_TICK.register((s) -> tick());
    }

    private static void tick() {
        tickCount++;

        Iterator<Pair<Long, SchedulableTask>> it = tasks.iterator();
        while (it.hasNext()) {
            Pair<Long, SchedulableTask> taskPair = it.next();
            if (tickCount == taskPair.getLeft()) {
                taskPair.getRight().run();
                it.remove();
            }
        }
    }

    public static void schedule(SchedulableTask task) {
        if (task.delay() == 0) {
            task.run();
            return;
        }
        tasks.add(new Pair<>(task.delay() + tickCount, task));
    }
}
