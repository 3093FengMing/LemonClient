package meteordevelopment.meteorclient.utils.render;

import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class HoleUtils {
    public static MinecraftClient mc;
    public static Screen screen;

    public static void init() {
        mc = MinecraftClient.getInstance();

        screen = null;
    }

    public static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dX = x2 - x1;
        double dY = y2 - y1;
        double dZ = z2 - z1;

        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    public static double distance(Vec3d vec1, Vec3d vec2) {
        double dX = vec2.x - vec1.x;
        double dY = vec2.y - vec1.y;
        double dZ = vec2.z - vec1.z;

        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    public static double distance(BlockPos pos1, BlockPos pos2) {
        double dX = pos2.getX() - pos1.getX();
        double dY = pos2.getY() - pos1.getY();
        double dZ = pos2.getZ() - pos1.getZ();

        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    public static double distanceXZ(Vec3d pos1, Vec3d pos2) {
        double dX = pos1.getX() - pos2.getX();
        double dZ = pos1.getZ() - pos2.getZ();

        return MathHelper.sqrt((float) (dX * dX + dZ * dZ));
    }

    public static double distanceY(double y1, double y2) {
        return Math.abs(y1 - y2);
    }

    public static boolean canPlace(BlockPos pos, boolean checkEntities) {
        return canPlace(pos, Blocks.OBSIDIAN.getDefaultState(), checkEntities);
    }

    public static boolean canPlace(BlockPos pos, BlockState state, boolean checkEntities) {
        if (pos == null || mc.world == null) return false;
        if (!World.isValid(pos)) return false;
        if (!mc.world.getBlockState(pos).getMaterial().isReplaceable()) return false;

        return !checkEntities || mc.world.canPlace(state, pos, ShapeContext.absent());
    }
}
