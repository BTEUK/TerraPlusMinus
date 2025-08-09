package de.btegermany.terraplusminus.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.buildtheearth.terraminusminus.util.http.Http;
import org.jetbrains.annotations.NotNull;

public class ConfigChanged implements BasicCommand {
    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        Http.configChanged();
    }
}
