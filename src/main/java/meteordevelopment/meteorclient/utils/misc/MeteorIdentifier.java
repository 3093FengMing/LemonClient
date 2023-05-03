package meteordevelopment.meteorclient.utils.misc;

import meteordevelopment.meteorclient.LemonClient;
import net.minecraft.util.Identifier;

public class MeteorIdentifier extends Identifier {
    public MeteorIdentifier(String path) {
        super(LemonClient.MOD_ID, path);
    }
}
