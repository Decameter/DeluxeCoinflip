/*
 * DeluxeCoinflip Plugin
 * Copyright (c) 2021 - 2025 Zithium Studios. All rights reserved.
 */

package net.zithium.deluxecoinflip.economy.provider.impl;

import me.mraxetv.beasttokens.api.BeastTokensAPI;
import me.mraxetv.beasttokens.api.handlers.BTTokensManager;
import net.zithium.deluxecoinflip.economy.provider.EconomyProvider;
import org.bukkit.OfflinePlayer;

import java.util.concurrent.CompletableFuture;

public class BeastTokensProvider extends EconomyProvider {

    private BTTokensManager tokensManager;

    public BeastTokensProvider() {
        super("BeastTokens");
    }

    @Override
    public void onEnable() {
        tokensManager = BeastTokensAPI.getTokensManager();
    }

    @Override
    public CompletableFuture<Double> getBalance(OfflinePlayer player) {
        double balance = player.isOnline() ? tokensManager.getTokens(player.getPlayer()) : tokensManager.getTokens(player);
        return CompletableFuture.completedFuture(balance);
    }

    @Override
    public CompletableFuture<Void> withdraw(OfflinePlayer player, double amount, String reason) {
        if (player.isOnline()) {
            tokensManager.removeTokens(player.getPlayer(), amount);
        } else {
            tokensManager.removeTokens(player, amount);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> deposit(OfflinePlayer player, double amount, String reason) {
        if (player.isOnline()) {
            tokensManager.addTokens(player.getPlayer(), amount);
        } else {
            tokensManager.addTokens(player, amount);
        }
        return CompletableFuture.completedFuture(null);
    }
}
