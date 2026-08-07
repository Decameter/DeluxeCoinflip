/*
 * DeluxeCoinflip Plugin
 * Copyright (c) 2021 - 2025 Zithium Studios. All rights reserved.
 */

package net.zithium.deluxecoinflip.game;

import com.tcoded.folialib.impl.PlatformScheduler;
import net.zithium.deluxecoinflip.DeluxeCoinflipPlugin;
import net.zithium.deluxecoinflip.storage.StorageManager;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager {

    private final DeluxeCoinflipPlugin plugin;
    private final PlatformScheduler scheduler;
    private final Map<UUID, CoinflipGame> coinflipGames;
    private final StorageManager storageManager;

    public GameManager(DeluxeCoinflipPlugin plugin) {
        this.plugin = plugin;
        this.scheduler = DeluxeCoinflipPlugin.scheduler();
        this.coinflipGames = new ConcurrentHashMap<>();
        this.storageManager = plugin.getStorageManager();
    }

    /**
     * Add a coinflip game
     *
     * @param uuid The UUID of the player creating the game
     * @param game The coinflip game object
     */
    public void addCoinflipGame(UUID uuid, CoinflipGame game) {
        coinflipGames.put(uuid, game);
        scheduler.runAsync(task -> storageManager.getStorageHandler().saveCoinflip(game));
    }

    /**
     * Delete an existing coinflip game
     *
     * <p>Scheduling on Folia when the plugin is disabling does not
     * work and shoots an exception. Please refrain from modifying
     * this logic unless you know what you're doing.</p>
     *
     * @param uuid The UUID of the player removing the game
     */
    public void removeCoinflipGame(@NotNull UUID uuid) {
        coinflipGames.remove(uuid);

        if (!plugin.isEnabled()) {
            try {
                storageManager.getStorageHandler().deleteCoinflip(uuid);
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to delete coinflip for " + uuid + " during shutdown: " + ex.getMessage());
            }

            return;
        }

        scheduler.runAsync(task -> {
            try {
                storageManager.getStorageHandler().deleteCoinflip(uuid);
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to delete coinflip for " + uuid + ": " + ex.getMessage());
            }
        });
    }

    /**
     * Get all coinflip games
     *
     * @return Map of UUID and CoinflipGame object
     */
    public Map<UUID, CoinflipGame> getCoinflipGames() {
        return coinflipGames;
    }

    /**
     * Atomically claim a coinflip game for acceptance, removing it from the map
     * so that concurrent accept attempts (e.g. rapid double-clicks) cannot both
     * succeed. Does not touch persistent storage; callers are responsible for
     * calling {@link #removeCoinflipGame(UUID)} once the accept is confirmed,
     * or {@link #restoreCoinflipGame(UUID, CoinflipGame)} if it is aborted.
     *
     * @param uuid The UUID of the player who created the game
     * @return The claimed game, or {@code null} if it was already claimed/removed
     */
    public CoinflipGame claimCoinflipGame(@NotNull UUID uuid) {
        return coinflipGames.remove(uuid);
    }

    /**
     * Restore a previously claimed coinflip game back into the map, e.g. when
     * an accept attempt is aborted (invalid currency, insufficient funds).
     *
     * @param uuid The UUID of the player who created the game
     * @param game The game to restore
     */
    public void restoreCoinflipGame(@NotNull UUID uuid, @NotNull CoinflipGame game) {
        coinflipGames.putIfAbsent(uuid, game);
    }

    public CoinflipGame getCoinflipGame(@NotNull UUID playerUUID) {
        return coinflipGames.get(playerUUID);
    }
}
