package meteordevelopment.meteorclient.gui.themes.lemon.widgets;

import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.themes.lemon.LemonWidget;
import meteordevelopment.meteorclient.gui.widgets.WAccount;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WLemonAccount extends WAccount implements LemonWidget {
    public WLemonAccount(WidgetScreen screen, Account<?> account) {
        super(screen, account);
    }

    @Override
    protected Color loggedInColor() {
        return theme().loggedInColor.get();
    }

    @Override
    protected Color accountTypeColor() {
        return theme().textSecondaryColor.get();
    }
}
