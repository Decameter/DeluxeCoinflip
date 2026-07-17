/*
 * DeluxeCoinflip Plugin
 * Copyright (c) 2021 - 2025 Zithium Studios. All rights reserved.
 */

package net.zithium.deluxecoinflip.economy.provider;

import org.bukkit.OfflinePlayer;

import java.util.concurrent.CompletableFuture;

public abstract class EconomyProvider {

    private final String identifier;
    private String name;

    public EconomyProvider(String identifier) {
        this.identifier = identifier;
        this.name = identifier;
    }

    public abstract void onEnable();

    public abstract CompletableFuture<Double> getBalance(OfflinePlayer player);

    public abstract CompletableFuture<Void> withdraw(OfflinePlayer player, double amount, String reason);

    public abstract CompletableFuture<Void> deposit(OfflinePlayer player, double amount, String reason);

    public CompletableFuture<Boolean> withdrawIfHas(OfflinePlayer player, double amount, String reason) {
        return getBalance(player).thenCompose(balance -> {
            if (balance < amount) {
                return CompletableFuture.completedFuture(false);
            }

            return withdraw(player, amount, reason).thenApply(ignored -> true);
        });
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setCurrencyDisplayName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return name;
    }
}
