package art.ameliah.fabric.autosprintfix.core.util;

import java.util.stream.Collectors;

import art.ameliah.fabric.autosprintfix.core.logger.ModLogger;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FriendsManager {

    private static final ModLogger logger = ModLogger.getInstance();

    private static final Path configDir = Minecraft.getInstance().gameDirectory
            .toPath()
            .resolve("config")
            .resolve("autosprintfix");

    private static final Set<String> friends = new HashSet<>();

    private static final Path friendsFile = configDir.resolve("Friends.txt");

    public static void load() {
        try {
            if (!Files.exists(friendsFile)) {
                Files.createFile(friendsFile);

                return;
            }

            friends.clear();
            friends.addAll(
                    Files.readAllLines(friendsFile)
                            .stream()
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(String::toLowerCase)
                            .collect(Collectors.toSet()));
        } catch (IOException e) {
            logger.error("Failed to load friends list: " + e.getMessage());
        }
    }

    public static void save() {
        try {
            Files.write(friendsFile, friends);
        } catch (IOException e) {
            logger.error("Failed to save friends list: " + e.getMessage());
        }
    }

    public static boolean add(String name) {
        boolean added = friends.add(name.toLowerCase());
        if (added)
            save();

        return added;
    }

    public static boolean remove(String name) {
        boolean removed = friends.remove(name.toLowerCase());
        if (removed)
            save();

        return removed;
    }

    public static boolean isFriend(String name) {
        return friends.contains(name.toLowerCase());
    }

    public static Set<String> getFriends() {
        return Collections.unmodifiableSet(friends);
    }
}
