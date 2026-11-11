package com.sunnbt.tools;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 1.8.9 的 ClientTickEvent 只在 FMLCommonHandler 的总线上发，
 * 所以两个模块的 tick 统一在这里转发。
 */
@SideOnly(Side.CLIENT)
public class TickDispatcher {

    private final ClientEvents clientEvents;
    private final AutoLoginHandler autoLogin;

    public TickDispatcher(ClientEvents clientEvents, AutoLoginHandler autoLogin) {
        this.clientEvents = clientEvents;
        this.autoLogin = autoLogin;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        autoLogin.onTick();
        clientEvents.onClientTick();
    }
}
