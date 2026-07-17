/*
 * DeluxeCoinflip Plugin
 * Copyright (c) 2021 - 2025 Zithium Studios. All rights reserved.
 */

package net.zithium.deluxecoinflip.economy.provider.impl;

import net.zithium.deluxecoinflip.economy.provider.EconomyProvider;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.OfflinePlayer;

import java.util.concurrent.CompletableFuture;

public class PlayerPointsProvider extends EconomyProvider {

    private PlayerPointsAPI api;

    public PlayerPointsProvider() {
        super("PlayerPoints");
    }

    @Override
    public void onEnable() {
        api = PlayerPoints.getInstance().getAPI();
    }

    @Override
    public CompletableFuture<Double> getBalance(OfflinePlayer player) {
        return CompletableFuture.completedFuture((double) api.look(player.getUniqueId()));
    }

    @Override
    public CompletableFuture<Void> withdraw(OfflinePlayer player, double amount, String reason) {
        api.take(player.getUniqueId(), (int) amount);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> deposit(OfflinePlayer player, double amount, String reason) {
        api.give(player.getUniqueId(), (int) amount);
        return CompletableFuture.completedFuture(null);
    }
}