package com.sunnbt.tools;

import java.util.List;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 往多人游戏列表加「Offline Accounts」按钮、往断线界面加「Rename &amp; Rejoin」按钮。
 * 两个按钮都可以在配置界面里单独关掉。
 */
@SideOnly(Side.CLIENT)
public class ClientEvents {

    private static final int BUTTON_ID = 3504;
    private static final int ACCOUNT_BUTTON_ID = 3505;

    private static final String LABEL_IDLE = "Rename & Rejoin";
    private static final String LABEL_RENAMED = "Renamed: ";
    private static final String LABEL_FAILED = "Rename failed";
    private static final String LABEL_CHANGE_ACCOUNT = "Offline Accounts";

    private int reconnectDelay = 0;
    private GuiScreen pendingParent = null;
    private ServerData pendingServer = null;

    /* --------------------------- 配置保存 --------------------------- */

    /** Forge 配置界面点 Done 后会把值写进 Property，这里负责落盘。 */
    @SubscribeEvent
    public void onConfigChanged(net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent event) {
        if (SunnBTMods.MODID.equals(event.modID)) {
            Config.save();
        }
    }

    /* --------------------------- 往界面加按钮 --------------------------- */

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!Config.renameEnabled()) {
            return;
        }
        GuiScreen gui = event.gui;

        if (gui instanceof GuiMultiplayer) {
            if (Config.showAccountsButton()) {
                event.buttonList.add(new GuiButton(
                        ACCOUNT_BUTTON_ID, 8, 6, 150, 20, LABEL_CHANGE_ACCOUNT));
            }
            return;
        }

        if (!(gui instanceof GuiDisconnected)) {
            return;
        }
        if (!Config.showRenameOnDisconnect()) {
            return;
        }
        if (!RenameHelper.isServerDisconnect((GuiDisconnected) gui)) {
            return;
        }

        List<GuiButton> buttons = event.buttonList;

        GuiButton backButton = null;
        for (GuiButton button : buttons) {
            if (button != null && button.id == 0) {
                backButton = button;
                break;
            }
        }

        int anchorY = (backButton != null) ? backButton.yPosition : (gui.height / 2 + 40);
        int newY = anchorY + 24;
        if (newY + 20 > gui.height - 2) {
            newY = anchorY - 24;
        }

        buttons.add(new GuiButton(BUTTON_ID, gui.width / 2 - 100, newY, 200, 20, LABEL_IDLE));
    }

    /* --------------------------- 点击按钮处理 --------------------------- */

    @SubscribeEvent
    public void onActionPerformed(GuiScreenEvent.ActionPerformedEvent.Post event) {
        GuiButton button = event.button;
        if (button == null) {
            return;
        }

        if (button.id == ACCOUNT_BUTTON_ID) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiAccountManager(event.gui));
            return;
        }

        if (button.id != BUTTON_ID) {
            return;
        }

        GuiScreen gui = event.gui;
        if (!(gui instanceof GuiDisconnected)) {
            return;
        }
        if (reconnectDelay > 0) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        String previousName = (mc.getSession() != null) ? mc.getSession().getUsername() : null;
        String newName = RenameHelper.generateName(previousName);
        UUID uuid = RenameHelper.offlineUUID(newName);

        if (!RenameHelper.applySession(newName, uuid)) {
            button.displayString = LABEL_FAILED;
            return;
        }

        button.displayString = LABEL_RENAMED + newName;

        pendingParent = RenameHelper.parentScreen((GuiDisconnected) gui);
        pendingServer = mc.getCurrentServerData();
        reconnectDelay = Config.reconnectDelayTicks();
    }

    /* -------------------------- 重连倒计时 tick -------------------------- */

    public void onClientTick() {
        if (reconnectDelay <= 0) {
            return;
        }

        reconnectDelay--;
        if (reconnectDelay > 0) {
            return;
        }

        ServerData server = pendingServer;
        GuiScreen parent = pendingParent;
        pendingServer = null;
        pendingParent = null;

        RenameHelper.reconnect(parent, server);
    }
}
