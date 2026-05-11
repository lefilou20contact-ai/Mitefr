package fr.mitefr.entity;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;

/**
 * Charognard des Collines — attaque en meute.
 *
 * Comportement :
 * - Détecte les joueurs à 32 blocs
 * - Appelle les autres Charognards proches (horde)
 * - Rapide, difficile de fuir
 * - Drop : os, cuir, parfois silex
 */
public class HillScavengerEntity extends HostileEntity {

    private int packCallTimer = 0;

    public HillScavengerEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH,          16.0)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED,       0.38)  // Rapide
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,        3.5)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE,         32.0)  // 2x vanilla
            .add(EntityAttributes.GENERIC_ARMOR,                2.0);
    }

    @Override
    protected void initGoals() {
        // Attaque le joueur
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.4, true));
        this.goalSelector.add(2, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(4, new LookAroundGoal(this));

        // Cibles
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(2, new RevengeGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient()) return;

        // Appel de meute toutes les 5 secondes si le joueur est proche
        packCallTimer++;
        if (packCallTimer >= 100 && getTarget() instanceof PlayerEntity) {
            packCallTimer = 0;
            callPack();
        }
    }

    /**
     * Alerte tous les Charognards dans un rayon de 24 blocs
     * pour qu'ils ciblent le même joueur.
     */
    private void callPack() {
        if (!(getWorld() instanceof ServerWorld sw)) return;
        if (!(getTarget() instanceof PlayerEntity target)) return;

        List<HillScavengerEntity> pack = sw.getEntitiesByType(
            MiteFREntities.HILL_SCAVENGER,
            getBoundingBox().expand(24),
            e -> e != this && e.getTarget() == null
        );

        for (HillScavengerEntity ally : pack) {
            ally.setTarget(target);
        }

        if (!pack.isEmpty()) {
            target.sendMessage(
                Text.literal("☠ Vous entendez des grognements se rapprocher...")
                    .formatted(Formatting.DARK_RED), true);
        }
    }

    @Override
    protected net.minecraft.sound.SoundEvent getAmbientSound() {
        return net.minecraft.sound.SoundEvents.ENTITY_WOLF_GROWL;
    }

    @Override
    protected net.minecraft.sound.SoundEvent getHurtSound(
            net.minecraft.entity.damage.DamageSource source) {
        return net.minecraft.sound.SoundEvents.ENTITY_WOLF_HURT;
    }

    @Override
    protected net.minecraft.sound.SoundEvent getDeathSound() {
        return net.minecraft.sound.SoundEvents.ENTITY_WOLF_DEATH;
    }
}
