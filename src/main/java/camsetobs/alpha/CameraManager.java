package camsetobs.alpha;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec2f;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import java.util.EnumSet;
import java.util.HashMap;







public class CameraManager {
    private static final HashMap<String, Vec3d> cameraPositions = new HashMap<>();
    private static final HashMap<String, Vec2f> cameraRotations = new HashMap<>();





    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> register(dispatcher)
        );
    }

    private static ServerPlayerEntity obsControlledPlayer = null;

    public static void setObsControlledPlayer(ServerPlayerEntity player) {
        obsControlledPlayer = player;

    }







    private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // /setcam
        dispatcher.register(CommandManager.literal("setcam")
                .then(CommandManager.argument("name", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            for (String name : cameraPositions.keySet()) {
                                builder.suggest(name);
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            String name = StringArgumentType.getString(context, "name");
                            Vec3d pos = player.getPos();
                            Vec2f rot = new Vec2f(player.getYaw(), player.getPitch());
                            cameraPositions.put(name, pos);
                            cameraRotations.put(name, rot);
                            context.getSource().sendFeedback(() -> Text.literal("Set camera '" + name + "' at " + pos.x + ", " + pos.y + ", " + pos.z), false);
                            return 1;
                        }))
        );


        // /cam
        dispatcher.register(CommandManager.literal("cam")
                .then(CommandManager.argument("name", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            for (String name : cameraPositions.keySet()) {
                                builder.suggest(name);
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            String name = StringArgumentType.getString(context, "name");

                            if (!cameraPositions.containsKey(name)) {
                                context.getSource().sendError(Text.literal("Camera '" + name + "' not found."));
                                return 0;
                            }

                            Vec3d pos = cameraPositions.get(name);
                            Vec2f rot = cameraRotations.getOrDefault(name, new Vec2f(0, 0));

                            String posStr = String.format("(%.1f, %.1f, %.1f)", pos.x, pos.y, pos.z);
                            context.getSource().sendFeedback(() -> Text.literal("Set camera '" + name + "' at " + posStr), false);
                            return 1;
                        }))
        );

        // /delcam
        dispatcher.register(CommandManager.literal("delcam")
                .then(CommandManager.argument("name", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            for (String name : cameraPositions.keySet()) {
                                builder.suggest(name);
                            }
                            return builder.buildFuture();
                        })
                            .executes(context -> {
                                String name = StringArgumentType.getString(context, "name");
                                if (cameraPositions.remove(name) != null) {
                                    cameraRotations.remove(name);
                                    context.getSource().sendFeedback(() -> Text.literal("Deleted camera '" + name + "'."), false);
                                    return 1;
                                } else {
                                    context.getSource().sendError(Text.literal("Camera '" + name + "' not found."));
                                    return 0;
                                }
                            }))
        );


        // /obsswitch
        dispatcher.register(CommandManager.literal("obsswitch")
                .then(CommandManager.argument("name", com.mojang.brigadier.arguments.StringArgumentType.string())
                        .suggests((context, builder) -> {
                            for (String name : cameraPositions.keySet()) {
                                builder.suggest(name);
                            }
                            return builder.buildFuture();
                        })
                            .executes(context -> {
                                String name = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "name");
                                OBSWebSocket.switchToScene(name);
                                context.getSource().sendFeedback(() -> Text.literal("scene '" + name + "'."), false);
                                return 1;
                            }))
        );

        // /obsconnet
        dispatcher.register(CommandManager.literal("obsconnect")
                .then(CommandManager.argument("ip", com.mojang.brigadier.arguments.StringArgumentType.string())
                        .then(CommandManager.argument("port", com.mojang.brigadier.arguments.IntegerArgumentType.integer())
                                .executes(context -> {
                                    String ip = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "ip");
                                    int port = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "port");
                                    String wsUrl = "ws://" + ip + ":" + port;

                                    OBSWebSocket.connect(wsUrl);
                                    context.getSource().sendFeedback(() -> Text.literal("Connecting" + wsUrl), false);
                                    return 1;


                                })))
        );
        // /obscontrol
        dispatcher.register(CommandManager.literal("obscontrol")
                .executes(context -> {
                    obsControlledPlayer = context.getSource().getPlayer();
                    context.getSource().sendFeedback(() -> Text.literal("You are now controlled OBS"), false);
                    return 1;
                })
        );


        // / camsetposition
        OBSWebSocket.setOnSceneChanged(scene -> {
            if (obsControlledPlayer != null && cameraPositions.containsKey(scene)) {
                obsControlledPlayer.teleport(
                        obsControlledPlayer.getServerWorld(),
                        cameraPositions.get(scene).x,
                        cameraPositions.get(scene).y,
                        cameraPositions.get(scene).z,
                        EnumSet.noneOf(PositionFlag.class),
                        cameraRotations.get(scene).x,
                        cameraRotations.get(scene).y,
                        false
                );
                obsControlledPlayer.sendMessage(Text.literal("Warped to camera: " + scene));
            }
        });

    }
}
