package fr.mitefr.data;

import fr.mitefr.MiteFRConstants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;
import java.util.Set;

/**
 * Toutes les données personnalisées d'un joueur MITE-FR.
 * Sauvegardées / chargées via NBT (PersistentState ou mixin sur PlayerEntity).
 */
public class PlayerMiteData {

    // ── Ère ────────────────────────────────────────────────────────
    private Era era = Era.SILEX;

    // ── Condition physique (0–100) ─────────────────────────────────
    private int condition = MiteFRConstants.CONDITION_MAX;

    // ── Température (°C simulés) ───────────────────────────────────
    private float temperature = 20f;

    // ── Soif (0–20) ───────────────────────────────────────────────
    private int thirst = MiteFRConstants.THIRST_MAX;

    // ── Sommeil ───────────────────────────────────────────────────
    private long awakeTicksAccumulated = 0L;
    private int  sleepDeprivationLevel = 0; // 0=ok 1=fatigué 2=épuisé 3=délirant

    // ── Maladies actives ──────────────────────────────────────────
    private final Set<Disease> activeDiseases = EnumSet.noneOf(Disease.class);

    // ── Durées des maladies (ticks restants) ──────────────────────
    private final int[] diseaseDurations = new int[Disease.values().length];

    // ── Campement (spawn de mort) ─────────────────────────────────
    private BlockPos campPos    = null;
    private String   campDimKey = null;

    // ── Dette d'XP (système Legs de Fer) ─────────────────────────
    private int xpDebt = 0;

    // ── Timer interne ─────────────────────────────────────────────
    private int thirstTimer        = 0;
    private int gangreneTimer      = 0;
    private int temperatureTimer   = 0;
    private int conditionTimer     = 0;

    // ══════════════════════════════════════════════════════════════
    // Sérialisation NBT
    // ══════════════════════════════════════════════════════════════

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt   (MiteFRConstants.NBT_ERA,              era.level);
        nbt.putInt   (MiteFRConstants.NBT_CONDITION,        condition);
        nbt.putFloat (MiteFRConstants.NBT_TEMPERATURE,      temperature);
        nbt.putInt   (MiteFRConstants.NBT_THIRST,           thirst);
        nbt.putLong  (MiteFRConstants.NBT_AWAKE_TICKS,      awakeTicksAccumulated);
        nbt.putInt   (MiteFRConstants.NBT_SLEEP_DEPRIVATION,sleepDeprivationLevel);
        nbt.putInt   (MiteFRConstants.NBT_XP_DEBT,          xpDebt);

        if (campPos != null) {
            nbt.putInt   (MiteFRConstants.NBT_CAMP_X,   campPos.getX());
            nbt.putInt   (MiteFRConstants.NBT_CAMP_Y,   campPos.getY());
            nbt.putInt   (MiteFRConstants.NBT_CAMP_Z,   campPos.getZ());
            nbt.putString(MiteFRConstants.NBT_CAMP_DIM, campDimKey);
        }

        // Maladies
        NbtList diseaseList = new NbtList();
        for (Disease d : activeDiseases) {
            NbtCompound dc = new NbtCompound();
            dc.putString("id",       d.id);
            dc.putInt   ("duration", diseaseDurations[d.ordinal()]);
            diseaseList.add(dc);
        }
        nbt.put(MiteFRConstants.NBT_DISEASES, diseaseList);

