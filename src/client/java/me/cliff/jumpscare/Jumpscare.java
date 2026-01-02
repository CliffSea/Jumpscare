package me.cliff.jumpscare;

import me.cliff.jumpscare.config.JumpscareConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import javax.imageio.ImageIO;
import javax.swing.text.JTextComponent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Jumpscare implements ClientModInitializer{

    public static final String MOD_ID = "jumpscare";

    public static final Random random = new Random();
    private static JumpscareConfig jumpscareConfig;
    private static SoundManager soundManager;

    private static final int TOTAL_FRAMES = 13;
    private static final double FRAME_DURATION_SECONDS = 0.04;

    private static final Identifier LAYER = Identifier.of(Jumpscare.MOD_ID, "jump-layer");

    private static PositionedSoundInstance currentSound;

    private static boolean animationStarted = false;
    private static double startTime = 0.0;
    private static int current = 13;

    public static final KeyBinding TRIGGER_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.jumpscare.debugtrigger",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_J,
                    "category.jumpscare.debug"
            )
    );

    @Override
    public void onInitializeClient() {
        AutoConfig.register(JumpscareConfig.class, JanksonConfigSerializer::new);
        soundManager = new SoundManager();
        soundManager.registry();
        jumpscareConfig = AutoConfig.getConfigHolder(JumpscareConfig.class).getConfig();
        HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer.attachLayerBefore(IdentifiedLayer.CHAT, LAYER, Jumpscare::render));

        ClientTickEvents.START_CLIENT_TICK.register(Jumpscare::randomTrigger);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while(TRIGGER_KEY.wasPressed()){
                Jumpscare.trigger();
            }
        });
    }


    private static void render(DrawContext context, RenderTickCounter tickCounter) {

        if (current >= TOTAL_FRAMES) {
            MinecraftClient.getInstance().getSoundManager().stop(currentSound);
            animationStarted = false;
            return;
        }

        if (!animationStarted){
            startTime = Util.getMeasuringTimeMs() / 1000.0;
            animationStarted = true;
            current = 0;
        }

        if (current == TOTAL_FRAMES) {
            context.drawTexture(RenderLayer::getGuiTextured, getFrameTexture(TOTAL_FRAMES),
                    0, 0, 0f, 0f, context.getScaledWindowWidth(), context.getScaledWindowHeight(),
                    context.getScaledWindowWidth(), context.getScaledWindowHeight());
            return;
        }

        double currentTime = Util.getMeasuringTimeMs() / 1000.0;
        double elapsedTime = currentTime - startTime;

        int frameIndex = (int) (elapsedTime / FRAME_DURATION_SECONDS);

        current = Math.min(frameIndex, TOTAL_FRAMES);

        context.drawTexture(RenderLayer::getGuiTextured, getFrameTexture(current),
                0, 0, 0f, 0f, context.getScaledWindowWidth(), context.getScaledWindowHeight(),
                context.getScaledWindowWidth(), context.getScaledWindowHeight());
    }


    public static Identifier getFrameTexture(int frame) {
        return Identifier.of(MOD_ID, String.format("textures/hud/foxy%s.png", frame));
    }

    public static void trigger() {
        animationStarted = false;
        currentSound = PositionedSoundInstance.master(SoundManager.SOUND_EVENT_SCREAM, 1f, 5);
        MinecraftClient.getInstance().getSoundManager().play(currentSound);
        current = 0;
    }

    public static void randomTrigger(MinecraftClient client){
        if (client.world == null) return;
        if (animationStarted) return;
        if (getConfig().probability <= 0) return;
        int rng = random.nextInt(getConfig().probability);
        if (rng == 0){
            trigger();
        }
    }

    public static JumpscareConfig getConfig(){
        return jumpscareConfig;
    }



}
