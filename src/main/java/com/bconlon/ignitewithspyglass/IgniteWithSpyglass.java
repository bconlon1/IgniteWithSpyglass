package com.bconlon.ignitewithspyglass;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("ignitewithspyglass")
public class IgniteWithSpyglass
{
    private static final Logger LOGGER = LogManager.getLogger();
    private int flammabilityTimer;

    public IgniteWithSpyglass() {
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, IgniteWithSpyglassConfig.COMMON_SPEC);
    }

    @SubscribeEvent
    public void onViewEntity(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        LivingEntity targetedEntity = null;
        if (player.getUseItem().getItem() == Items.SPYGLASS) {
            Vec3 eyePosition = player.getEyePosition();
            Vec3 viewVector = player.getViewVector(1.0F);
            Vec3 viewRange = eyePosition.add(viewVector.x * 100.0D, viewVector.y * 100.0D, viewVector.z * 100.0D);
            EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(player.level, player, eyePosition, viewRange, (new AABB(eyePosition, viewRange)).inflate(1.0D), (entity) -> !entity.isSpectator(), 0.0F);
            if (entityHitResult != null && entityHitResult.getType() == HitResult.Type.ENTITY) {
                Entity entity = entityHitResult.getEntity();
                if (entity instanceof LivingEntity livingEntity) {
                    if (player.hasLineOfSight(livingEntity) && !livingEntity.isOnFire() && livingEntity.getRemainingFireTicks() <= 0 && !livingEntity.fireImmune()) {
                        targetedEntity = livingEntity;
                    }
                }
            }
        }

        if (targetedEntity != null) {
            if (!targetedEntity.level.isClientSide) {
                int maxTimer = IgniteWithSpyglassConfig.COMMON.flammability_timer.get();
                int smokeTimer = (int) (maxTimer * 0.65);
                int largeSmokeTimer = (int) (maxTimer * 0.9);
                if (maxTimer > 0) {
                    this.flammabilityTimer++;
                    if (this.flammabilityTimer < maxTimer) {
                        if (this.flammabilityTimer >= smokeTimer) {
                            this.spawnParticles(targetedEntity, ParticleTypes.SMOKE, 2);
                        }
                        if (this.flammabilityTimer >= largeSmokeTimer) {
                            this.spawnParticles(targetedEntity, ParticleTypes.LARGE_SMOKE, 1);
                        }
                    } else {
                        this.ignite(targetedEntity);
                    }
                } else {
                    this.ignite(targetedEntity);
                }
                if (targetedEntity.isOnFire() && targetedEntity.getRemainingFireTicks() > 0) {
                    this.flammabilityTimer = 0;
                }
            }
        } else {
            this.flammabilityTimer = 0;
        }
    }

    private <T extends ParticleOptions> void spawnParticles(LivingEntity targetedEntity, T particle, int amount) {
        if (targetedEntity.level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < amount; ++i) {
                serverLevel.sendParticles(particle, targetedEntity.getRandomX(0.6D), targetedEntity.getRandomY(), targetedEntity.getRandomZ(0.6D), 1, 0.0D, 0.0D, 0.0D, 0.0F);
            }
        }
    }

    private void ignite(LivingEntity targetedEntity) {
        targetedEntity.playSound(SoundEvents.FIRECHARGE_USE, 1.0F, (targetedEntity.getRandom().nextFloat() - targetedEntity.getRandom().nextFloat()) * 0.1F + 0.5F);
        if (targetedEntity instanceof Creeper creeper) {
            creeper.ignite();
        } else {
            targetedEntity.setSecondsOnFire(100);
        }
    }
}
