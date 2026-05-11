package fr.mitefr.entity;

import fr.mitefr.data.MitePlayerDataHolder;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

/**
 * Spectre de Brume — vole dans les marais la nuit,
 * draine la Condition du joueur au contact.
 */
public class SwampSpecterEntity extends FlyingEntity {

    private int conditionDrainCooldown = 0;

    public SwampSpecterEntity(EntityType<? extends FlyingEntity> type, World world) {
        super(type, world);
        this.setNoGravity(true);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return FlyingEntity.createMobAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH,         8.0)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED,     0.22)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,      1.0)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE,       20.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new LookAtEntityGoal(this, PlayerEntity.class, 12.0f));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient()) return;

        int light = getWorld().getLightLevel(LightType.BLOCK, getBlockPos());
        if (light > 7) {
            setTarget(null);
            addVelocity(-getVelocity().x * 0.5, 0.1, -getVelocity().z * 0.5);
            return;
        }

        // Move towards target
        if (getTarget() instanceof PlayerEntity player) {
            double dx = player.getX() - getX();
            double dy = player.getY() - getY();
            double dz = player.getZ() - getZ();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist > 2.0) {
                double speed = 0.02;
                addVelocity(dx / dist * speed, dy / dist * speed, dz / dist * speed);
            }

            conditionDrainCooldown--;
            if (conditionDrainCooldown <= 0 && dist < 2.0) {
                drainCondition(player);
                conditionDrainCooldown = 40;
            }
        }
    }

    private void drainCondition(PlayerEntity player) {
        var data = ((MitePlayerDataHolder) player).mitefr_getData();
        data.modifyCondition(-10);

        player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
            net.minecraft.entity.effect.StatusEffects.WEAKNESS, 100, 0, false, true, true));

        player.sendMessage(
            Text.literal("☠ Le Spectre de Brume draine votre énergie vitale !")
                .formatted(Formatting.DARK_PURPLE), true);

        player.damage(player.getDamageSources().generic(), 0.5f);
    }

    @Override
    protected net.minecraft.sound.SoundEvent getAmbientSound() {
        return net.minecraft.sound.SoundEvents.ENTITY_VEX_AMBIENT;
    }

    @Override
    protected net.minecraft.sound.SoundEvent getHurtSound(
            net.minecraft.entity.damage.DamageSource source) {
        return net.minecraft.sound.SoundEvents.ENTITY_VEX_HURT;
    }

    @Override
    protected net.minecraft.sound.SoundEvent getDeathSound() {
        return net.minecraft.sound.SoundEvents.ENTITY_VEX_DEATH;
    }
}
