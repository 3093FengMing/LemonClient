package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.events.entity.player.InteractEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

public class MultiTask extends Module {

    public MultiTask() {
        super(Categories.Player, "multi-task", "Allows you to eat while mining a block.");
    }

    @EventHandler
    public void onInteractEvent(InteractEvent event) {
        event.usingItem = false;
    }
}
