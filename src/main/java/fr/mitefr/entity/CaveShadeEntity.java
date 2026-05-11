package fr.mitefr.entity;

import fr.mitefr.data.MitePlayerDataHolder;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

/**
 * Ombre Rampante — invisible dans l'obscurité, attaque par surprise.
 *
 * Comportement :
 * - Invisible (getOpacity = 0) quand le niveau de lumière < 4
 * - Devient visible en lumière > 7 et fuit
 * - Attaque critique si le joueur ne la voit pas (dans le dos ou invisible)
 * - Drop : fragments d'ombre (aucun usage pour l'instant, lore item)
 */
public class CaveShadeEntity extends HostileEntity {

    public CaveShadeEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
        this.setInvisible(true); // Commence invisible
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH,         10.0)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED,      0.30)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,       5.0)  // Haute
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE,        24.0)
            .add(EntityAttributes.GENERIC_ARMOR,                0.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.add(2, new WanderAroundFarGoal(this, 0.6));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(2, new RevengeGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        int lightLevel = getWorld().getLightLevel(LightType.BLOCK, getBlockPos());

        if (lightLevel >= 7) {
            // Lumière → visible et fuit
            setInvisible(false);
            if (getTarget() != null) {
                // Fuit la lumière : abandon de la cible
                setTarget(null);
            }
        } else {
            // Obscurité → invisible
            setInvisible(true);
        }
    }

    @Override
    public boolean damage(net.minecraft.entity.damage.DamageSource source, float amount) {
        // Révéler lors d'un coup
        if (isInvisible()) {
            setInvisible(false);
            if (source.getAttacker() instanceof PlayerEntity player) {
                player.sendMessage(
                    Text.literal("☠ L'Ombre Rampante est révélée !")
                        .formatted(Formatting.DARK_GRAY), true);
            }
        }
        return super.damage(source, amount);
    }

    @Override
    protected net.minecraft.sound.SoundEvent getAmbientSound() {
        return net.minecraft.sound.SoundEvents.ENTITY_PHANTOM_AMBIENT;
    }

    @Override
    protected net.minecraft.sound.SoundEvent getHurtSound(
            net.minecraft.entity.damage.DamageSource source) {
        return net.minecraft.sound.SoundEvents.ENTITY_PHANTOM_HURT;
    }

    @Override
    protected net.minecraft.sound.SoundEvent getDeathSound() {
        return net.minecraft.sound.SoundEvents.ENTITY_PHANTOM_DEATH;
    }
}
