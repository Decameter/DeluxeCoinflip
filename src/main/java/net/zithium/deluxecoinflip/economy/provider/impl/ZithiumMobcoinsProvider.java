/*
 * DeluxeCoinflip Plugin
 * Copyright (c) 2021 - 2025 Zithium Studios. All rights reserved.
 */

package net.zithium.deluxecoinflip.economy.provider.impl;

import net.zithium.deluxecoinflip.economy.provider.EconomyProvider;
import net.zithium.mobcoins.ZithiumMobCoinsAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ZithiumMobcoinsProvider extends EconomyProvider {

    private ZithiumMobCoinsAPI api;

    public ZithiumMobcoinsProvider() {
        super("ZithiumMobcoins");
    }

    @Override
    public void onEnable() {
        api = (ZithiumMobCoinsAPI) Bukkit.getPluginManager().getPlugin("ZithiumMobcoins");
    }

    @Override
    public CompletableFuture<Double> getBalance(OfflinePlayer player) {
        Optional<Long> balanceOptional = api.getUserBalance(player.getUniqueId());
        return CompletableFuture.completedFuture(balanceOptional.orElse(0L).doubleValue());
    }

    @Override
    public CompletableFuture<Void> withdraw(OfflinePlayer player, double amount, String reason) {
        api.subtractCoins(player.getUniqueId(), (int) amount);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> deposit(OfflinePlayer player, double amount, String reason) {
        api.addCoins(player.getUniqueId(), (int) amount);
        return CompletableFuture.completedFuture(null);
    }
}
