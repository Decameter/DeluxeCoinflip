package net.zithium.deluxecoinflip.menu.inventories;

import net.leonemc.neon.spigot.util.Tasks;
import net.leonemc.neon.spigot.util.menu.Button;
import net.leonemc.neon.spigot.util.menu.Menu;
import net.zithium.deluxecoinflip.DeluxeCoinflipPlugin;
import net.zithium.deluxecoinflip.api.events.CoinflipCompletedEvent;
import net.zithium.deluxecoinflip.config.ConfigType;
import net.zithium.deluxecoinflip.config.Messages;
import net.zithium.deluxecoinflip.economy.EconomyManager;
import net.zithium.deluxecoinflip.game.CoinflipGame;
import net.zithium.deluxecoinflip.storage.PlayerData;
import net.zithium.deluxecoinflip.storage.StorageManager;
import net.zithium.deluxecoinflip.utility.ItemStackBuilder;
import net.zithium.deluxecoinflip.utility.TextUtil;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

public class CoinflipGUI implements Listener {

    private final DeluxeCoinflipPlugin plugin;
    private static final Random RANDOM = new Random();
    private final EconomyManager economyManager;
    private final FileConfiguration config;
    private final String coinflipGuiTitle;
    private final boolean taxEnabled;
    private final double taxRate;
    private final long minimumBroadcastWinnings;
    private static final int ANIMATION_COUNT_THRESHOLD = 12;

    public CoinflipGUI(@NotNull DeluxeCoinflipPlugin plugin) {
        this.plugin = plugin;
        this.economyManager = plugin.getEconomyManager();
        this.config = plugin.getConfigHandler(ConfigType.CONFIG).getConfig();

        // Load config values into variables this helps improve performance.
        this.coinflipGuiTitle = TextUtil.color(config.getString("coinflip-gui.title"));
        this.taxEnabled = config.getBoolean("settings.tax.enabled");
        this.taxRate = config.getDouble("settings.tax.rate");
        this.minimumBroadcastWinnings = config.getLong("settings.minimum-broadcast-winnings");
    }

    public void startGame(@NotNull Player creator, @NotNull OfflinePlayer opponent, CoinflipGame game) {
        // Send the challenge message BEFORE any swapping
        Messages.PLAYER_CHALLENGE.send(opponent.getPlayer(), "{OPPONENT}", creator.getName());

        // Randomly shuffle player order to avoid bias
        if (RANDOM.nextBoolean()) {
            Player temp = creator;
            creator = (Player) opponent;
            opponent = temp;
        }

        // Determine the winner and loser
        OfflinePlayer winner = RANDOM.nextBoolean() ? creator : opponent;
        OfflinePlayer loser = (winner == creator) ? opponent : creator;

        // Proceed with the game animation and results
        runAnimation(creator, winner, loser, game);
    }

