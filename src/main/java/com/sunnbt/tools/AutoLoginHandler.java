package com.sunnbt.tools;

import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 进服后在设定的时间窗内盯聊天栏：
 *   出现注册提示 -> 发送 registerCommand（默认 /register 密码 密码）
 *   出现登录提示 -> 发送 loginCommand（默认 /login 密码）
 * 已经发过 /register 时，默认不再发 /login（可在配置里改）。
 */
@SideOnly(Side.CLIENT)
public class AutoLoginHandler {

    private static final long GRACE_MS = 2000L;
    private static final long LOGIN_AFTER_REGISTER_DELAY_MS = 3000L;

    private final Minecraft mc = Minecraft.getMinecraft();

    private boolean inGame = false;
    private long joinTime = 0L;
    private boolean registerSent = false;
    private int loginAttempts = 0;
    private long pendingLoginAt = 0L;
    private boolean done = false;

    /* ------------------------------ 状态 ------------------------------ */

    private void onJoin() {
        inGame = true;
        joinTime = 0L;
        registerSent = false;
        loginAttempts = 0;
        pendingLoginAt = 0L;
        done = false;
    }

    private void onQuit() {
        inGame = false;
        joinTime = 0L;
        pendingLoginAt = 0L;
    }

    public void onTick() {
        if (mc.theWorld == null || mc.thePlayer == null) {
            if (inGame && mc.theWorld == null) {
                onQuit();
            }
            return;
        }
        if (!inGame) {
            onJoin();
        }

        long now = System.currentTimeMillis();
        if (joinTime == 0L) {
            joinTime = now;
        }
        long elapsed = now - joinTime;

        if (!done && elapsed > Config.detectWindowMs()) {
            done = true;
            pendingLoginAt = 0L;
        }
        if (!done && pendingLoginAt > 0L && now >= pendingLoginAt) {
            pendingLoginAt = 0L;
            doLogin(true);
        }
    }

    /* ------------------------------ 事件 ------------------------------ */

    @SubscribeEvent
    public void onConnected(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        onJoin();
    }

    @SubscribeEvent
    public void onDisconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        onQuit();
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!Config.loginEnabled() || !inGame || done || event.message == null) {
            return;
        }
        String raw = event.message.getUnformattedText();
        if (raw == null || raw.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (joinTime > 0L && now - joinTime > Config.detectWindowMs() + GRACE_MS) {
            return;
        }

        String msg = raw.toLowerCase(Locale.ENGLISH);
        String password = Config.password();
        if (password != null && !password.isEmpty() && msg.contains(password.toLowerCase(Locale.ENGLISH))) {
            return; // 自己发出去的指令回显
        }

        if (isSuccess(msg)) {
            done = true;
            pendingLoginAt = 0L;
            print("[SunnBT] 已登录成功，停止检测");
            return;
        }

        if (msg.contains("already registered") || msg.contains("已被注册") || msg.contains("已被占用")) {
            doLogin(false);
            return;
        }

        boolean needRegister = Config.detectRegisterPrompt() && (
                msg.contains("/register") || msg.contains("/reg ")
                        || msg.contains("请注册") || msg.contains("请先注册")
                        || msg.contains("please register") || msg.contains("register with /"));
        boolean needLogin = Config.detectLoginPrompt() && (
                msg.contains("/login") || msg.contains("/l ")
                        || msg.contains("请登录") || msg.contains("请先登录")
                        || msg.contains("please login") || msg.contains("login with /"));

        if (needRegister) {
            doRegister();
        } else if (needLogin) {
            doLogin(false);
        }
    }

    private boolean isSuccess(String msg) {
        return msg.contains("successfully registered")
                || msg.contains("successfully logged in")
                || msg.contains("logged in successfully")
                || msg.contains("you are now logged in")
                || msg.contains("already logged in")
                || msg.contains("注册成功")
                || msg.contains("登录成功")
                || msg.contains("登陆成功")
                || msg.contains("已登录")
                || msg.contains("已成功登录");
    }

    /* ---------------------------- 发送指令 ---------------------------- */

    private void doRegister() {
        if (registerSent || !canAct()) {
            return;
        }
        registerSent = true;
        send(Config.registerCommand());
        print("[SunnBT] 检测到注册提示，已发送注册指令");
        if (Config.sendLoginAfterRegister()) {
            pendingLoginAt = System.currentTimeMillis() + LOGIN_AFTER_REGISTER_DELAY_MS;
        }
    }

    private void doLogin(boolean fromDelay) {
        if (!fromDelay && registerSent && !Config.sendLoginAfterRegister()) {
            return; // 已经注册过了，默认不再登录
        }
        if (loginAttempts >= Config.maxLoginAttempts() || !canAct()) {
            return;
        }
        loginAttempts++;
        pendingLoginAt = 0L;
        send(Config.loginCommand());
        print("[SunnBT] 已发送登录指令（第 " + loginAttempts + " 次）");
    }

    private boolean canAct() {
        return Config.loginEnabled() && inGame && !done && mc.thePlayer != null
                && (joinTime == 0L || System.currentTimeMillis() - joinTime <= Config.detectWindowMs() + GRACE_MS);
    }

    private void send(String command) {
        if (command == null || command.isEmpty()) {
            return;
        }
        try {
            mc.thePlayer.sendChatMessage(command);
        } catch (Throwable throwable) {
            SunnBTMods.LOGGER.error("[SunnBT] 发送指令失败: " + command, throwable);
        }
    }

    private void print(String text) {
        if (!Config.chatLogEnabled()) {
            return;
        }
        try {
            mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(text));
        } catch (Throwable ignored) {
        }
    }
}
