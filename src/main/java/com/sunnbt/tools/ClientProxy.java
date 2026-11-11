package com.sunnbt.tools;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void init() {
        ClientEvents clientEvents = new ClientEvents();
        AutoLoginHandler autoLogin = new AutoLoginHandler();

        MinecraftForge.EVENT_BUS.register(clientEvents);
        MinecraftForge.EVENT_BUS.register(autoLogin);
        FMLCommonHandler.instance().bus().register(new TickDispatcher(clientEvents, autoLogin));
    }
}
