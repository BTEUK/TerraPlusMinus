package de.btegermany.terraplusminus.commands;

import de.btegermany.terraplusminus.Terraplusminus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OffsetCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (command.getName().equalsIgnoreCase("offset")) {
            Player player = (Player) commandSender;
            if (player.hasPermission("t+-.offset")) {
                player.sendMessage(Terraplusminus.config.getString("prefix") + "§7Offsets:");
                player.sendMessage(Terraplusminus.config.getString("prefix") + "§7 | X: §8" + Terraplusminus.config.getInt("terrain_offset.x"));
                player.sendMessage(Terraplusminus.config.getString("prefix") + "§7 | Y: §8" + Terraplusminus.config.getInt("terrain_offset.y"));
                player.sendMessage(Terraplusminus.config.getString("prefix") + "§7 | Z: §8" + Terraplusminus.config.getInt("terrain_offset.z"));
            } else {
                player.sendMessage(Terraplusminus.config.getString("prefix") + "§7No permission for /offset");
                return true;
            }
        }
        return true;
    }
}
