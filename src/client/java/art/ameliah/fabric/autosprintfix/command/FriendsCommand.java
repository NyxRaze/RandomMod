package art.ameliah.fabric.autosprintfix.command;

import art.ameliah.fabric.autosprintfix.core.util.FriendsManager;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.network.chat.Component;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class FriendsCommand {
        @SuppressWarnings("null")
        public static void register() {
                ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
                        dispatcher.register(literal("friends")
                                        .then(literal("add")
                                                        .then(argument("name", StringArgumentType.word())
                                                                        .executes(ctx -> {
                                                                                String name = StringArgumentType
                                                                                                .getString(ctx, "name");
                                                                                boolean added = FriendsManager
                                                                                                .add(name);

                                                                                ctx.getSource().sendFeedback(
                                                                                                Component.literal(
                                                                                                                added
                                                                                                                                ? "§aAdded friend: "
                                                                                                                                                + name
                                                                                                                                : "§cAlready a friend: "
                                                                                                                                                + name));
                                                                                return 1;
                                                                        })))

                                        .then(literal("remove")
                                                        .then(argument("name", StringArgumentType.word())
                                                                        .executes(ctx -> {
                                                                                String name = StringArgumentType
                                                                                                .getString(ctx, "name");
                                                                                boolean removed = FriendsManager
                                                                                                .remove(name);

                                                                                ctx.getSource().sendFeedback(
                                                                                                Component.literal(
                                                                                                                removed
                                                                                                                                ? "§aRemoved friend: "
                                                                                                                                                + name
                                                                                                                                : "§cNot in friends list: "
                                                                                                                                                + name));
                                                                                return 1;
                                                                        })))

                                        .then(literal("list")
                                                        .executes(ctx -> {
                                                                var friends = FriendsManager.getFriends();

                                                                ctx.getSource().sendFeedback(
                                                                                Component.literal(
                                                                                                friends.isEmpty()
                                                                                                                ? "§7Friends list is empty."
                                                                                                                : "§aFriends (§f"
                                                                                                                                + friends.size()
                                                                                                                                + "§a): §f"
                                                                                                                                + String.join(", ",
                                                                                                                                                friends)));
                                                                return 1;
                                                        })));
                });
        }
}
