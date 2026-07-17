/*
 * DeluxeCoinflip Plugin
 * Copyright (c) 2021 - 2025 Zithium Studios. All rights reserved.
 */

package net.zithium.deluxecoinflip.economy.provider.impl;

import net.leonemc.neon.spigot.features.economy.EconomyResult;
import net.leonemc.neon.spigot.features.economy.EconomyService;
import net.leonemc.neon.spigot.features.economy.model.Currency;
import net.zithium.deluxecoinflip.economy.provider.EconomyProvider;
import org.bukkit.OfflinePlayer;

import java.util.concurrent.CompletableFuture;

public class NeonCurrencyProvider extends EconomyProvider {

    private final Currency currency;

    public NeonCurrencyProvider(Currency currency) {
        super("NEON_" + currency.getKey().toUpperCase());
        this.currency = currency;
        setCurrencyDisplayName(currency.getDisplayName());
    }

    @Override
    public void onEnable() {
        // Neon manages its own currency lifecycle; nothing to set up here.
    }

    @Override
    public CompletableFuture<Double> getBalance(OfflinePlayer player) {
        return EconomyService.INSTANCE.getBalance(player.getUniqueId(), currency);
    }

    @Override
    public CompletableFuture<Void> withdraw(OfflinePlayer player, double amount, String reason) {
        return EconomyService.INSTANCE.withdraw(player.getUniqueId(), currency, amount, reason)
                .thenApply(result -> null);
    }

    @Override
    public CompletableFuture<Void> deposit(OfflinePlayer player, double amount, String reason) {
        return EconomyService.INSTANCE.deposit(player.getUniqueId(), currency, amount, reason)
                .thenApply(result -> null);
    }

    @Override
    public CompletableFuture<Boolean> withdrawIfHas(OfflinePlayer player, double amount, String reason) {
        return EconomyService.INSTANCE.withdrawIfHas(player.getUniqueId(), currency, amount, reason)
                .thenApply(EconomyResult::isSuccess);
    }
}