        return nbt;
    }

    public static PlayerMiteData fromNbt(NbtCompound nbt) {
        PlayerMiteData data = new PlayerMiteData();
        data.era                      = Era.fromLevel(nbt.getInt(MiteFRConstants.NBT_ERA));
        data.condition                = nbt.getInt   (MiteFRConstants.NBT_CONDITION);
        data.temperature              = nbt.getFloat (MiteFRConstants.NBT_TEMPERATURE);
        data.thirst                   = nbt.getInt   (MiteFRConstants.NBT_THIRST);
        data.awakeTicksAccumulated    = nbt.getLong  (MiteFRConstants.NBT_AWAKE_TICKS);
        data.sleepDeprivationLevel    = nbt.getInt   (MiteFRConstants.NBT_SLEEP_DEPRIVATION);
        data.xpDebt                   = nbt.getInt   (MiteFRConstants.NBT_XP_DEBT);

        if (nbt.contains(MiteFRConstants.NBT_CAMP_X)) {
            data.campPos    = new BlockPos(
                nbt.getInt(MiteFRConstants.NBT_CAMP_X),
                nbt.getInt(MiteFRConstants.NBT_CAMP_Y),
                nbt.getInt(MiteFRConstants.NBT_CAMP_Z)
            );
            data.campDimKey = nbt.getString(MiteFRConstants.NBT_CAMP_DIM);
        }

        // Maladies
        NbtList diseaseList = nbt.getList(MiteFRConstants.NBT_DISEASES, 10);
        for (int i = 0; i < diseaseList.size(); i++) {
            NbtCompound dc = diseaseList.getCompound(i);
            String id = dc.getString("id");
            for (Disease d : Disease.values()) {
                if (d.id.equals(id)) {
                    data.activeDiseases.add(d);
                    data.diseaseDurations[d.ordinal()] = dc.getInt("duration");
                }
            }
        }

        return data;
    }

    // ══════════════════════════════════════════════════════════════
    // Accesseurs
    // ══════════════════════════════════════════════════════════════

    public Era  getEra()         { return era; }
    public void setEra(Era era)  { this.era = era; }

    public int  getCondition()           { return condition; }
    public void setCondition(int v)      { this.condition = Math.max(0, Math.min(MiteFRConstants.CONDITION_MAX, v)); }
    public void modifyCondition(int delta){ setCondition(this.condition + delta); }

    public float getTemperature()        { return temperature; }
    public void  setTemperature(float t) { this.temperature = t; }

    public int  getThirst()              { return thirst; }
    public void setThirst(int v)         { this.thirst = Math.max(0, Math.min(MiteFRConstants.THIRST_MAX, v)); }
    public void modifyThirst(int delta)  { setThirst(this.thirst + delta); }

    public long getAwakeTicks()              { return awakeTicksAccumulated; }
    public void setAwakeTicks(long v)        { this.awakeTicksAccumulated = v; }
    public void incrementAwakeTicks()        { this.awakeTicksAccumulated++; }
    public void resetAwakeTicks()            { this.awakeTicksAccumulated = 0; sleepDeprivationLevel = 0; }

    public int  getSleepDeprivation()        { return sleepDeprivationLevel; }
    public void setSleepDeprivation(int v)   { this.sleepDeprivationLevel = v; }

    public boolean hasDisease(Disease d)     { return activeDiseases.contains(d); }
    public void addDisease(Disease d)        {
        activeDiseases.add(d);
        diseaseDurations[d.ordinal()] = d.naturalDurationTicks;
    }
    public void removeDisease(Disease d)     { activeDiseases.remove(d); diseaseDurations[d.ordinal()] = 0; }
    public Set<Disease> getDiseases()        { return activeDiseases; }
    public int  getDiseaseDuration(Disease d){ return diseaseDurations[d.ordinal()]; }
    public void tickDisease(Disease d)       { if (diseaseDurations[d.ordinal()] > 0) diseaseDurations[d.ordinal()]--; }

    public BlockPos getCampPos()             { return campPos; }
    public String   getCampDim()             { return campDimKey; }
    public void setCamp(BlockPos pos, String dim) { this.campPos = pos; this.campDimKey = dim; }
    public boolean hasCamp()                 { return campPos != null; }

    public int  getXpDebt()                  { return xpDebt; }
    public void setXpDebt(int v)             { this.xpDebt = Math.max(0, v); }
    public void addXpDebt(int v)             { this.xpDebt += v; }

    // Timers internes (non sauvegardés, reset à chaque connexion)
    public int  getThirstTimer()             { return thirstTimer; }
    public void setThirstTimer(int v)        { this.thirstTimer = v; }
    public int  getGangreneTimer()           { return gangreneTimer; }
    public void setGangreneTimer(int v)      { this.gangreneTimer = v; }
    public int  getTemperatureTimer()        { return temperatureTimer; }
    public void setTemperatureTimer(int v)   { this.temperatureTimer = v; }
    public int  getConditionTimer()          { return conditionTimer; }
    public void setConditionTimer(int v)     { this.conditionTimer = v; }
}
