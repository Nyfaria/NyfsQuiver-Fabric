package thenyfaria.nyfsquivers.config;

import draylar.omegaconfig.api.Comment;
import draylar.omegaconfig.api.Config;
import net.minecraft.sounds.SoundEvents;

import java.util.Arrays;
import java.util.List;

public class NyfsQuiversConfig implements Config {
    public List<QuiverInfo> quivers = Arrays.asList(
            QuiverInfo.of("basic", 9, 1, false, SoundEvents.ARMOR_EQUIP_LEATHER),
            QuiverInfo.of("iron", 9, 2, false, SoundEvents.ARMOR_EQUIP_IRON),
            QuiverInfo.of("copper", 9, 2, false, SoundEvents.ARMOR_EQUIP_IRON),
            QuiverInfo.of("gold", 9, 3, false, SoundEvents.ARMOR_EQUIP_GOLD),
            QuiverInfo.of("silver", 9, 3, false, SoundEvents.ARMOR_EQUIP_GOLD),
            QuiverInfo.of("diamond", 9, 4, false, SoundEvents.ARMOR_EQUIP_DIAMOND),
            QuiverInfo.of("netherite", 9, 5, true, SoundEvents.ARMOR_EQUIP_LEATHER)
    );

    @Comment(value = "Whether Backpacks should play a sound when opened.")
    public boolean playSound = true;

    @Comment(value = "Position of HUD")
    public int xpos = 0;
    public int ypos = 0;
    @Override
    public String getName() {
        return "nyfsquivers";
    }
}
