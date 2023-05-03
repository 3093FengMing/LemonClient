package meteordevelopment.meteorclient.utils.entity.fakeplayer;

import meteordevelopment.meteorclient.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static meteordevelopment.meteorclient.LemonClient.mc;

public class FakePlayerManager {
    public static final List<FakePlayerEntity> fakePlayers = new ArrayList<>();

    public static FakePlayerEntity get(String name) {
        for (FakePlayerEntity fp : fakePlayers) {
            if (fp.getEntityName().equals(name)) return fp;
        }

        return null;
    }

    public static void add(String name, float health, boolean copyInv) {
        if (!Utils.canUpdate()) return;

        FakePlayerEntity fakePlayer = new FakePlayerEntity(mc.player, name, health, copyInv);
        fakePlayer.spawn();
        fakePlayers.add(fakePlayer);
    }

    public static void remove(FakePlayerEntity fp) {
        fakePlayers.removeIf(fp1 -> {
            if (fp1.getEntityName().equals(fp.getEntityName())) {
                fp1.despawn();
                return true;
            }

            return false;
        });
    }

    public static void clear() {
        if (fakePlayers.isEmpty()) return;
        fakePlayers.forEach(FakePlayerEntity::despawn);
        fakePlayers.clear();
    }

    public static void forEach(Consumer<FakePlayerEntity> action) {
        for (FakePlayerEntity fakePlayer : fakePlayers) {
            action.accept(fakePlayer);
        }
    }

    public static int count() {
        return fakePlayers.size();
    }

    public static Stream<FakePlayerEntity> stream() {
        return fakePlayers.stream();
    }

    public static boolean contains(FakePlayerEntity fp) {
        return fakePlayers.contains(fp);
    }

    public static List<FakePlayerEntity> getPlayers() {
        return fakePlayers;
    }

    public static int size() {
        return fakePlayers.size();
    }
}
