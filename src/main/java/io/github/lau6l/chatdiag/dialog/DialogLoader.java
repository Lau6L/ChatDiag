package io.github.lau6l.chatdiag.dialog;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.github.lau6l.chatdiag.ChatDiag;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.stream.Collectors;

public class DialogLoader {
    public static Dialog loadDialog(Identifier id) {
        String file = String.join("/","data", id.getNamespace(), "chatdiag", id.getPath() + ".json");
        try (InputStream in = Files.newInputStream(
                FabricLoader
                        .getInstance()
                        .getModContainer(id.getNamespace())
                        .get()
                        .findPath(file)
                        .get()
        )) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String json = reader.lines().collect(Collectors.joining());
            return deserializeDialog(json);
        } catch (IOException e) {
            ChatDiag.LOGGER.error("Error loading dialog:", e);
        }
        return Dialog.EMPTY;
    }

    public static String serializeDialog(Dialog dialog) {
        return Dialog.CODEC
                .encodeStart(JsonOps.INSTANCE, dialog)
                .result()
                .orElseThrow()
                .getAsString();
    }
    public static Dialog deserializeDialog(String json) {
        return Dialog.CODEC.parse(
                        JsonOps.INSTANCE,
                        JsonParser.parseString(json)
                )
                .result()
                .orElseThrow();
    }
}
