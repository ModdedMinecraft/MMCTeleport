package net.moddedminecraft.mmcteleport.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public class teleport implements CommandExecutor {



    @Override
    public CommandResult execute(CommandSource src, CommandContext commandContext) throws CommandException {
        //TODO Sort out the teleportation.
        return CommandResult.success();

        /*if (src instanceof Player) {
            Player sender = (Player) src;
            if(!commandContext.hasAny("target")) {
                src.sendMessage(Text.of(new Object[]{TextColors.RED, "Invalid arguments"}));
                return CommandResult.success();
            } else {
                User user = commandContext.<User>getOne("target").get();
                if (user.isOnline()) {
                    sender.sendMessage(Text.of(TextColors.RED, "Player is online, Please use /teleport"));
                } else {
                    GameProfileManager gameProfileManager = Sponge.getGame().getServer().getGameProfileManager();
                    Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
                    UUID uuid = null;
                    try {
                        uuid = userStorage.get().getOrCreate(gameProfileManager.get(user.getName(), false).get()).getUniqueId();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    if (userStorage.isPresent()) {
                        UserStorageService userStorage2 = userStorage.get();
                        Optional<User> userOptional = userStorage2.get(uuid);
                        if (userOptional.isPresent()) {
                            User user2 = userOptional.get();
                            double x = user2.getPlayer().get().getLocation().getX();
                            double y = user2.getPlayer().get().getLocation().getY();
                            double z = user2.getPlayer().get().getLocation().getZ();
                            sender.sendMessage(Text.of("Loc: x:", x, " y:", y, " z:", z));
                            Vector3d vector = new Vector3d(x, y, z);
                            sender.setLocation(sender.getLocation().setPosition(vector));
                        } else {
                            // error?
                        }
                    }
                }
                return CommandResult.success();
            }
        } else {
            src.sendMessage(Text.of("Only a player Can run this command!"));
            return CommandResult.success();
        }*/
    }
}
