package com.sunnbt.tools;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 离线账号管理界面：点列表里的一行即切换账号，下面有添加 / 随机 / 完成。
 */
@SideOnly(Side.CLIENT)
public class GuiAccountManager extends GuiScreen {

    private static final int BTN_ADD = 1;
    private static final int BTN_RANDOM = 2;
    private static final int BTN_DONE = 3;

    private static final int ROW_HEIGHT = 20;
    private static final int LIST_TOP = 46;
    private static final int LIST_WIDTH = 260;

    private final GuiScreen parent;
    private int scroll = 0;
    private String status = "";

    public GuiAccountManager(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();

        int bottomY = this.height - 46;
        int buttonWidth = 100;
        this.buttonList.add(new GuiButton(BTN_ADD,
                this.width / 2 - buttonWidth - 2, bottomY, buttonWidth, 20, "Add Offline User"));
        this.buttonList.add(new GuiButton(BTN_RANDOM,
                this.width / 2 + 2, bottomY, buttonWidth, 20, "Random Name"));
        this.buttonList.add(new GuiButton(BTN_DONE,
                this.width / 2 - 50, bottomY + 24, 100, 20, "Done"));

        this.status = RenameHelper.currentName();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        this.drawCenteredString(this.fontRendererObj, "Offline Accounts", this.width / 2, 14, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, "Now using: " + this.status,
                this.width / 2, 28, 0xAAAAAA);

        List<String> names = AccountStore.getNames();
        int left = this.width / 2 - LIST_WIDTH / 2;

        if (names.isEmpty()) {
            this.drawCenteredString(this.fontRendererObj, "No offline accounts yet",
                    this.width / 2, LIST_TOP + 8, 0x888888);
        } else {
            int rows = visibleRows();
            for (int i = 0; i < rows; i++) {
                int index = this.scroll + i;
                if (index >= names.size()) {
                    break;
                }
                String name = names.get(index);
                int rowY = LIST_TOP + i * ROW_HEIGHT;
                boolean hovered = mouseX >= left && mouseX <= left + LIST_WIDTH
                        && mouseY >= rowY && mouseY <= rowY + ROW_HEIGHT - 2;
                boolean current = name.equalsIgnoreCase(this.status);
                int background = current ? 0x8033AA33 : (hovered ? 0x60FFFFFF : 0x40000000);
                drawRect(left, rowY, left + LIST_WIDTH, rowY + ROW_HEIGHT - 2, background);
                this.drawString(this.fontRendererObj, name, left + 6, rowY + 5, 0xFFFFFF);
                if (current) {
                    this.drawString(this.fontRendererObj, "using", left + LIST_WIDTH - 40, rowY + 5, 0x55FF55);
                }
            }
            if (maxScroll() > 0) {
                this.drawString(this.fontRendererObj, "scroll " + (this.scroll + 1) + "/" + maxScroll(),
                        left + LIST_WIDTH + 6, LIST_TOP + 4, 0x777777);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0) {
            return;
        }

        List<String> names = AccountStore.getNames();
        if (names.isEmpty()) {
            return;
        }
        int left = this.width / 2 - LIST_WIDTH / 2;
        if (mouseX < left || mouseX > left + LIST_WIDTH) {
            return;
        }
        if (mouseY < LIST_TOP) {
            return;
        }

        int row = (mouseY - LIST_TOP) / ROW_HEIGHT;
        int rowY = LIST_TOP + row * ROW_HEIGHT;
        if (mouseY > rowY + ROW_HEIGHT - 2) {
            return;
        }

        int index = this.scroll + row;
        if (index < 0 || index >= names.size()) {
            return;
        }
        useAccount(names.get(index));
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) {
            return;
        }
        this.scroll += (wheel > 0 ? -1 : 1);
        if (this.scroll < 0) {
            this.scroll = 0;
        }
        int limit = maxScroll();
        if (this.scroll > limit) {
            this.scroll = limit;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case BTN_ADD:
                this.mc.displayGuiScreen(new GuiAddAccount(this));
                break;
            case BTN_RANDOM:
                useRandom();
                break;
            case BTN_DONE:
                this.mc.displayGuiScreen(this.parent != null ? this.parent : new GuiMainMenu());
                break;
            default:
                break;
        }
    }

    private void useAccount(String name) {
        UUID uuid = RenameHelper.offlineUUID(name);
        if (RenameHelper.applySession(name, uuid)) {
            this.status = name;
        } else {
            this.status = "failed";
        }
    }

    private void useRandom() {
        String previous = RenameHelper.currentName();
        String name = RenameHelper.generateName(previous);
        AccountStore.add(name);
        useAccount(name);
        int limit = maxScroll();
        this.scroll = (limit > 0) ? limit : 0;
    }

    private int listBottom() {
        return this.height - 54;
    }

    private int visibleRows() {
        return Math.max(1, (listBottom() - LIST_TOP) / ROW_HEIGHT);
    }

    private int maxScroll() {
        return Math.max(0, AccountStore.getNames().size() - visibleRows());
    }
}
