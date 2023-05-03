package meteordevelopment.meteorclient.systems.hud.modules;

import meteordevelopment.meteorclient.LemonClient;
import meteordevelopment.meteorclient.systems.hud.DoubleTextHudElement;
import meteordevelopment.meteorclient.systems.hud.HUD;

public class WatermarkHud extends DoubleTextHudElement {
    public WatermarkHud(HUD hud) {
        super(hud, "watermark", "Displays a Lemon Client watermark.", "LemonClient ");
    }

    @Override
    protected String getRight() {
        if (LemonClient.DEV_BUILD.isEmpty()) {
            return LemonClient.VERSION.toString();
        }

        return LemonClient.VERSION + " " + LemonClient.DEV_BUILD;
    }
}
