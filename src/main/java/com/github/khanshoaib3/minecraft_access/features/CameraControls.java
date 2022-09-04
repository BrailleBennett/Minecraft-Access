package com.github.khanshoaib3.minecraft_access.features;

import com.github.khanshoaib3.minecraft_access.MainClass;
import com.github.khanshoaib3.minecraft_access.utils.ClientPlayerEntityUtils;
import com.mojang.text2speech.Narrator;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class CameraControls {
    private KeyBinding up, right, down, left;
    private MinecraftClient minecraftClient;

    private final float delta90Degrees = 600f; // 90 / 0.15
    private final float normalRotatingAngle = 22.5f; //TODO add this to config
    private final float normalRotatingDeltaAngle = delta90Degrees / (90 / normalRotatingAngle);
    private final float modifiedRotatingAngle = 11.25f; //TODO add this to config
    private final float modifiedRotatingDeltaAngle = delta90Degrees / (90 / modifiedRotatingAngle);

    public CameraControls() {
        init();
    }

    private void init() {
        String categoryTranslationKey = "Camera Controls"; //TODO add translation key instead

        up = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Up", //TODO add translation key instead
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                categoryTranslationKey
        ));

        right = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Right", //TODO add translation key instead
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_L,
                categoryTranslationKey
        ));

        down = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Down", //TODO add translation key instead
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                categoryTranslationKey
        ));

        left = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Left", //TODO add translation key instead
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                categoryTranslationKey
        ));
    }

    public void update(MinecraftClient minecraftClient) {
        try {
            if (minecraftClient == null) return;
            if (minecraftClient.currentScreen != null) return; //Prevent running if any screen is opened
            this.minecraftClient = minecraftClient;

            boolean isLeftAltPressed = InputUtil.isKeyPressed(
                    MinecraftClient.getInstance().getWindow().getHandle(),
                    InputUtil.fromTranslationKey("key.keyboard.left.alt").getCode()
            );

            boolean isRightAltPressed = InputUtil.isKeyPressed(
                    MinecraftClient.getInstance().getWindow().getHandle(),
                    InputUtil.fromTranslationKey("key.keyboard.right.alt").getCode()
            );

            if (up.wasPressed()) {
                upKeyHandler(isLeftAltPressed, isRightAltPressed);
            }

            if (right.wasPressed()) {
                rightKeyHandler(isLeftAltPressed, isRightAltPressed);
            }

            if (down.wasPressed()) {
                downKeyHandler(isLeftAltPressed, isRightAltPressed);
            }

            if (left.wasPressed()) {
                leftKeyHandler(isLeftAltPressed, isRightAltPressed);
            }
        } catch (Exception e) {
            MainClass.errorLog("Error encountered in Camera Controls feature.\n");
            e.printStackTrace();
        }
    }

    private void upKeyHandler(boolean isLeftAltPressed, boolean isRightAltPressed) {
        if (minecraftClient.player == null) return;

        if (isRightAltPressed && !isLeftAltPressed) {
            lookNorth();
        } else {
            if (isLeftAltPressed)
                minecraftClient.player.changeLookDirection(0, -modifiedRotatingDeltaAngle);
            else
                minecraftClient.player.changeLookDirection(0, -normalRotatingDeltaAngle);

            MainClass.infoLog("Rotating %f degrees upwards.".formatted((isLeftAltPressed) ? modifiedRotatingAngle : normalRotatingAngle));
            if(ClientPlayerEntityUtils.getVerticalFacingDirectionInWords()!=null)
                Narrator.getNarrator().say(ClientPlayerEntityUtils.getVerticalFacingDirectionInWords(), true);
        }
    }

    private void rightKeyHandler(boolean isLeftAltPressed, boolean isRightAltPressed) {
        if (minecraftClient.player == null) return;

        if (isRightAltPressed && !isLeftAltPressed) {
            lookEast();
        } else {
            if (isLeftAltPressed)
                minecraftClient.player.changeLookDirection(modifiedRotatingDeltaAngle, 0);
            else
                minecraftClient.player.changeLookDirection(normalRotatingDeltaAngle, 0);

            MainClass.infoLog("Rotating %f degrees rightwards.".formatted((isLeftAltPressed) ? modifiedRotatingAngle : normalRotatingAngle));
            if(ClientPlayerEntityUtils.getHorizontalFacingDirectionInWords(true)!=null)
                Narrator.getNarrator().say(ClientPlayerEntityUtils.getHorizontalFacingDirectionInWords(true), true);
        }
    }

    private void downKeyHandler(boolean isLeftAltPressed, boolean isRightAltPressed) {
        if (minecraftClient.player == null) return;

        if (isRightAltPressed && !isLeftAltPressed) {
            lookSouth();
        } else {
            if (isLeftAltPressed)
                minecraftClient.player.changeLookDirection(0, modifiedRotatingDeltaAngle);
            else
                minecraftClient.player.changeLookDirection(0, normalRotatingDeltaAngle);

            MainClass.infoLog("Rotating %f degrees downwards.".formatted((isLeftAltPressed) ? modifiedRotatingAngle : normalRotatingAngle));
            if(ClientPlayerEntityUtils.getVerticalFacingDirectionInWords()!=null)
                Narrator.getNarrator().say(ClientPlayerEntityUtils.getVerticalFacingDirectionInWords(), true);
        }
    }

    private void leftKeyHandler(boolean isLeftAltPressed, boolean isRightAltPressed) {
        if (minecraftClient.player == null) return;

        if (isRightAltPressed && !isLeftAltPressed) {
            lookWest();
        } else {
            if (isLeftAltPressed)
                minecraftClient.player.changeLookDirection(-modifiedRotatingDeltaAngle, 0);
            else
                minecraftClient.player.changeLookDirection(-normalRotatingDeltaAngle, 0);

            MainClass.infoLog("Rotating %f degrees leftwards.".formatted((isLeftAltPressed) ? modifiedRotatingAngle : normalRotatingAngle));
            if(ClientPlayerEntityUtils.getHorizontalFacingDirectionInWords(true)!=null)
                Narrator.getNarrator().say(ClientPlayerEntityUtils.getHorizontalFacingDirectionInWords(true), true);
        }
    }

    private void lookNorth() {
        if (minecraftClient.player == null) return;

        Vec3d playerBlockPosition = minecraftClient.player.getPos();
        Vec3d northBlockPosition = new Vec3d(playerBlockPosition.x + 0, playerBlockPosition.y + 0, playerBlockPosition.z - 1);

        minecraftClient.player.lookAt(EntityAnchorArgumentType.EntityAnchor.FEET, northBlockPosition);
        MainClass.infoLog("Looking north");
        Narrator.getNarrator().say("North", true); //TODO use i18n instead
    }

    private void lookEast() {
        if (minecraftClient.player == null) return;

        Vec3d playerBlockPosition = minecraftClient.player.getPos();
        Vec3d eastBlockPosition = new Vec3d(playerBlockPosition.x + 1, playerBlockPosition.y + 0, playerBlockPosition.z + 0);

        minecraftClient.player.lookAt(EntityAnchorArgumentType.EntityAnchor.FEET, eastBlockPosition);
        MainClass.infoLog("Looking east");
        Narrator.getNarrator().say("East", true); //TODO use i18n instead
    }

    private void lookWest() {
        if (minecraftClient.player == null) return;

        Vec3d playerBlockPosition = minecraftClient.player.getPos();
        Vec3d westBlockPosition = new Vec3d(playerBlockPosition.x - 1, playerBlockPosition.y + 0, playerBlockPosition.z + 0);

        minecraftClient.player.lookAt(EntityAnchorArgumentType.EntityAnchor.FEET, westBlockPosition);
        MainClass.infoLog("Looking west");
        Narrator.getNarrator().say("West", true); //TODO use i18n instead
    }

    private void lookSouth() {
        if (minecraftClient.player == null) return;

        Vec3d playerBlockPosition = minecraftClient.player.getPos();
        Vec3d southBlockPosition = new Vec3d(playerBlockPosition.x + 0, playerBlockPosition.y + 0, playerBlockPosition.z + 1);

        minecraftClient.player.lookAt(EntityAnchorArgumentType.EntityAnchor.FEET, southBlockPosition);
        MainClass.infoLog("Looking south");
        Narrator.getNarrator().say("South", true); //TODO use i18n instead
    }
}