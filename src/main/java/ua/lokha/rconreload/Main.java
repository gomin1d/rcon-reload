package ua.lokha.rconreload;

import lombok.SneakyThrows;
import lombok.extern.java.Log;
import net.minecraft.server.v1_12_R1.DedicatedServer;
import net.minecraft.server.v1_12_R1.RemoteConnectionThread;
import net.minecraft.server.v1_12_R1.RemoteControlListener;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

@Log
public class Main extends JavaPlugin {

    @SneakyThrows
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        try {
            reloadCron();
        } catch (IllegalStateException e) {
            sender.sendMessage("§cRcon нельзя перезапустить, он включен в server.properties.");
            return true;
        }

        sender.sendMessage("§aRcon перезагружен.");
        return true;
    }

    @SneakyThrows
    public static void reloadCron() {
        DedicatedServer server = (DedicatedServer) ((CraftServer) Bukkit.getServer()).getServer();

        Field pField = DedicatedServer.class.getDeclaredField("p");
        pField.setAccessible(true);
        RemoteControlListener rcon = (RemoteControlListener) pField.get(server);

        if (rcon == null) {
            throw new IllegalStateException("Rcon нельзя перезапустить, он включен в server.properties");
        }

        Field aField = RemoteConnectionThread.class.getDeclaredField("a");
        aField.setAccessible(true);
        aField.set(rcon, false);

        log.info("stop rcon");
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.getName().contains("RCON Listener")) {
                thread.interrupt();
                log.info("stop thread " + thread);
            }
        }

        log.info("start rcon");
        rcon.a();
    }
}
