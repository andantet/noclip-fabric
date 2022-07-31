package me.andante.noclip.impl.client.keybinding;

import me.andante.noclip.api.client.NoClipClient;
import me.andante.noclip.api.client.NoClipManager;
import me.andante.noclip.api.client.config.NoClipConfig;
import me.andante.noclip.api.client.keybinding.NoClipKeybindings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;

import java.util.List;

@Environment(EnvType.CLIENT)
public final class NoClipKeybindingsImpl implements NoClipKeybindings {
    public static final List<KeyBinding> HORIZONTAL_MOVE = Util.make(() -> {
        MinecraftClient client = MinecraftClient.getInstance();
        GameOptions options = client.options;
        return List.of(options.leftKey, options.rightKey, options.backKey, options.forwardKey);
    });

    public static final List<KeyBinding> VERTICAL_MOVE = Util.make(() -> {
        MinecraftClient client = MinecraftClient.getInstance();
        GameOptions options = client.options;
        return List.of(options.sneakKey, options.jumpKey);
    });

    public static void onEndClientTick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        NoClipManager clipping = NoClipManager.INSTANCE;
        if (clipping.canClip()) {
            PlayerAbilities abilities = player.getAbilities();
            NoClipConfig config = NoClipClient.getConfig();

            boolean prev = clipping.isClipping();
            boolean curr = TOGGLE_NOCLIP.isPressed() && !(config.onlyCreative && !client.interactionManager.getCurrentGameMode().isCreative());
            if (prev != curr) {
                if (clipping.setClipping(curr)) {
                    // set flying
                    if (config.enableFlightOnClip) {
                        if (abilities.allowFlying) abilities.flying = true;
                    }
                }

                clipping.updateClipping();
            }

            if (abilities.flying) {
                if (config.snappyFlight && (!config.snappyFlightRequiresClipping || clipping.isClipping())) {
                    if (HORIZONTAL_MOVE.stream().noneMatch(KeyBinding::isPressed)) {
                        Vec3d velocity = player.getVelocity();
                        player.setVelocity(0.0D, velocity.getY(), 0.0D);
                    }
                    if (VERTICAL_MOVE.stream().noneMatch(KeyBinding::isPressed)) {
                        Vec3d velocity = player.getVelocity();
                        player.setVelocity(velocity.getX(), 0.0D, velocity.getZ());
                    }
                }
            }
        }
    }
}
