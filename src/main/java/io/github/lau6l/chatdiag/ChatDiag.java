package io.github.lau6l.chatdiag;

import io.github.lau6l.chatdiag.api.ChatDiagApi;
import io.github.lau6l.chatdiag.command.ChatDiagCommand;
import io.github.lau6l.chatdiag.command.DialogArgumentType;
import io.github.lau6l.chatdiag.dialog.DialogLoader;
import io.github.lau6l.chatdiag.util.Scheduler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
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
        registerCommands();
        DialogLoader.registerReloadListener();
        ArgumentTypeRegistry.registerArgumentType(of("dialog"), DialogArgumentType.class, ConstantArgumentSerializer.of(DialogArgumentType::dialog));
    }

    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                ChatDiagCommand.register(dispatcher)
        );
    }

    public static Identifier of(String value) {
        return Identifier.of(MOD_ID, value);
    }

    private static final ChatDiagApi API = ChatDiagApi.api();
    public static ChatDiagApi api() {
        return API;
    }
}
