/*
 * DeluxeCoinflip Plugin
 * Copyright (c) 2021 - 2025 Zithium Studios. All rights reserved.
 */

package net.zithium.deluxecoinflip.economy.provider.impl;

import net.milkbowl.vault.economy.Economy;
import net.zithium.deluxecoinflip.DeluxeCoinflipPlugin;
import net.zithium.deluxecoinflip.economy.provider.EconomyProvider;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

public class VaultProvider extends EconomyProvider {

    private Economy economy;

    public VaultProvider() {
        super("Vault");
    }

    @Override
    public void onEnable() {
        RegisteredServiceProvider<Economy> rsp = JavaPlugin.getProvidingPlugin(DeluxeCoinflipPlugin.class).getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        }
    }

    @Override
    public CompletableFuture<Double> getBalance(OfflinePlayer player) {
        return CompletableFuture.completedFuture(economy.getBalance(player));
    }

    @Override
    public CompletableFuture<Void> withdraw(OfflinePlayer player, double amount, String reason) {
        economy.withdrawPlayer(player, amount);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> deposit(OfflinePlayer player, double amount, String reason) {
        economy.depositPlayer(player, amount);
        return CompletableFuture.completedFuture(null);
    }
}
