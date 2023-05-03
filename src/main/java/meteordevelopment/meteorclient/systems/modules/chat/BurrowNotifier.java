package meteordevelopment.meteorclient.systems.modules.chat;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.render.Notifications;
import meteordevelopment.meteorclient.utils.player.AutomationUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class BurrowNotifier extends Module {
    public static List<PlayerEntity> burrowedPlayers = new ArrayList<>();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgNone = settings.createGroup("");

    // General

    private final Setting<Notifications.Mode> notifications = sgNone.add(new EnumSetting.Builder<Notifications.Mode>()
        .name("notifications")
        .defaultValue(Notifications.Mode.Chat)
        .build()
    );

    private final Setting<NotificationType> notificationType = sgGeneral.add(new EnumSetting.Builder<NotificationType>()
        .name("type")
        .description("Determines when to notify you.")
        .defaultValue(NotificationType.Both)
        .build()
    );

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("How far away from you to check for burrowed players.")
        .defaultValue(3)
        .min(0)
        .sliderMax(15)
        .build()
    );

    public BurrowNotifier() {
        super(Categories.Chat, "burrow-notifier", "Notifies you when a player burrows in your render distance.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (isBurrowValid(player)) {
                burrowedPlayers.add(player);

                if (notificationType.get() == NotificationType.Not_Burrowed) sendBurrowedNotification(player.getGameProfile().getName());
            }

            if (burrowedPlayers.contains(player) && !AutomationUtils.isBurrowed(player, true)) {
                burrowedPlayers.remove(player);

                if (notificationType.get() == NotificationType.Burrowed) sendNotBurrowedNotification(player.getGameProfile().getName());
            }
        }
    }

    private boolean isBurrowValid(PlayerEntity p) {
        if (p == mc.player) return false;
        return mc.player.distanceTo(p) <= range.get() && !burrowedPlayers.contains(p) && AutomationUtils.isBurrowed(p, true) && !PlayerUtils.isPlayerMoving(p);
    }

    private void sendBurrowedNotification(String playerName) {
        switch (notifications.get()) {
            case Chat -> warning("(highlight)%s(default) is burrowed!", playerName);
            case Toast -> Notifications.send(playerName + " is burrowed!", notifications);
        }
    }

    private void sendNotBurrowedNotification(String playerName) {
        switch (notifications.get()) {
            case Chat -> warning("(highlight)%s(default) is no longer burrowed!", playerName);
            case Toast -> Notifications.send(playerName + " is no longer burrowed!", notifications);
        }
    }

    public enum NotificationType {
        Burrowed("Burrowed"),
        Not_Burrowed("Not Burrowed"),
        Both("Both");

        private final String title;

        NotificationType(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}
