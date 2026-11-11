package com.sunnbt.tools;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Forge 原生配置界面（主菜单 Mods 列表里的 Config 按钮打开）。
 * 左边是分类 autologin / autorename，右边是每个可编辑项。
 */
@SideOnly(Side.CLIENT)
public class GuiModConfig extends GuiConfig {

    public GuiModConfig(GuiScreen parentScreen) {
        super(parentScreen, configElements(), SunnBTMods.MODID, false, false,
                "SunnBT Tools Configuration");
    }

    private static List<IConfigElement> configElements() {
        List<IConfigElement> elements = new ArrayList<IConfigElement>();
        if (Config.loginCategory() != null) {
            elements.addAll(new ConfigElement(Config.loginCategory()).getChildElements());
        }
        if (Config.renameCategory() != null) {
            elements.addAll(new ConfigElement(Config.renameCategory()).getChildElements());
        }
        return elements;
    }

    @Override
    public void onGuiClosed() {
        Config.save();
        super.onGuiClosed();
    }
}
