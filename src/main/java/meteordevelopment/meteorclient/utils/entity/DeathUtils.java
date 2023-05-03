package meteordevelopment.meteorclient.utils.entity;

import meteordevelopment.meteorclient.LemonClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.chat.AutoEz;
import meteordevelopment.meteorclient.systems.modules.world.KillFx;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

import java.util.ArrayList;

import static meteordevelopment.meteorclient.LemonClient.mc;
import static meteordevelopment.meteorclient.utils.entity.EntityInfo.getName;

public class DeathUtils {
    private static final int DeathStatus = 3;

    @PreInit
    public static void init() {
        LemonClient.EVENT_BUS.subscribe(DeathUtils.class);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private static void onPacket(PacketEvent.Receive event) {
        if (getTargets().isEmpty()) return;

        if (!(event.packet instanceof EntityStatusS2CPacket packet)) return;
        if (packet.getStatus() != DeathStatus) return;

        Entity entity = packet.getEntity(mc.world);
        if (entity == null) return;

        if (entity instanceof PlayerEntity player && getTargets().contains(getName(player))) {
            Modules.get().get(AutoEz.class).onKill(player);
            Modules.get().get(KillFx.class).onKill(player);
        }
    }

    public static ArrayList<String> getTargets() {
        ArrayList<String> list = new ArrayList<>();

        for (Module module : Modules.get().getAll()) {
            String name = module.getInfoString();

            if (module.isActive() && name != null && !list.contains(name)) list.add(name);
        }

        try {
            list.removeIf(name -> !isName(name));
        } catch (Exception exception) {
            exception.fillInStackTrace();
        }

        return list;
    }

    private static boolean isName(String string) {
        ArrayList<PlayerListEntry> playerListEntries = new ArrayList<>(mc.getNetworkHandler().getPlayerList());

        for (PlayerListEntry entry : playerListEntries) {
            if (string.contains(entry.getProfile().getName())) return true;
        }

        return false;
    }
}
