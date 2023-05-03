package meteordevelopment.meteorclient.utils.world;

import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;

import static meteordevelopment.meteorclient.LemonClient.mc;

public class PlayerHelper {


    public static ArrayList<PlayerEntity> getPlayers() {
        ArrayList<PlayerEntity> players = new ArrayList<>();
        mc.world.getEntities().forEach(entity -> { if (entity instanceof PlayerEntity p) players.add(p);});
        return players;
    }

    public static PlayerEntity getPlayerByName(String name) {
        for (PlayerEntity p : getPlayers()) { if (p.getEntityName().equalsIgnoreCase(name)) return p; }
        return null;
    }



}
