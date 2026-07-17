/*
 * DeluxeCoinflip Plugin
 * Copyright (c) 2021 - 2025 Zithium Studios. All rights reserved.
 */

package net.zithium.deluxecoinflip.economy.provider.impl;

import com.vk2gpz.tokenenchant.api.ITokenEnchant;
import net.zithium.deluxecoinflip.economy.provider.EconomyProvider;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.concurrent.CompletableFuture;

public class TokenEnchantProvider extends EconomyProvider {

    private ITokenEnchant tokenEnchantAPI;

    public TokenEnchantProvider() {
        super("TokenEnchant");
    }

    @Override
    public void onEnable() {
        tokenEnchantAPI = (ITokenEnchant) Bukkit.getServer().getPluginManager().getPlugin("TokenEnchant");
    }

    @Override
    public CompletableFuture<Double> getBalance(OfflinePlayer player) {
        return CompletableFuture.completedFuture(tokenEnchantAPI.getTokens(player));
    }

    @Override
    public CompletableFuture<Void> withdraw(OfflinePlayer player, double amount, String reason) {
        tokenEnchantAPI.removeTokens(player, amount);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> deposit(OfflinePlayer player, double amount, String reason) {
        tokenEnchantAPI.addTokens(player, amount);
        return CompletableFuture.completedFuture(null);
    }
}
