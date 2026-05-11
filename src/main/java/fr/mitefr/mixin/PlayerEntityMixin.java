package fr.mitefr.mixin;

import fr.mitefr.data.MitePlayerDataHolder;
import fr.mitefr.data.PlayerMiteData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin principal sur PlayerEntity :
 *  1. Porte les données MITE-FR sur l'entité (MitePlayerDataHolder)
 *  2. Sauvegarde/chargement NBT
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements MitePlayerDataHolder {

    @Unique
    private PlayerMiteData mitefr$data = new PlayerMiteData();

    // ── Interface MitePlayerDataHolder ────────────────────────────

    @Override
    public PlayerMiteData mitefr_getData() {
        return mitefr$data;
    }

    @Override
    public void mitefr_setData(PlayerMiteData data) {
        this.mitefr$data = data;
    }

    // ── NBT : Sauvegarde ─────────────────────────────────────────

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void onWriteNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.put("MiteFRData", mitefr$data.toNbt());
    }

    // ── NBT : Chargement ──────────────────────────────────────────

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void onReadNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("MiteFRData")) {
            mitefr$data = PlayerMiteData.fromNbt(nbt.getCompound("MiteFRData"));
        }
    }
}
