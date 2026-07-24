/*
 * DeluxeCoinflip Plugin
 * Copyright (c) 2021 - 2025 Zithium Studios. All rights reserved.
 */

package net.zithium.deluxecoinflip.listener.game;

import net.zithium.deluxecoinflip.DeluxeCoinflipPlugin;
import net.zithium.deluxecoinflip.config.Messages;
import net.zithium.deluxecoinflip.economy.EconomyManager;
import net.zithium.deluxecoinflip.economy.provider.EconomyProvider;
import net.zithium.deluxecoinflip.game.CoinflipGame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public record GameQuitListener(DeluxeCoinflipPlugin plugin) implements Listener {

    public GameQuitListener(@NotNull DeluxeCoinflipPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        final Player quitter = event.getPlayer();

        if (plugin.getActiveGamesCache().isInGame(quitter.getUniqueId())) {
            return;
        }

        final CoinflipGame game = plugin.getGameManager().getCoinflipGame(quitter.getUniqueId());
        if (game == null) {
            return;
        }

        if (game.isActiveGame()) {
            return;
        }

        if (!game.getPlayerUUID().equals(quitter.getUniqueId())) {
            return;
        }

        final EconomyManager economyManager = plugin.getEconomyManager();
        final EconomyProvider economyProvider = economyManager.getEconomyProvider(game.getProvider());
        if (economyProvider == null) {
            plugin.getLogger().warning("[DeluxeCoinflip] Missing economy provider '" + game.getProvider() + "'; refund skipped for " + quitter.getName() + ".");
            plugin.getGameManager().removeCoinflipGame(game.getPlayerUUID());
            return;
        }

        final long amount = game.getAmount();
        final String amountFormatted = String.format(Locale.US, "%,d", amount);

        final String reason = "Coinflip bet refunded (" + amountFormatted + " - " + quitter.getName() + " quit with an open listing)";
        economyProvider.deposit(game.getOfflinePlayer(), amount, reason).exceptionally(ex -> {
            plugin.getLogger().warning("[DeluxeCoinflip] Failed to refund " + quitter.getName() + ": " + ex.getMessage());
            return null;
        });

        if (quitter.isOnline()) {
            Messages.GAME_REFUNDED.sendCurrency(
                  quitter, economyProvider.getIdentifier(),
                  "{AMOUNT}", amountFormatted,
                  "{CURRENCY}", game.getProvider()
            );
        }

        plugin.getGameManager().removeCoinflipGame(game.getPlayerUUID());
    }
}
