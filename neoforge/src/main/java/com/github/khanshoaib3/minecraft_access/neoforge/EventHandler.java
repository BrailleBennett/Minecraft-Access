package com.github.khanshoaib3.minecraft_access.neoforge;

import com.github.khanshoaib3.minecraft_access.MainClass;
import com.github.khanshoaib3.minecraft_access.utils.KeyBindingsHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = MainClass.MOD_ID)
public class EventHandler {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        MainClass.clientTickEventsMethod(MinecraftClient.getInstance());
    }

    @SubscribeEvent
    public static void registerKeybindings(RegisterKeyMappingsEvent event) {
        for (KeyBinding kb : KeyBindingsHandler.getInstance().getKeys()) {
            event.register(kb);
        }
    }
}