package net.rdk31.autoshutdown;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AutoShutdown implements DedicatedServerModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("AutoShutdown");

    private long shutdownTime = 0;

    @Override
    public void onInitializeServer() {
        LOGGER.info("AutoShutdown initialized");

        ServerTickEvents.END_SERVER_TICK.register(this::checkPlayerCount);
        ServerPlayConnectionEvents.JOIN.register(this::playerJoined);
    }

    private void playerJoined(ServerPlayNetworkHandler serverPlayNetworkHandler, PacketSender packetSender, MinecraftServer minecraftServer) {
        if (shutdownTime != 0) {
            LOGGER.info("canceling shutdown");
            shutdownTime = 0;
        }
    }

    private void checkPlayerCount(MinecraftServer server) {
        long currentTime = System.currentTimeMillis();

        if (server.getTicks() % (20 * 5) == 0) {
            LOGGER.info("current count: " + server.getCurrentPlayerCount());
            if (server.getCurrentPlayerCount() == 0 && shutdownTime == 0 && !server.isStopping()) {
                LOGGER.info("scheduling shutdown in 15s");
                shutdownTime = currentTime + 15 * 1000;
            }
        }

        if (shutdownTime != 0 && shutdownTime < currentTime) {
            shutdownTime = 0;
            server.stop(false);
        }
    }
}
