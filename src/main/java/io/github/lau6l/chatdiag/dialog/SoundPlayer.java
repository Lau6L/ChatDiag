package io.github.lau6l.chatdiag.dialog;

import com.mojang.brigadier.StringReader;
import com.mojang.datafixers.util.Either;
import io.github.lau6l.chatdiag.network.SoundS2CPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.permission.LeveledPermissionPredicate;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SoundPlayer {
    private static final EntityArgumentType ENTITY_ARGUMENT_TYPE = EntityArgumentType.entities();
    private static ServerCommandSource SOURCE;

    public static void initialize(MinecraftServer server) {
        SOURCE = new ServerCommandSource(
                CommandOutput.DUMMY,
                Vec3d.ZERO,
                Vec2f.ZERO,
                server.getOverworld(),
                LeveledPermissionPredicate.GAMEMASTERS,
                "",
                Text.empty(),
                server,
                null
        );
    }

    private static List<Vec3d> computePositions(List<Either<Vec3d, String>> pos) {
        List<Vec3d> computedPositions = new ArrayList<>();
        for (Either<Vec3d, String> position : pos) {
            position.map(
                    computedPositions::add,
                    entitySelector -> {
                        try {
                            computedPositions.addAll(
                                    ENTITY_ARGUMENT_TYPE
                                            .parse(new StringReader(entitySelector))
                                            .getEntities(SOURCE)
                                            .stream()
                                            .map(Entity::getEntityPos)
                                            .toList()
                            );
                        } catch (Exception ignored) {}
                        return null;
                    }
            );
        }
        return computedPositions;
    }

    public static void playSound(Collection<ServerPlayerEntity> players, Sound sound) {
        List<Either<Vec3d, String>> pos = sound.positions();

        if (pos == null) {
            playSoundsNoPos(players, sound);
        } else {
            playSoundsPos(players, sound, computePositions(pos));
        }
    }

    private static void playSoundsNoPos(Collection<ServerPlayerEntity> players, Sound sound) {
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
