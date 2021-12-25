package net.rdk31.autoshutdown;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AutoShutdown implements DedicatedServerModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("AutoShutdown");
    public static ConfigInstance CONFIG;

    private long shutdownTime = 0;

    @Override
    public void onInitializeServer() {
        LOGGER.info("AutoShutdown initialized");

        ServerTickEvents.END_SERVER_TICK.register(this::onTickEnd);
        ServerPlayConnectionEvents.JOIN.register(this::onPlayerJoined);
        ServerPlayConnectionEvents.DISCONNECT.register(this::onPlayerDisconnected);

        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
    }

    private void onServerStarting(MinecraftServer minecraftServer) {
        CONFIG = ConfigManager.loadConfig();
    }

    private void onTickEnd(MinecraftServer minecraftServer) {
        if (minecraftServer.getTicks() % (20 * CONFIG.checkPeriod) == 0) {
            checkPlayerCount(minecraftServer);
        }

        if (shutdownTime != 0 && shutdownTime < System.currentTimeMillis()) {
            shutdownTime = 0;
            LOGGER.info("Shutting down");
            minecraftServer.stop(false);
        }
    }

    private void onPlayerDisconnected(ServerPlayNetworkHandler serverPlayNetworkHandler, MinecraftServer minecraftServer) {
        checkPlayerCount(minecraftServer);
    }

    private void onPlayerJoined(ServerPlayNetworkHandler serverPlayNetworkHandler, PacketSender packetSender, MinecraftServer minecraftServer) {
        if (shutdownTime != 0) {
            LOGGER.info("Somebody joined the server, canceling the shutdown");
            shutdownTime = 0;
        }
    }

    private void checkPlayerCount(MinecraftServer minecraftServer) {
        if (minecraftServer.getCurrentPlayerCount() == 0 && shutdownTime == 0 && !minecraftServer.isStopping()) {
            LOGGER.info("Scheduling shutdown in " + CONFIG.shutdownDelay + " seconds");
            shutdownTime = System.currentTimeMillis() + CONFIG.shutdownDelay * 1000;
        }
    }
}
