package meteordevelopment.meteorclient.gui.themes.lemon.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.themes.lemon.LemonGuiTheme;
import meteordevelopment.meteorclient.gui.themes.lemon.LemonWidget;
import meteordevelopment.meteorclient.gui.widgets.WVerticalSeparator;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WLemonVerticalSeparator extends WVerticalSeparator implements LemonWidget {
    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        LemonGuiTheme theme = theme();
        Color colorEdges = theme.separatorEdges.get();
        Color colorCenter = theme.separatorCenter.get();

        double s = theme.scale(1);
        double offsetX = Math.round(width / 2.0);

        renderer.quad(x + offsetX, y, s, height / 2, colorEdges, colorEdges, colorCenter, colorCenter);
        renderer.quad(x + offsetX, y + height / 2, s, height / 2, colorCenter, colorCenter, colorEdges, colorEdges);
    }
}
