package meteordevelopment.meteorclient.systems.modules.render;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class CustomFOV extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> fovSetting = sgGeneral.add(new IntSetting.Builder()
        .name("fov")
        .description("Your custom fov.")
        .defaultValue(100)
        .sliderMin(1)
        .sliderMax(180)
        .build()
    );

    private int fov;

    public CustomFOV() {
        super(Categories.Render, "custom-fov", "Allows your FOV to be more customizable.");
    }

    @Override
    public void onActivate() {
        fov = mc.options.getFov().getValue();
        mc.options.getFov().setValue(fovSetting.get());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        mc.options.getFov().setValue(fovSetting.get());
    }

    @Override
    public void onDeactivate() {
        mc.options.getFov().setValue(fov);
    }
}
