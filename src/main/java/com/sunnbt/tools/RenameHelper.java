package com.sunnbt.tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.Session;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 生成合法离线用户名、算离线 UUID、替换客户端 Session、断线重连。
 * 名字前缀与长度限制从配置读取。
 */
@SideOnly(Side.CLIENT)
public final class RenameHelper {

    /** Minecraft 用户名允许的字符集 */
    private static final char[] ALLOWED = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    private RenameHelper() {
    }

    /* ------------------------------ 名字生成 ------------------------------ */

    public static String generateName(String previousName) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String[] prefixes = Config.prefixes();
        int min = Config.minNameLength();
        int max = Config.maxNameLength();

        for (int attempt = 0; attempt < 400; attempt++) {
            String prefix = prefixes[random.nextInt(prefixes.length)];
            String candidate = prefix + "_" + randomToken(random, 5);

            if (candidate.length() > max) {
                candidate = candidate.substring(0, max);
            }
            while (candidate.length() < min) {
                candidate = candidate + ALLOWED[random.nextInt(ALLOWED.length)];
            }

            if (!isValidName(candidate)) {
                continue;
            }
            if (previousName != null && candidate.equalsIgnoreCase(previousName)) {
                continue;
            }
            return candidate;
        }
        return "Player" + (100000 + random.nextInt(900000));
    }

    public static boolean isValidName(String name) {
        if (name == null || name.length() < Config.minNameLength() || name.length() > Config.maxNameLength()) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            boolean ok = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_';
            if (!ok) {
                return false;
            }
        }
        return true;
    }

    private static String randomToken(ThreadLocalRandom random, int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(ALLOWED[random.nextInt(ALLOWED.length)]);
        }
        return builder.toString();
    }

    /* -------------------------------- UUID -------------------------------- */

    public static UUID offlineUUID(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }

    public static String currentName() {
        try {
            Session session = Minecraft.getMinecraft().getSession();
            if (session != null && session.getUsername() != null) {
                return session.getUsername();
            }
        } catch (Throwable ignored) {
        }
        return "unknown";
    }

    /* ---------------------------- Session 替换 ---------------------------- */

    public static boolean applySession(String name, UUID uuid) {
        Minecraft mc = Minecraft.getMinecraft();
        Session oldSession = mc.getSession();
        if (oldSession == null) {
            return false;
        }

        String id32 = uuid.toString().replace("-", "");
        String token = oldSession.getToken();
        String oldName = oldSession.getUsername();
        String oldId = oldSession.getPlayerID();

        Session newSession = buildSession(name, id32, token, oldSession);
        boolean replaced = false;

        if (newSession != null) {
            for (Field field : Minecraft.class.getDeclaredFields()) {
                if (field.getType() != Session.class) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    field.set(mc, newSession);
                    replaced = true;
                } catch (Throwable ignored) {
                    // final 字段可能不允许整体替换，走下面的兜底
                }
            }
        }

        boolean patched = patchSessionFields(oldSession, name, id32, oldName, oldId);

        if (replaced || patched) {
            SunnBTMods.LOGGER.info("[SunnBT] 已切换离线身份: {} (uuid={})", name, uuid);
            return true;
        }
        return false;
    }

    private static Session buildSession(String name, String id32, String token, Session reference) {
        for (Constructor<?> constructor : Session.class.getConstructors()) {
            Class<?>[] types = constructor.getParameterTypes();
            if (types.length != 4) {
                continue;
            }
            if (types[0] != String.class || types[1] != String.class || types[2] != String.class) {
                continue;
            }

            Object typeArgument;
            if (types[3] == String.class) {
                typeArgument = "legacy";
            } else {
                Object existing = reference.getSessionType();
                if (existing == null || !types[3].isInstance(existing)) {
                    Object[] constants = types[3].getEnumConstants();
                    existing = (constants != null && constants.length > 0) ? constants[0] : null;
                }
                typeArgument = existing;
            }

            try {
                return (Session) constructor.newInstance(name, id32, token, typeArgument);
            } catch (Throwable ignored) {
                // 下一个构造函数
            }
        }
        return null;
    }

    private static boolean patchSessionFields(Session session, String name, String id32, String oldName, String oldId) {
        boolean nameDone = false;
        for (Field field : Session.class.getDeclaredFields()) {
            if (field.getType() != String.class) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object value = field.get(session);
                if (oldName != null && oldName.equals(value)) {
                    field.set(session, name);
                    nameDone = true;
                } else if (oldId != null && oldId.equals(value)) {
                    field.set(session, id32);
                }
            } catch (Throwable ignored) {
            }
        }
        return nameDone;
    }

    /* --------------------------- 断开界面 / 重连 --------------------------- */

    public static boolean isServerDisconnect(GuiDisconnected screen) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getCurrentServerData() != null) {
            return true;
        }
        GuiScreen parent = parentScreen(screen);
        return parent instanceof GuiMultiplayer;
    }

    public static GuiScreen parentScreen(GuiDisconnected screen) {
        for (Field field : GuiDisconnected.class.getDeclaredFields()) {
            if (field.getType() != GuiScreen.class) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object value = field.get(screen);
                if (value instanceof GuiScreen) {
                    return (GuiScreen) value;
                }
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    public static void reconnect(GuiScreen parent, ServerData server) {
        Minecraft mc = Minecraft.getMinecraft();

        if (!(mc.currentScreen instanceof GuiDisconnected)) {
            return;
        }

        try {
            if (server == null) {
                mc.displayGuiScreen(new GuiMultiplayer(new GuiMainMenu()));
                return;
            }
            GuiScreen targetParent = (parent != null) ? parent : new GuiMultiplayer(new GuiMainMenu());
            mc.displayGuiScreen(new GuiConnecting(targetParent, mc, server));
        } catch (Throwable throwable) {
            SunnBTMods.LOGGER.error("[SunnBT] 重连失败，返回服务器列表", throwable);
            mc.displayGuiScreen(new GuiMultiplayer(new GuiMainMenu()));
        }
    }
}
