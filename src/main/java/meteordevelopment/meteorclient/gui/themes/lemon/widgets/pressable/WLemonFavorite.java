package meteordevelopment.meteorclient.gui.themes.lemon.widgets.pressable;

import meteordevelopment.meteorclient.gui.themes.lemon.LemonWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WFavorite;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WLemonFavorite extends WFavorite implements LemonWidget {
    public WLemonFavorite(boolean checked) {
        super(checked);
    }

    @Override
    protected Color getColor() {
        return theme().favoriteColor.get();
    }
}
