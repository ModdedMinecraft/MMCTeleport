package net.moddedminecraft.mmcteleport;

import com.flowpowered.math.vector.Vector3d;
import com.google.inject.Inject;
import net.moddedminecraft.mmcteleport.commands.teleport;
import net.moddedminecraft.mmcteleport.commands.teleportBack;
import net.moddedminecraft.mmcteleport.commands.teleportHere;
import net.moddedminecraft.mmcteleport.commands.teleportSpawn;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.spongepowered.api.Sponge.getServer;

@SuppressWarnings("ResultOfMethodCallIgnored")
@Plugin(id = "mmcteleport", name = "MMCTeleport", version = "1.0")
class Main {

    @Inject
    private Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    @Inject
    @ConfigDir(sharedRoot = false)
    Path configDir;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path path;

    private YAMLConfigurationLoader UserD;

    private Main main = Main.this;

    private HashMap<String, String> uuids = new HashMap<>();
    private UUID pjoin;

    private CommandManager cmdManager = Sponge.getCommandManager();

    private Scheduler scheduler = Sponge.getScheduler();
    private Task.Builder taskBuilder = scheduler.createTaskBuilder();

    public Main() {
    }

    Main getMain()
    {
        return main;
    }
    private Logger getLogger()
    {
        return logger;
    }

    @Listener
    public void preInit(GamePreInitializationEvent event)
    {
        path.resolve("playerdata").toFile().mkdirs();
    }
    @Listener
    public void Init(GameInitializationEvent event) {
        loadCommands();
    }
    @Listener
    public void onServerStart(GameStartedServerEvent event) throws IOException {
        File folder = new File(this.configDir.resolve("playerdata/").toString());
        File[] fileList = new File(this.configDir.resolve("playerdata/").toString()).listFiles();
        logger.info("Mapping UUID's..");
        assert fileList != null;
        for (File i : fileList) {
            if (i.isFile()) {
                String name = i.getName().substring(0, i.getName().lastIndexOf("."));
                configurePlayer(name);
                if (!UserD.load().getNode("UUID").getString().equals("null")) {
                    uuids.put(name, UserD.load().getNode("UUID").getString());
                } else {
                    getLogger().info("error: No UUID in the userfile: " + i.getName());
                }
                UserD = null;
            }
        }
        logger.info("Finished Mapping (" + uuids.size() + ")");
        logger.info("MMCTeleport Loaded");
    }

