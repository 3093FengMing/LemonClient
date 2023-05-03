package meteordevelopment.meteorclient.systems.hud.modules;

import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.systems.hud.HUD;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import net.minecraft.item.BedItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class BedHud extends HudElement {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // General

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("The scale.")
        .defaultValue(2)
        .min(1)
        .sliderRange(1, 5)
        .build()
    );

    public BedHud(HUD hud) {
        super(hud, "bed", "Displays the amount of beds in your inventory.", true);
    }

    @Override
    public void update(HudRenderer renderer) {
        box.setSize(16 * scale.get(), 16 * scale.get());
    }

    @Override
    public void render(HudRenderer renderer) {
        double x = box.getX();
        double y = box.getY();

        if (isInEditor()) RenderUtils.drawItem(Items.RED_BED.getDefaultStack(), (int) x, (int) y, scale.get(), true);
        else if (InvUtils.find(itemStack -> itemStack.getItem() instanceof BedItem).count() > 0) RenderUtils.drawItem(new ItemStack(Items.RED_BED, InvUtils.find(itemStack -> itemStack.getItem() instanceof BedItem).count()), (int) x, (int) y, scale.get(), true);
    }
}
