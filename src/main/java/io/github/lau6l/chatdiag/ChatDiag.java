package io.github.lau6l.chatdiag;

import io.github.lau6l.chatdiag.dialog.Dialogs;
import io.github.lau6l.chatdiag.util.Scheduler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatDiag implements ModInitializer {
    public static final String MOD_ID = "chatdiag";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing ChatDiag...");

        Scheduler.initialize();

        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStart);
    }

    private void onServerStart(MinecraftServer minecraftServer) {
        Dialogs.freeze();
    }

    public static Identifier of(String value) {
        return Identifier.of(MOD_ID, value);
    }
}