    private void runAnimation(Player player, OfflinePlayer winner, OfflinePlayer loser, CoinflipGame game) {
        CoinflipMenu gui = new CoinflipMenu();

        Button winnerHead = Button.create(
                new ItemStackBuilder(
                        winner.equals(game.getOfflinePlayer()) ? game.getCachedHead() : new ItemStack(Material.PLAYER_HEAD)
                ).withName(ChatColor.YELLOW + winner.getName()).setSkullOwner(winner).build()
        );

        Button loserHead = Button.create(
                new ItemStackBuilder(
                        winner.equals(game.getOfflinePlayer()) ? new ItemStack(Material.PLAYER_HEAD) : game.getCachedHead()
                ).withName(ChatColor.YELLOW + loser.getName()).setSkullOwner(loser).build()
        );

        if (winner.isOnline()) {
            gui.open(winner.getPlayer());
        }
        if (loser.isOnline()) {
            gui.open(loser.getPlayer());
        }

        new BukkitRunnable() {
            boolean alternate = false;
            int count = 0;
            long winAmount = game.getAmount() * 2;
            long beforeTax = winAmount / 2;

            @Override
            public void run() {
                try {
                    this.handle();
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to run coinflip runnable!", e);
                    cancel();
                }
            }

            public void handle() {
                count++;

                if (count >= ANIMATION_COUNT_THRESHOLD) {
                    // Completed animation
                    gui.fill(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
                    gui.setItem(13, winnerHead);
                    gui.update();

                    if (player.isOnline()) {
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1L, 0L);
                    }

                    long taxed = 0;

                    if (taxEnabled) {
                        taxed = (long) ((taxRate * winAmount) / 100.0);
                        winAmount -= taxed;
                    }

                    // Run event.
                    Bukkit.getPluginManager().callEvent(new CoinflipCompletedEvent(winner, loser, winAmount));

                    // Update player stats
                    Tasks.runAsync(() -> {
                        StorageManager storageManager = plugin.getStorageManager();
                        updatePlayerStats(storageManager, winner, winAmount, beforeTax, true);
                        updatePlayerStats(storageManager, loser, 0, beforeTax, false);

                        // Deposit winnings asynchronously
                        economyManager.getEconomyProvider(game.getProvider()).deposit(winner, winAmount);
                    });

                    String winAmountFormatted = TextUtil.numberFormat(winAmount);
                    String taxedFormatted = TextUtil.numberFormat(taxed);

                    // Send win/loss messages
                    if (winner.isOnline()) {
                        Messages.GAME_SUMMARY_WIN.send(winner.getPlayer(), replacePlaceholders(String.valueOf(taxRate), taxedFormatted, winner.getName(), loser.getName(), economyManager.getEconomyProvider(game.getProvider()).getDisplayName(), winAmountFormatted));
                    }
                    if (loser.isOnline()) {
                        Messages.GAME_SUMMARY_LOSS.send(loser.getPlayer(), replacePlaceholders(String.valueOf(taxRate), taxedFormatted, winner.getName(), loser.getName(), economyManager.getEconomyProvider(game.getProvider()).getDisplayName(), winAmountFormatted));
                    }
                    // Broadcast to the server
                    broadcastWinningMessage(winAmount, taxed, winner.getName(), loser.getName(), economyManager.getEconomyProvider(game.getProvider()).getDisplayName());

                    //closeAnimationGUI(gui);
                    gui.finish();
                    cancel();
                    return;
                }

                // Do animation
                if (alternate) {
                    gui.fill(Material.YELLOW_STAINED_GLASS_PANE);
                    gui.setItem(13, winnerHead);
                } else {
                    gui.fill(Material.GRAY_STAINED_GLASS_PANE);
                    gui.setItem(13, loserHead);
                }

                alternate = !alternate;

                if (player.isOnline()) {
                    player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1L, 0L);
                }

                gui.update();
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private void updatePlayerStats(StorageManager storageManager, OfflinePlayer player, long winAmount, long beforeTax, boolean isWinner) {
        Optional<PlayerData> playerDataOptional = storageManager.getPlayer(player.getUniqueId());
        if (playerDataOptional.isPresent()) {
            PlayerData playerData = playerDataOptional.get();
            if (isWinner) {
                playerData.updateWins();
                playerData.updateProfit(winAmount);
                playerData.updateGambled(beforeTax);
            } else {
                playerData.updateLosses();
                playerData.updateLosses(beforeTax);
                playerData.updateGambled(beforeTax);
            }
        } else {
            if (isWinner) {
                storageManager.updateOfflinePlayerWin(player.getUniqueId(), winAmount, beforeTax);
            } else {
                storageManager.updateOfflinePlayerLoss(player.getUniqueId(), beforeTax);
            }
        }
    }

    private void broadcastWinningMessage(long winAmount, long tax, String winner, String loser, String currency) {
        if (winAmount >= minimumBroadcastWinnings) {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                plugin.getStorageManager().getPlayer(player.getUniqueId()).ifPresent(playerData -> {
                    if (playerData.isDisplayBroadcastMessages()) {
                        Messages.COINFLIP_BROADCAST.send(player, replacePlaceholders(
                                String.valueOf(taxRate),
                                TextUtil.numberFormat(tax),
                                winner,
                                loser,
                                currency,
                                TextUtil.numberFormat(winAmount)
                        ));
                    }
                });
            }
        }
    }

    private Object[] replacePlaceholders(String taxRate, String taxDeduction, String winner, String loser, String currency, String winnings) {
        return new Object[]{"{TAX_RATE}", taxRate,
                "{TAX_DEDUCTION}", taxDeduction,
                "{WINNER}", winner,
                "{LOSER}", loser,
                "{CURRENCY}", currency,
                "{WINNINGS}", winnings};
    }

    private static class CoinflipMenu extends Menu {

        private static final int SIZE = 27;
        private Set<OfflinePlayer> viewers = new HashSet<>();
        private Map<Integer, Button> buttons = new HashMap<>();

        public CoinflipMenu() {
            super("Coinflip");
        }

        @Override
        public Map<Integer, Button> getButtons(Player player) {
            return buttons;
        }

        @Override
        public int size(Player player) {
            return SIZE;
        }

        public void fill(Material material) {
            fill(Button.create(new ItemStack(material)));
        }

        public void fill(Button button) {
            for (int i = 0; i < SIZE; i++) {
                buttons.put(i, button);
            }
        }

        public void setItem(int slot, Button button) {
            buttons.put(slot, button);
        }

        public void open(Player player) {
            openMenu(player);
            viewers.add(player);
        }

        public void update() {
            for (OfflinePlayer player : viewers) {
                if (player.isOnline()) {
                    instantUpdate(player.getPlayer());
                }
            }
        }

        public void finish() {
            viewers.clear();
        }

    }
}