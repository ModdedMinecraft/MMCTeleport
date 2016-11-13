package net.moddedminecraft.mmcteleport.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public class teleportBack implements CommandExecutor {

    public teleportBack() {
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext commandContext) throws CommandException {
        //TODO Start TPBACK
        return CommandResult.success();
    }

}
