package com.github.khanshoaib3.minecraft_access.features.point_of_interest;

import com.github.khanshoaib3.minecraft_access.config.config_maps.POIEntitiesConfigMap;
import com.github.khanshoaib3.minecraft_access.config.config_maps.POIMarkingConfigMap;
import com.github.khanshoaib3.minecraft_access.utils.WorldUtils;
import com.github.khanshoaib3.minecraft_access.utils.condition.Interval;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;
import java.util.TreeMap;
import java.util.function.Predicate;

/**
 * Scans the area for entities, groups them and plays a sound at their location.
 */
@Slf4j
public class POIEntities {
    private TreeMap<Double, Entity> passiveEntity = new TreeMap<>();
    private TreeMap<Double, Entity> hostileEntity = new TreeMap<>();
    private TreeMap<Double, Entity> bossEntities = new TreeMap<>();
    private TreeMap<Double, Entity> markedEntities = new TreeMap<>();
    private TreeMap<Double, Entity> vehicleEntities = new TreeMap<>();
    private TreeMap<Double, Entity> playerEntities = new TreeMap<>();

    private int range;
    private boolean playSound;
    private float volume;
    private Interval interval;
    private boolean enabled;

    private static final POIEntities instance;
    private boolean onPOIMarkingNow = false;
    private Predicate<Entity> markedEntity = e -> false;

    static {
        instance = new POIEntities();
    }

    public static POIEntities getInstance() {
        return instance;
    }

    private POIEntities() {
        loadConfigurations();
    }

    public void update(boolean onMarking, Entity markedEntity) {
        this.onPOIMarkingNow = onMarking;
        if (onPOIMarkingNow) setMarkedEntity(markedEntity);
        loadConfigurations();

        if (!enabled) return;
        if (interval != null && !interval.isReady()) return;

        try {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();

            if (minecraftClient == null) return;
            if (minecraftClient.player == null) return;
            if (minecraftClient.world == null) return;
            if (minecraftClient.currentScreen != null) return; //Prevent running if any screen is opened

            passiveEntity = new TreeMap<>();
            hostileEntity = new TreeMap<>();
            bossEntities = new TreeMap<>();
            markedEntities = new TreeMap<>();
            vehicleEntities = new TreeMap<>();
            playerEntities = new TreeMap<>();

            log.debug("POIEntities started.");

            // Copied from PlayerEntity.tickMovement()
            Box scanBox = minecraftClient.player.getBoundingBox().expand(range, range, range);
            List<Entity> entities = minecraftClient.world.getOtherEntities(minecraftClient.player, scanBox);

            for (Entity i : entities) {
                double distance = minecraftClient.player.distanceTo(i);
                BlockPos entityPos = i.getBlockPos();

                if (this.markedEntity.test(i)) {
                    markedEntities.put(distance, i);
                    if (i instanceof HostileEntity) {
                        this.playSoundAt(entityPos, SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 2f);
                    } else {
                        this.playSoundAt(entityPos, SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 0f);
                    }
                }

                if (onPOIMarkingNow && POIMarkingConfigMap.getInstance().isSuppressOtherWhenEnabled()) {
                    log.debug("POIEntities end early by POI marking feature.");
                    return;
                }

                if (i instanceof MobEntity mob && mob.getMaxHealth() >= 80 && !(i instanceof IronGolemEntity)) {
                    bossEntities.put(distance, i);
                    hostileEntity.put(distance, i);
                    this.playSoundAt(entityPos, SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 2f);
                } else if (i instanceof Monster || i instanceof Angerable angerable && angerable.hasAngerTime()) {
                    hostileEntity.put(distance, i);
                    this.playSoundAt(entityPos, SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 2f);
                } else if (i instanceof ItemEntity itemEntity && itemEntity.isOnGround() || i instanceof  PersistentProjectileEntity projectile && projectile.pickupType.equals(PersistentProjectileEntity.PickupPermission.ALLOWED)) {
                    this.playSoundAt(entityPos, SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON, 2f);
                } else if (i instanceof PlayerEntity) {
                    playerEntities.put(distance, i);
                    this.playSoundAt(entityPos, SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 1f);
                } else if (i instanceof VehicleEntity) {
                    vehicleEntities.put(distance, i);
                    this.playSoundAt(entityPos, SoundEvents.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE.value(),  1f);
                } else if(i instanceof PassiveEntity || i instanceof  WaterCreatureEntity) {
                    passiveEntity.put(distance, i);
                    this.playSoundAt(entityPos, SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 0f);
                }
            }
            log.debug("POIEntities end.");

        } catch (Exception e) {
            log.error("An error occurred while executing POIEntities", e);
        }
    }

    private void playSoundAt(BlockPos pos, SoundEvent soundEvent, float pitch) {
        if (!playSound || volume == 0f) return;
        log.debug("Play sound at [x:%d y:%d z%d]".formatted(pos.getX(), pos.getY(), pos.getZ()));
        WorldUtils.playSoundAtPosition(soundEvent, volume, pitch, pos.toCenterPos());
    }

    /**
     * Loads the configs from config.json
     */
    private void loadConfigurations() {
        POIEntitiesConfigMap map = POIEntitiesConfigMap.getInstance();
        this.enabled = map.isEnabled();
        this.range = map.getRange();
        this.playSound = map.isPlaySound();
        this.volume = map.getVolume();
        this.interval = Interval.inMilliseconds(map.getDelay(), this.interval);
    }

    private void setMarkedEntity(Entity entity) {
        if (entity == null) {
            this.markedEntity = e -> false;
        } else {
            // Mark an entity = mark the type of entity (class type)
            Class<? extends Entity> clazz = entity.getClass();
            this.markedEntity = clazz::isInstance;
        }
    }

    public List<TreeMap<Double, Entity>> getLockingCandidates() {
        if (onPOIMarkingNow) {
            if (POIMarkingConfigMap.getInstance().isSuppressOtherWhenEnabled()) {
                return List.of(markedEntities);
            } else {
                return List.of(markedEntities, hostileEntity, passiveEntity, vehicleEntities);
            }
        } else {
            return List.of(hostileEntity, passiveEntity, vehicleEntities);
        }
    }

    public TreeMap<Double, Entity> getAimAssistTargetCandidates() {
        return hostileEntity;
    }

}