package meteordevelopment.meteorclient.gui.themes.lemon.widgets.pressable;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.lemon.LemonGuiTheme;
import meteordevelopment.meteorclient.gui.themes.lemon.LemonWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPlus;

public class WLemonPlus extends WPlus implements LemonWidget {
    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        LemonGuiTheme theme = theme();
        double pad = pad();
        double s = theme.scale(3);

        renderBackground(renderer, this, pressed, mouseOver);
        renderer.quad(x + pad, y + height / 2 - s / 2, width - pad * 2, s, theme.plusColor.get());
        renderer.quad(x + width / 2 - s / 2, y + pad, s, height - pad * 2, theme.plusColor.get());
    }
}
