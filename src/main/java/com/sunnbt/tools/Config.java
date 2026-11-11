package com.sunnbt.tools;

import java.io.File;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

/**
 * 全部可编辑项都在这里：改的值会写进 config/sunnbtmods.cfg，
 * 也能在游戏的 Mods 列表 -> Config 界面里直接改。
 */
public final class Config {

    public static final String CAT_LOGIN = "autologin";
    public static final String CAT_RENAME = "autorename";

    private static Configuration config;

    /* ---------------- 自动登录 ---------------- */
    private static Property loginEnabled;
    private static Property loginPassword;
    private static Property registerCommand;
    private static Property loginCommand;
    private static Property detectWindowSeconds;
    private static Property sendLoginAfterRegister;
    private static Property maxLoginAttempts;
    private static Property detectRegisterPrompt;
    private static Property detectLoginPrompt;
    private static Property chatLog;

    /* --------------- 离线账号改名 --------------- */
    private static Property renameEnabled;
    private static Property showAccountsButton;
    private static Property showRenameOnDisconnect;
    private static Property reconnectDelayTicks;
    private static Property minNameLength;
    private static Property maxNameLength;
    private static Property namePrefixes;

    private Config() {
    }

    public static void init(File file) {
        if (config != null) {
            return;
        }
        config = new Configuration(file);
        config.load();

        // ---------------- 自动登录 ----------------
        loginEnabled = config.get(CAT_LOGIN, "enableAutoLogin", true,
                "Turn the automatic /register and /login on or off.");
        loginPassword = config.get(CAT_LOGIN, "password", "sunnbt111",
                "Password used by auto register/login. %password% in the commands is replaced by this.");
        registerCommand = config.get(CAT_LOGIN, "registerCommand", "/register %password% %password%",
                "Command sent when a register prompt is detected. %password% = password.");
        loginCommand = config.get(CAT_LOGIN, "loginCommand", "/login %password%",
                "Command sent when a login prompt is detected. %password% = password.");
        detectWindowSeconds = config.get(CAT_LOGIN, "detectWindowSeconds", 40,
                "How many seconds after joining the server the chat is watched.");
        sendLoginAfterRegister = config.get(CAT_LOGIN, "sendLoginAfterRegister", false,
                "If false, a /login is never sent once /register has been sent.");
        maxLoginAttempts = config.get(CAT_LOGIN, "maxLoginAttempts", 3,
                "How many times /login may be sent inside the time window.");
        detectRegisterPrompt = config.get(CAT_LOGIN, "detectRegisterPrompt", true,
                "Watch chat for register prompts (/register, /reg, please register ...).");
        detectLoginPrompt = config.get(CAT_LOGIN, "detectLoginPrompt", true,
                "Watch chat for login prompts (/login, please login ...).");
        chatLog = config.get(CAT_LOGIN, "showChatMessages", true,
                "Print a local chat line whenever the mod sends a command.");

        // --------------- 离线账号改名 ---------------
        renameEnabled = config.get(CAT_RENAME, "enableAutoRename", true,
                "Master switch for the offline-account / rename features.");
        showAccountsButton = config.get(CAT_RENAME, "showAccountsButtonInMultiplayer", true,
                "Show the 'Offline Accounts' button on the multiplayer server list.");
        showRenameOnDisconnect = config.get(CAT_RENAME, "showRenameButtonOnDisconnect", true,
                "Show the 'Rename & Rejoin' button on the disconnect screen.");
        reconnectDelayTicks = config.get(CAT_RENAME, "reconnectDelayTicks", 15,
                "Ticks to wait before reconnecting after a rename (20 ticks = 1 second).");
        minNameLength = config.get(CAT_RENAME, "minNameLength", 3,
                "Minimum length of offline user names (3 is the lowest allowed by Minecraft).");
        maxNameLength = config.get(CAT_RENAME, "maxNameLength", 16,
                "Maximum length of offline user names (16 is the highest allowed by Minecraft).");
        namePrefixes = config.get(CAT_RENAME, "namePrefixes", new String[]{
                "Steve", "Alex", "Craft", "Pixel", "Nova", "Echo", "Byte", "Frost",
                "Blaze", "Shadow", "Ghost", "Turbo", "Neo", "Zero", "Diamond", "Ender",
                "Sky", "Iron", "Storm", "Lucky"
        }, "Prefixes used by the random name generator, one entry per line.");

        Property[] mcRestartList = {
                loginEnabled, loginPassword, registerCommand, loginCommand, detectWindowSeconds,
                sendLoginAfterRegister, maxLoginAttempts, detectRegisterPrompt, detectLoginPrompt, chatLog,
                renameEnabled, showAccountsButton, showRenameOnDisconnect, reconnectDelayTicks,
                minNameLength, maxNameLength, namePrefixes
        };
        for (Property property : mcRestartList) {
            if (property != null) {
                property.setRequiresMcRestart(false);
                property.setRequiresWorldRestart(false);
            }
        }

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void save() {
        if (config != null) {
            config.save();
        }
    }

    public static ConfigCategory loginCategory() {
        return config == null ? null : config.getCategory(CAT_LOGIN);
    }

    public static ConfigCategory renameCategory() {
        return config == null ? null : config.getCategory(CAT_RENAME);
    }

    /* ---------------------------- 自动登录取值 ---------------------------- */

    public static boolean loginEnabled() {
        return loginEnabled == null || loginEnabled.getBoolean();
    }

    public static String password() {
        return loginPassword == null ? "sunnbt111" : loginPassword.getString();
    }

    public static String registerCommand() {
        return build(registerCommand == null ? "/register %password% %password%" : registerCommand.getString());
    }

    public static String loginCommand() {
        return build(loginCommand == null ? "/login %password%" : loginCommand.getString());
    }

    private static String build(String template) {
        return template.replace("%password%", password()).trim();
    }

    public static long detectWindowMs() {
        int seconds = detectWindowSeconds == null ? 40 : detectWindowSeconds.getInt();
        if (seconds < 1) {
            seconds = 1;
        }
        return seconds * 1000L;
    }

    public static boolean sendLoginAfterRegister() {
        return sendLoginAfterRegister != null && sendLoginAfterRegister.getBoolean();
    }

    public static int maxLoginAttempts() {
        int value = maxLoginAttempts == null ? 3 : maxLoginAttempts.getInt();
        return value < 1 ? 1 : value;
    }

    public static boolean detectRegisterPrompt() {
        return detectRegisterPrompt == null || detectRegisterPrompt.getBoolean();
    }

    public static boolean detectLoginPrompt() {
        return detectLoginPrompt == null || detectLoginPrompt.getBoolean();
    }

    public static boolean chatLogEnabled() {
        return chatLog == null || chatLog.getBoolean();
    }

    /* ---------------------------- 离线改名取值 ---------------------------- */

    public static boolean renameEnabled() {
        return renameEnabled == null || renameEnabled.getBoolean();
    }

    public static boolean showAccountsButton() {
        return showAccountsButton == null || showAccountsButton.getBoolean();
    }

    public static boolean showRenameOnDisconnect() {
        return showRenameOnDisconnect == null || showRenameOnDisconnect.getBoolean();
    }

    public static int reconnectDelayTicks() {
        int value = reconnectDelayTicks == null ? 15 : reconnectDelayTicks.getInt();
        return value < 1 ? 1 : value;
    }

    public static int minNameLength() {
        int value = minNameLength == null ? 3 : minNameLength.getInt();
        if (value < 3) {
            value = 3;
        }
        if (value > 16) {
            value = 16;
        }
        return value;
    }

    public static int maxNameLength() {
        int value = maxNameLength == null ? 16 : maxNameLength.getInt();
        if (value < 3) {
            value = 3;
        }
        if (value > 16) {
            value = 16;
        }
        if (value < minNameLength()) {
            value = minNameLength();
        }
        return value;
    }

    public static String[] prefixes() {
        String[] values = namePrefixes == null ? null : namePrefixes.getStringList();
        if (values == null || values.length == 0) {
            return new String[]{"Steve", "Alex", "Craft", "Pixel", "Nova"};
        }
        return values;
    }
}
