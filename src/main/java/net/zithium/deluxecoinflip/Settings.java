package net.zithium.deluxecoinflip;

import net.kyori.adventure.key.Key;
import net.leonemc.neon.spigot.features.settings.SettingsRegistry;
import net.leonemc.neon.spigot.features.settings.model.SettingDefinition;
import net.leonemc.neon.spigot.features.settings.type.BooleanSetting;
import org.bukkit.Material;

import java.util.List;

public class Settings {

    private static final Key COINFLIP_BROADCASTS_KEY = Key.key("deluxecoinflip", "coinflip-broadcasts");
    public static final SettingDefinition.BooleanDefinition COINFLIP_BROADCASTS = new SettingDefinition.BooleanDefinition(
            COINFLIP_BROADCASTS_KEY,
            "Coinflip Broadcasts",
            List.of("Do you want to see", "coinflip win broadcasts?"),
            Material.GOLD_INGOT,
            new BooleanSetting(COINFLIP_BROADCASTS_KEY, true),
            List.of(),
            List.of(),
            List.of()
    );

    public static void register() {
        SettingsRegistry.INSTANCE.register(COINFLIP_BROADCASTS);
    }

}
