package meteordevelopment.meteorclient.systems.modules;

import net.minecraft.item.Items;

public class Categories {
    public static final Category Combat = new Category("Combat", Items.GOLDEN_SWORD.getDefaultStack());
    public static final Category Player = new Category("Player", Items.ARMOR_STAND.getDefaultStack());
    public static final Category Movement = new Category("Movement", Items.DIAMOND_BOOTS.getDefaultStack());
    public static final Category Render = new Category("Render", Items.GLASS.getDefaultStack());
    public static final Category World = new Category("World", Items.GRASS_BLOCK.getDefaultStack());
    public static final Category Hig = new Category("HIG Tools", Items.NETHERITE_PICKAXE.getDefaultStack());
    public static final Category Borers = new Category("Borers", Items.NETHERITE_PICKAXE.getDefaultStack());
    public static final Category Misc = new Category("Misc", Items.LAVA_BUCKET.getDefaultStack());
    public static final Category Chat = new Category("Chat", Items.PLAYER_HEAD.getDefaultStack());
    public static final Category Settings = new Category("Settings", Items.COMPASS.getDefaultStack());

    public static boolean REGISTERING;

    public static void init() {
        REGISTERING = true;

        // Categories
        Modules.registerCategory(Combat);
        Modules.registerCategory(Player);
        Modules.registerCategory(Movement);
        Modules.registerCategory(Render);
        Modules.registerCategory(World);
        Modules.registerCategory(Hig);
        Modules.registerCategory(Borers);
        Modules.registerCategory(Misc);
        Modules.registerCategory(Chat);
        Modules.registerCategory(Settings);

        REGISTERING = false;
    }
}
