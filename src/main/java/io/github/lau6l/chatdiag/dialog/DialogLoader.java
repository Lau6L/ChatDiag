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

/**
 * Loads and serializes dialog definitions from JSON resources.
 * <p>
 * All stored dialogs must be inside the /data/[namespace]/chatdiag/ directory.
 */
public class DialogLoader {
    /**
     * Loads a dialog JSON file from its {@link Identifier}.
     *
     * @param id the dialog identifier
     * @return the loaded dialog, or {@link Dialog#EMPTY} if loading fails
     */
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

    /**
     * Serializes a {@link Dialog} to JSON.
     * <p>
     * Can be used by other mods for data generation.
     *
     * @param dialog the dialog to serialize
     * @return a JSON string
     */
    public static String serializeDialog(Dialog dialog) {
        return Dialog.CODEC
                .encodeStart(JsonOps.INSTANCE, dialog)
                .result()
                .orElseThrow()
                .getAsString();
    }

    /**
     * Parses a dialog from JSON.
     *
     * @param json the JSON input
     * @return the parsed dialog
     */
    public static Dialog deserializeDialog(String json) {
        return Dialog.CODEC.parse(
                        JsonOps.INSTANCE,
                        JsonParser.parseString(json)
                )
                .result()
                .orElseThrow();
    }
}
