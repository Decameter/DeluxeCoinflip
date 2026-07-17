/*
 * DeluxeCoinflip Plugin
 * Copyright (c) 2021 - 2025 Zithium Studios. All rights reserved.
 */

package net.zithium.deluxecoinflip.hook;

import net.leonemc.neon.spigot.features.economy.EconomyService;
import net.leonemc.neon.spigot.features.economy.events.CurrencyRegisteredEvent;
import net.leonemc.neon.spigot.features.economy.model.Currency;
import net.zithium.deluxecoinflip.DeluxeCoinflipPlugin;
import net.zithium.deluxecoinflip.config.ConfigType;
import net.zithium.deluxecoinflip.economy.provider.impl.NeonCurrencyProvider;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class NeonHook implements Listener {

    private final DeluxeCoinflipPlugin plugin;

    public NeonHook(@NotNull DeluxeCoinflipPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void registerCurrencies() {
        for (Currency currency : EconomyService.INSTANCE.getCurrencies()) {
            registerCurrency(currency);
        }
    }

    @EventHandler
    public void onCurrencyRegistered(@NotNull CurrencyRegisteredEvent event) {
        registerCurrency(event.getCurrency());
    }

    private void registerCurrency(Currency currency) {
        String key = "NEON_" + currency.getKey().toUpperCase();

        FileConfiguration config = plugin.getConfigHandler(ConfigType.CONFIG).getConfig();
        ConfigurationSection providers = config.getConfigurationSection("settings.providers");
        if (providers == null) {
            providers = config.createSection("settings.providers");
        }

        ConfigurationSection section = providers.getConfigurationSection(key);
        if (section == null) {
            section = providers.createSection(key);
            section.set("enabled", true);
            section.set("display_currency_name", currency.getDisplayName());
            plugin.getConfigHandler(ConfigType.CONFIG).save();
        }

        if (!section.getBoolean("enabled", true)) {
            return;
        }

        NeonCurrencyProvider provider = new NeonCurrencyProvider(currency);
        provider.setCurrencyDisplayName(section.getString("display_currency_name", currency.getDisplayName()));
        provider.onEnable();
        plugin.getEconomyManager().registerEconomyProvider(provider, null);
    }
}
