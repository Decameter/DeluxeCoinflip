/*
 * DeluxeCoinflip Plugin
 * Copyright (c) 2021 - 2025 Zithium Studios. All rights reserved.
 */

package net.zithium.deluxecoinflip.economy.provider.impl;

import me.realized.tokenmanager.api.TokenManager;
import net.zithium.deluxecoinflip.economy.provider.EconomyProvider;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;

public class TokenManagerProvider extends EconomyProvider {

    private TokenManager tokenManager;

    public TokenManagerProvider() {
        super("TokenManager");
    }

    @Override
    public void onEnable() {
        tokenManager = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
    }

    @Override
    public CompletableFuture<Double> getBalance(OfflinePlayer player) {
        OptionalLong tokens = tokenManager.getTokens(player.getPlayer());
        return CompletableFuture.completedFuture(tokens.isEmpty() ? 0.0 : (double) tokens.getAsLong());
    }

    @Override
    public CompletableFuture<Void> withdraw(OfflinePlayer player, double amount, String reason) {
        tokenManager.removeTokens(player.getPlayer(), (long) amount);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> deposit(OfflinePlayer player, double amount, String reason) {
        tokenManager.addTokens(player.getPlayer(), (long) amount);
        return CompletableFuture.completedFuture(null);
    }
}
