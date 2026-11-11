package com.sunnbt.tools;

import java.io.IOException;
import java.util.UUID;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 手动添加离线用户名：回车或 Add 提交，存入列表并立刻使用。
 */
@SideOnly(Side.CLIENT)
public class GuiAddAccount extends GuiScreen {

    private static final int BTN_OK = 1;
    private static final int BTN_CANCEL = 0;

    private final GuiAccountManager parent;
    private GuiTextField nameField;
    private String error = "";

    public GuiAddAccount(GuiAccountManager parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        Keyboard.enableRepeatEvents(true);

        this.nameField = new GuiTextField(0, this.fontRendererObj,
                this.width / 2 - 100, this.height / 2 - 32, 200, 20);
        this.nameField.setMaxStringLength(16);
        this.nameField.setFocused(true);

        this.buttonList.add(new GuiButton(BTN_OK,
                this.width / 2 - 102, this.height / 2 + 8, 100, 20, "Add"));
        this.buttonList.add(new GuiButton(BTN_CANCEL,
                this.width / 2 + 2, this.height / 2 + 8, 100, 20, "Cancel"));
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        super.onGuiClosed();
    }

    @Override
    public void updateScreen() {
        this.nameField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        this.drawCenteredString(this.fontRendererObj, "Add Offline User",
                this.width / 2, this.height / 2 - 68, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj,
                Config.minNameLength() + "-" + Config.maxNameLength() + " chars, only a-z A-Z 0-9 _",
                this.width / 2, this.height / 2 - 52, 0xAAAAAA);

        this.nameField.drawTextBox();

        if (this.error != null && !this.error.isEmpty()) {
            this.drawCenteredString(this.fontRendererObj, this.error,
                    this.width / 2, this.height / 2 + 38, 0xFF5555);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(this.parent);
            return;
        }
        if (keyCode == Keyboard.KEY_RETURN) {
            confirm();
            return;
        }
        this.nameField.textboxKeyTyped(typedChar, keyCode);
        this.error = "";
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == BTN_OK) {
            confirm();
        } else if (button.id == BTN_CANCEL) {
            this.mc.displayGuiScreen(this.parent);
        }
    }

    private void confirm() {
        String text = this.nameField.getText();
        String name = (text == null) ? "" : text.trim();

        if (!RenameHelper.isValidName(name)) {
            this.error = "Invalid name";
            return;
        }

        AccountStore.add(name);
        UUID uuid = RenameHelper.offlineUUID(name);
        if (!RenameHelper.applySession(name, uuid)) {
            this.error = "Failed to apply session";
            return;
        }

        this.mc.displayGuiScreen(this.parent);
    }
}
