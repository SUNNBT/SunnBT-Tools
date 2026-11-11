package com.sunnbt.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * 合并模组：自动登录 + 离线账号改名。By Sunnbt
 * （本工程由 AI 生成，见 README 开头声明）
 * 所有开关和参数都在 config/sunnbtmods.cfg，
 * 也可以在游戏主菜单 -> Mods -> SunnBT Tools -> Config 里改。
 */
@Mod(
        modid = SunnBTMods.MODID,
        name = SunnBTMods.NAME,
        version = SunnBTMods.VERSION,
        acceptedMinecraftVersions = "[1.8.9]",
        guiFactory = "com.sunnbt.tools.ModGuiFactory"
)
public class SunnBTMods {

    public static final String MODID = "sunnbtmods";
    public static final String NAME = "SunnBT Tools";
    public static final String VERSION = "1.3.0";

    public static final Logger LOGGER = LogManager.getLogger("SunnBT");

    @SidedProxy(clientSide = "com.sunnbt.tools.ClientProxy", serverSide = "com.sunnbt.tools.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Config.init(event.getSuggestedConfigurationFile());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }
}
