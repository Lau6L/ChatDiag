package io.github.lau6l.chatdiag.dialog;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import io.github.lau6l.chatdiag.network.SoundS2CPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Plays sounds and computes entity selector positions.
 *
 * @see DialogExecutor
 */
public class SoundPlayer {
    private static final EntityArgumentType ENTITY_ARGUMENT_TYPE = EntityArgumentType.entities();
    private static ServerCommandSource SOURCE;

    public static void initialize(MinecraftServer server) {
        if (SOURCE != null) return;
        SOURCE = new ServerCommandSource(
                CommandOutput.DUMMY,
                Vec3d.ZERO,
                Vec2f.ZERO,
                server.getOverworld(),
                4,
                "",
                Text.empty(),
                server,
                null
        );
    }

    /**
     * Returns a list of vectors from a list of either vectors or entity selector strings.
     *
     * @param pos {@link Either} a {@link Vec3d} or a {@code String} containing an entity selector
     * @return a {@link List} containing all stored and calculated vectors
     */
    private static List<Vec3d> toVec3dList(List<Either<Vec3d, String>> pos) {
        List<Vec3d> computedPositions = new ArrayList<>();
        for (Either<Vec3d, String> position : pos) {
            position.map(
                    computedPositions::add,
                    entitySelector -> {
                        try {
                            computedPositions.addAll(
                                    computePosition(entitySelector)
                            );
                        } catch (Exception ignored) {}
                        return null;
                    }
            );
        }
        return computedPositions;
    }

    /**
     * Calculates an entity selector.
     *
     * @param entitySelector A string containing an entity selector (e.g. "{@code @a}", "{@code @e[limit=2]}")
     * @return A {@link List<Vec3d>} of all selected positions, if valid.
     * @throws CommandSyntaxException if the given entity selector is invalid
     */
    public static List<Vec3d> computePosition(String entitySelector) throws CommandSyntaxException {
        return ENTITY_ARGUMENT_TYPE
                .parse(new StringReader(entitySelector))
                .getEntities(SOURCE)
                .stream()
                .map(Entity::getEntityPos)
                .toList();
    }

    /**
     * Plays a sound to a collection of players.
     *
     * @param players A {@link Collection} of selected players. This method will not run if this is empty.
     * @param sound A {@link Sound}. If its position is {@code null}, a {@link SoundS2CPayload} packet will
     *              be sent to the players for non-localized sound instead of a normal {@link ServerWorld#playSound} method.
     */
    public static void playSound(Collection<ServerPlayerEntity> players, Sound sound) {
        List<Either<Vec3d, String>> pos = sound.positions();

        if (pos == null) {
            playSoundsNoPos(players, sound);
        } else {
            playSoundsPos(players, sound, toVec3dList(pos));
        }
    }

    public static void playSoundsNoPos(Collection<ServerPlayerEntity> players, Sound sound) {
        SoundS2CPayload payload = new SoundS2CPayload(sound);
        SoundEvent soundEvent = SoundEvent.of(sound.id());

        for (ServerPlayerEntity player : players) {
            if (ServerPlayNetworking.canSend(player, SoundS2CPayload.ID)) {
                ServerPlayNetworking.send(
                        player,
                        payload
                );
            } else {
                playSound(
                        player,
                        player.getEntityPos(),
                        soundEvent,
                        sound.pitch(),
                        sound.volume()
                );
            }
        }
    }

    public static void playSoundsPos(Collection<ServerPlayerEntity> players, Sound sound, List<Vec3d> positions) {
        SoundEvent soundEvent = SoundEvent.of(sound.id());

        for (ServerPlayerEntity player : players) {
            for (Vec3d position : positions) {
                playSound(
                        player,
                        position,
                        soundEvent,
                        sound.pitch(),
                        sound.volume()
                );
            }
        }
    }

    private static void playSound(ServerPlayerEntity player, Vec3d pos, SoundEvent soundEvent, float pitch, float volume) {
        player.getEntityWorld()
                .playSound(
                        null,
                        pos.x, pos.y, pos.z,
                        soundEvent,
                        SoundCategory.MASTER,
                        volume,
                        pitch
                );
    }
}
