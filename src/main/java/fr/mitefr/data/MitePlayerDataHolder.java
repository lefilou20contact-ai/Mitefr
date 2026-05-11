package fr.mitefr.data;

/**
 * Interface injectée via mixin dans PlayerEntity
 * pour porter les données MITE-FR directement sur l'entité.
 */
public interface MitePlayerDataHolder {
    PlayerMiteData mitefr_getData();
    void           mitefr_setData(PlayerMiteData data);
}
