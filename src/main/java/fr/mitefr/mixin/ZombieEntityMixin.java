package fr.mitefr.mixin;

import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin sur ZombieEntity :
 * - Tente de grimper les murs d'1 bloc (escalade simple)
 */
@Mixin(ZombieEntity.class)
public abstract class ZombieEntityMixin {

    /**
     * Si le zombie est bloqué par un mur d'1 bloc, il tente de sauter dessus.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        ZombieEntity self = (ZombieEntity) (Object) this;
        if (self.getWorld().isClient()) return;
        if (self.getTarget() == null) return;

        if (self.isOnGround() && self.horizontalCollision) {
            World world = self.getWorld();
            BlockPos frontPos = self.getBlockPos().offset(self.getMovementDirection());
            BlockPos aboveFront = frontPos.up();

            if (world.getBlockState(frontPos).isSolidBlock(world, frontPos)
             && !world.getBlockState(aboveFront).isSolidBlock(world, aboveFront)) {
                self.setJumping(true);
            }
        }
    }
}