    private void loadCommands() {
        // /mmctp spawn
        CommandSpec mmctpspawn = CommandSpec.builder()
                .permission("mmcteleport.teleport.spawn")
                .description(Text.of("Teleport an offline player to spawn"))
                .arguments(GenericArguments.player(Text.of("target")))
                .executor(new teleportSpawn())
                .build();
        // /mmctp here
        CommandSpec mmctphere = CommandSpec.builder()
                .permission("mmcteleport.teleport.here")
                .description(Text.of("Teleport an offline player to your location"))
                .executor(new teleportHere())
                .build();
        // /mmctp back
        CommandSpec mmctpback = CommandSpec.builder()
                .permission("mmcteleport.teleport.back")
                .description(Text.of("Teleport to your previous location"))
                .executor(new teleportBack())
                .build();
        // /mmctp
        CommandSpec mmctp = CommandSpec.builder()
                .description(Text.of("Teleport to a players log out location"))
                .permission("mmcteleport.teleport")
                .arguments(GenericArguments.user(Text.of("target")))
                .child(mmctpspawn, "spawn", "s")
                .child(mmctphere, "here", "h") //TODO Finsih Command
                .child(mmctpback, "back", "b") //TODO Finish Command
                .executor(new teleport())
                .build();

        cmdManager.register(this, mmctp, "mmctp", "mmcteleport");
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player player) throws IOException {
        this.sendSpawn(player);

        String playerString = player.getName();
        GameProfileManager gameProfileManager = Sponge.getGame().getServer().getGameProfileManager();
        Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        try {
            pjoin = userStorage.get().getOrCreate(gameProfileManager.get(player.getName(), false).get()).getUniqueId();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        if (!(uuids.containsValue(pjoin.toString()))) {
            configurePlayer(playerString);
            createFile();
            UserD.load().getNode("UUID").setValue(pjoin.toString());
            savePlayer();
            uuids.put(player.getName(), pjoin.toString());
        } else {
            String key = getkey(playerString);
            if (key != null && !(key.equals(player.getName()))) {
                File pfile = path.resolve(this.configDir.resolve("playerdata/" + key + ".yml")).toFile();
                File nname = new File(key);
                pfile.renameTo(nname);
                uuids.remove(key);
                uuids.put(player.getName(), pjoin.toString());
            }
        }
        configurePlayer(playerString);
        if (UserD.load().getNode("newPosition", "world").getString() != "null") {
            Location loc = new Location(getServer().getWorld("world").get(), 8, 64, 8);
            Optional<World> worldOp = (Sponge.getServer().getWorld(UserD.load().getNode("newPosition", "world").getString()));
            World world = null;
            if (worldOp.isPresent()) {
                world = worldOp.get();
            }
            double x = (UserD.load().getNode("newPosition", "x").getDouble());
            double y = (UserD.load().getNode("newPosition", "y").getDouble());
            double z = (UserD.load().getNode("newPosition", "z").getDouble());
            String setter = (UserD.load().getNode("newPosition", "setter").getString());
            Vector3d vector = new Vector3d(x, y, z);
            player.setLocationAndRotation(loc, vector);

            World finalWorld = world;
            taskBuilder.execute(new Runnable() {
                public void run() {
                    player.sendMessage(Text.of(TextColors.GRAY + "You were teleported!"));
                    player.sendMessage(Text.of(TextColors.DARK_PURPLE + "-----------------------------------------------------"));
                    player.sendMessage(Text.of(TextColors.DARK_GRAY + "Teleporter: " + setter));
                    player.sendMessage(Text.of(TextColors.DARK_GRAY + "Prev Pos: " + finalWorld + ", " + x + ", " + y + ", " + z));
                    if ((player.hasPermission("mmcteleporter.mmctback"))) {
                        player.sendMessage(Text.of(TextColors.DARK_GRAY + "You can use /mmctback to go back to where you were!"));
                    }
                }
            }).delay(20, TimeUnit.MILLISECONDS);

            if (!UserD.load().getNode("newPosition", "world").getString().equals("null")) {
                UserD.load().getNode("newPosition", "world").setValue("null");
                UserD.load().getNode("newPosition", "x").setValue("null");
                UserD.load().getNode("newPosition", "y").setValue("null");
                UserD.load().getNode("newPosition", "z").setValue("null");
                UserD.load().getNode("newPosition", "yaw").setValue("null");
                UserD.load().getNode("newPosition", "pitch").setValue("null");
                UserD.load().getNode("newPosition", "setter").setValue("null");
                savePlayer();
            }
        }
    }

    @Listener
    public void onPlayerQuit(ClientConnectionEvent.Disconnect event, @Getter("getTargetEntity") Player player) throws IOException {
        String playerString = player.getName();
        Location loc = player.getLocation();
        letsSet(player, "lastPosition", loc);
        configurePlayer(playerString);
        if (!UserD.load().getNode("newPosition", "world").getString().equals("null")) {
            UserD.load().getNode("newPosition", "world").setValue("null");
            UserD.load().getNode("newPosition", "x").setValue("null");
            UserD.load().getNode("newPosition", "y").setValue("null");
            UserD.load().getNode("newPosition", "z").setValue("null");
            UserD.load().getNode("newPosition", "yaw").setValue("null");
            UserD.load().getNode("newPosition", "pitch").setValue("null");
            UserD.load().getNode("newPosition", "setter").setValue("null");
            savePlayer();
        }
    }


    private void sendSpawn(Player player) {
        List spawnList = teleportSpawn.SpawnList;
        if(spawnList.contains(player.getName())) {

            World w = getServer().getWorld(getServer().getDefaultWorldName()).get();
            if(w.isLoaded()) {
                player.setLocation(w.getSpawnLocation());
                teleportSpawn.SpawnList.remove(player.getName());
            }
        }

    }

    private String getkey(String value) {
        for (Map.Entry<String, String> entry : uuids.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void configurePlayer(String player) {
        UserD = YAMLConfigurationLoader.builder().setPath(this.configDir.resolve("playerdata/" + player + ".yml")).build();
    }

    private void savePlayer() throws IOException {
        UserD.save(UserD.load());
        UserD = null;

    }

    private void letsSet(Player player, String list, Location loc) throws IOException {
        configurePlayer(player.getName());
        UserD.load().getNode(list ,"world").setValue(player.getWorld().getName());
        UserD.load().getNode(list ,"x").setValue(loc.getX());
        UserD.load().getNode(list ,"y").setValue(loc.getY());
        UserD.load().getNode(list ,"z").setValue(loc.getZ());
        UserD.load().getNode(list ,"yaw").setValue(player.getRotation().getY());
        UserD.load().getNode(list ,"pitch").setValue(player.getRotation().getX());
        try {
            savePlayer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createFile() throws IOException {
        URL defaultsInJarURL = Main.class.getResource("user.yml");
        YAMLConfigurationLoader defaultsLoader = YAMLConfigurationLoader.builder().setURL(defaultsInJarURL).build();
        ConfigurationNode defaults = defaultsLoader.load();

        ConfigurationNode rootNode = UserD.load();
        rootNode.mergeValuesFrom(defaults);
        UserD.save(rootNode);
    }

}
