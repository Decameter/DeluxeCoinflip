/*
 * DeluxeCoinflip Plugin
 * Copyright (c) 2021 - 2025 Zithium Studios. All rights reserved.
 */

package net.zithium.deluxecoinflip.config;

import net.zithium.deluxecoinflip.utility.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Locale;

public enum Messages {

    PREFIX("general.prefix"),
    RELOAD("general.reload"),
    NO_PERMISSION("general.no-permission"),
    HELP_DEFAULT("general.help_default"),
    HELP_ADMIN("general.help_admin"),

    BROADCASTS_TOGGLED_ON("coinflip.toggle_broadcasts_on"),
    BROADCASTS_TOGGLED_OFF("coinflip.toggle_broadcasts_off"),
    GAME_NOT_FOUND("coinflip.game_not_found"),
    CREATED_GAME("coinflip.created_coinflip"),
    DELETED_GAME("coinflip.deleted_coinflip"),
    INSUFFICIENT_FUNDS("coinflip.insufficient-funds"),
    CREATE_MINIMUM_AMOUNT("coinflip.minimum-amount"),
    CREATE_MAXIMUM_AMOUNT("coinflip.maximum-amount"),
    GAME_ACTIVE("coinflip.coinflip-active"),
    PLAYER_CHALLENGE("coinflip.player-challenged-you"),
    COINFLIP_BROADCAST("coinflip.broadcast-coinflip"),
    COINFLIP_CREATED_BROADCAST("coinflip.broadcast-created-coinflip"),

    ERROR_GAME_UNAVAILABLE("coinflip.game-unavailable"),
    ERROR_COINFLIP_SELF("coinflip.cant-coinflip-self"),
    CHAT_CANCELLED("coinflip.chat-cancelled"),
    INVALID_CURRENCY("coinflip.invalid-currency"),
    INVALID_AMOUNT("coinflip.invalid-amount"),

    GAME_FORFEIT("coinflip.summary-forfeit"),
    GAME_REFUNDED("coinflip.refunded"),
    GAME_SUMMARY_LOSS("coinflip.summary-loss"),
    GAME_SUMMARY_WIN("coinflip.summary-win");

    private static FileConfiguration config;

    private final String path;

    Messages(String path) {
        this.path = path;
    }

    public static void setConfiguration(FileConfiguration c) {
        config = c;
    }

    public void broadcast(Object... replacements) {
        broadcastCurrency(null, replacements);
    }

    /**
     * Broadcasts the message to all online players, using the currency-specific
     * override for {@code currencyIdentifier} if one is configured (see {@link #sendCurrency}).
     */
    public void broadcastCurrency(String currencyIdentifier, Object... replacements) {
        if (config == null) {
            return;
        }

        Bukkit.getOnlinePlayers().forEach(player -> sendCurrency(player, currencyIdentifier, replacements));
    }

    public void send(CommandSender receiver, Object... replacements) {
        sendCurrency(receiver, null, replacements);
    }

    /**
     * Sends the message to the receiver, preferring a currency-specific override if one exists.
     * <p>
     * A message at path {@code x.y} can be overridden for a currency by adding a sibling
     * section {@code x.y_overrides.<CURRENCY_IDENTIFIER>} in messages.yml, where
     * {@code CURRENCY_IDENTIFIER} matches the economy provider's identifier (e.g. {@code VAULT},
     * {@code NEON_GEMS}, {@code CUSTOM_CURRENCY}).
     *
     * @param currencyIdentifier the economy provider identifier, or null/empty for no override lookup
     */
    public void sendCurrency(CommandSender receiver, String currencyIdentifier, Object... replacements) {
        if (config == null || receiver == null) {
            return;
        }

        String resolvedPath = resolvePath(currencyIdentifier);

        Object value = config.get(resolvedPath);

        String message;
        if (value == null) {
            message = "DeluxeCoinflip: message not found (" + resolvedPath + ")";
        } else if (value instanceof List) {
            List<String> lines = config.getStringList(resolvedPath);
            message = TextUtil.fromList(lines);
        } else {
            message = String.valueOf(value);
        }

        if (message == null || message.isEmpty()) {
            return;
        }

        String colored = TextUtil.color(replace(message, currencyIdentifier, replacements));
        if (colored == null || colored.isEmpty()) {
            return;
        }

        receiver.sendMessage(colored);
    }

    /**
     * Resolves the effective config path for this message, given an optional currency
     * identifier. Returns the currency override path if it exists in the config, otherwise
     * falls back to the default path.
     */
    private String resolvePath(String currencyIdentifier) {
        if (config == null || currencyIdentifier == null || currencyIdentifier.isEmpty()) {
            return this.path;
        }

        int lastDot = this.path.lastIndexOf('.');
        String parent = lastDot >= 0 ? this.path.substring(0, lastDot + 1) : "";
        String key = lastDot >= 0 ? this.path.substring(lastDot + 1) : this.path;

        String overridePath = parent + key + "_overrides." + currencyIdentifier.toUpperCase(Locale.ROOT);
        return config.contains(overridePath) ? overridePath : this.path;
    }

    private String replace(String message, String currencyIdentifier, Object... replacements) {
        if (message == null) {
            return "";
        }

        if (replacements != null) {
            for (int i = 0; i + 1 < replacements.length; i += 2) {
                String key = String.valueOf(replacements[i]);
                String val = String.valueOf(replacements[i + 1]);
                if (key != null && !key.isEmpty()) {
                    message = message.replace(key, val != null ? val : "");
                }
            }
        }

        if (config != null) {
            String prefix = config.getString(PREFIX.resolvePath(currencyIdentifier));
            message = message.replace("{PREFIX}", (prefix != null && !prefix.isEmpty()) ? prefix : "");
        }

        return message;
    }

    public String getPath() {
        return this.path;
    }
}
