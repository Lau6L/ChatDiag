package io.github.lau6l.chatdiag.dialog;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.github.lau6l.chatdiag.ChatDiag;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.SimpleResourceReloader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Loads and serializes dialog definitions from JSON resources.
 * <p>
 * All stored dialogs must be inside the /data/[namespace]/chatdiag/ directory.
 */
public class DialogLoader {

    /**
     * Registers a listener that processes dialog definitions from {@code /chatdiag/} data.
     */
    public static void registerReloadListener() {
        ResourceLoader.get(ResourceType.SERVER_DATA).registerReloader(
                ChatDiag.of("chat_dialogs"),
                new SimpleResourceReloader<Map<Identifier, Dialog>>() {
                    @Override
                    protected @NotNull Map<Identifier, Dialog> prepare(@NotNull Store store) {
                        Map<Identifier, Dialog> dialogs = new HashMap<>();
                        ResourceManager resourceManager = store.getResourceManager();

                        Map<Identifier, Resource> resources = resourceManager.findResources(
                                "chatdiag",
                                id -> id.getPath().endsWith(".json")
                        );
                        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
                            Dialog dialog = loadDialog(entry.getValue());
                            if (dialog != Dialog.EMPTY) {
                                // turn namespace:chatdiag/file.json into namespace:file
                                Identifier id = entry.getKey();
                                String path = id.getPath().replaceFirst("chatdiag/", "");
                                String finalPath = path.substring(
                                        0,
                                        path.lastIndexOf(".")
                                );
                                dialogs.put(Identifier.of(id.getNamespace(), finalPath), dialog);
                            }
                        }

                        return dialogs;
                    }

                    @Override
                    protected void apply(@NotNull Map<Identifier, Dialog> prepared, @NotNull Store store) {
                        Dialogs.setDialogs(prepared);
                    }
                }
        );
    }

    /**
     * Reads a resource and returns the contents as a {@link Dialog}.
     *
     * @param resource the resource to load
     * @return a new {@code Dialog}
     */
    private static Dialog loadDialog(Resource resource) {
        try {
            String json = resource
                    .getReader()
                    .lines()
                    .collect(Collectors.joining());
            return deserializeDialog(json);
        } catch (IOException e) {
            ChatDiag.LOGGER.error("Error loading dialog:", e);
            return Dialog.EMPTY;
        }
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
