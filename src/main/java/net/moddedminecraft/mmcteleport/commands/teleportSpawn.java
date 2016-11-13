package net.moddedminecraft.mmcteleport.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;

public class teleportSpawn implements CommandExecutor {
    public static List<String> SpawnList = new ArrayList();

    public teleportSpawn() {
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext commandContext) throws CommandException {
        if(!commandContext.hasAny("target")) {
            src.sendMessage(Text.of(new Object[]{TextColors.RED, "Invalid arguments"}));
            return CommandResult.success();
        } else {
            User user = commandContext.<User>getOne("target").get();
            String target = user.getName();
            if(SpawnList.contains(target)) {
                src.sendMessage(Text.of(new Object[]{TextColors.RED, target + " Is already being sent to spawn!"}));
                return CommandResult.success();
            } else {
                SpawnList.add(target);
                src.sendMessage(Text.of(new Object[]{TextColors.GREEN, target + " Is being sent to spawn upon login!"}));
                return CommandResult.success();
            }
        }
    }

    public List<String> getSpawnList() {
        return SpawnList;
    }
}
