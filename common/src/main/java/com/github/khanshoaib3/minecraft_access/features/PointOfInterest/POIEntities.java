package com.github.khanshoaib3.minecraft_access.features.PointOfInterest;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EyeOfEnderEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

public class POIEntities {
    private boolean shouldRun = true;

    public static TreeMap<Double, Entity> passiveEntity = new TreeMap<>();
    public static TreeMap<Double, Entity> hostileEntity = new TreeMap<>();
    public static TreeMap<Double, Entity> eyeOfEnderEntity = new TreeMap<>();

    public void update() {
        if (!this.shouldRun) return;
        try {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();

            int range = 6;
            float volume = 0.25f;
            int delayInMilliseconds = 3000;

            if (minecraftClient == null) return;
            if (minecraftClient.player == null) return;
            if (minecraftClient.world == null) return;
            if (minecraftClient.currentScreen != null) return; //Prevent running if any screen is opened

            passiveEntity = new TreeMap<>();
            hostileEntity = new TreeMap<>();
            eyeOfEnderEntity = new TreeMap<>();

            for (Entity i : minecraftClient.world.getEntities()) {
                if (!(i instanceof MobEntity || i instanceof ItemEntity || i instanceof EyeOfEnderEntity || (i instanceof PlayerEntity && i != minecraftClient.player)))
                    continue;

                BlockPos blockPos = i.getBlockPos();

                Vec3d entityVec3d = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                Vec3d playerVec3d = new Vec3d(minecraftClient.player.getBlockPos().getX(), minecraftClient.player.getBlockPos().getY(),
                        minecraftClient.player.getBlockPos().getZ());
                Double distance = entityVec3d.distanceTo(playerVec3d);

                if (distance <= range) {
                    String entityString = i + "";
                    int z = entityString.indexOf("/");
                    int y = entityString.indexOf(",", z);
                    entityString = entityString.substring(z, y);

                    if (i instanceof EyeOfEnderEntity && distance <= 0.2) {
                        eyeOfEnderEntity.put(distance, i);
//                        LockingHandler.lockedOnEntity = i;
//                        LockingHandler.lockedOnBlockEntries = "";
//
//                        LockingHandler.lockedOnBlock = null;
//                        LockingHandler.isLockedOntoLadder = false;

                    } else if (i instanceof PassiveEntity) {
                        passiveEntity.put(distance, i);
                            minecraftClient.world.playSound(minecraftClient.player, blockPos, SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), SoundCategory.BLOCKS,
                                    volume, 0f);
                    } else if (i instanceof HostileEntity) {
                        hostileEntity.put(distance, i);
                            minecraftClient.world.playSound(minecraftClient.player, blockPos, SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), SoundCategory.BLOCKS,
                                    volume, 2f);
                    } else if (i instanceof ItemEntity) {
                        if (i.isOnGround()) {
                                minecraftClient.world.playSound(minecraftClient.player, blockPos, SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON,
                                        SoundCategory.BLOCKS, volume, 2f);
                            }
                    } else if (i instanceof PlayerEntity) {
                        passiveEntity.put(distance, i);
                            minecraftClient.world.playSound(minecraftClient.player, blockPos, SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), SoundCategory.BLOCKS,
                                    volume, 0f);
                    }
                }
            }
//        POIHandler.passiveEntity = passiveEntity;
//        POIHandler.hostileEntity = hostileEntity;
//        POIHandler.eyeOfEnderEntity = eyeOfEnderEntity;


            // Pause the execution of this feature for 250 milliseconds
            // TODO Remove Timer
            shouldRun = false;
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    shouldRun = true;
                }
            };
            new Timer().schedule(timerTask, delayInMilliseconds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}