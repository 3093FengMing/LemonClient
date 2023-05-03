package meteordevelopment.meteorclient.utils.player;

import net.minecraft.item.PickaxeItem;

public class ItemUtils {
    public static FindItemResult findPick() {
        return InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof PickaxeItem);
    }
}
