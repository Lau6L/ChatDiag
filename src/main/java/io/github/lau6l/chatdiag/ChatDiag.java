package io.github.lau6l.chatdiag;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatDiag implements ModInitializer {
    public static final String MOD_ID = "chatdiag";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing ChatDiag...");
    }
}
