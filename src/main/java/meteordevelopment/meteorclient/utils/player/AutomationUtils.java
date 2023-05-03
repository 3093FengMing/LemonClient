package meteordevelopment.meteorclient.utils.player;

import meteordevelopment.meteorclient.utils.entity.EntityInfo;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

import static meteordevelopment.meteorclient.LemonClient.mc;

public class AutomationUtils {

    public static ArrayList<Vec3d> surroundPositions = new ArrayList<Vec3d>() {{
        add(new Vec3d(1, 0, 0));
        add(new Vec3d(-1, 0, 0));
        add(new Vec3d(0, 0, 1));
        add(new Vec3d(0, 0, -1));
    }};


    public static boolean isAnvilBlock(BlockPos pos) {
        return BlockUtils.getBlock(pos) == Blocks.ANVIL || BlockUtils.getBlock(pos) == Blocks.CHIPPED_ANVIL || BlockUtils.getBlock(pos) == Blocks.DAMAGED_ANVIL;
    }

    public static boolean isWeb(BlockPos pos) {
        return BlockUtils.getBlock(pos) == Blocks.COBWEB || BlockUtils.getBlock(pos) == Block.getBlockFromItem(Items.STRING);
    }

    public static boolean isBurrowed(PlayerEntity p, boolean holeCheck) {
        BlockPos pos = p.getBlockPos();
        if (holeCheck && !EntityInfo.isInHole(p)) return false;
        return BlockUtils.getBlock(pos) == Blocks.ENDER_CHEST || BlockUtils.getBlock(pos) == Blocks.OBSIDIAN || isAnvilBlock(pos);
    }

    public static boolean isWebbed(PlayerEntity p) {
        BlockPos pos = p.getBlockPos();
        if (isWeb(pos)) return true;
        return isWeb(pos.up());
    }

    public static boolean isTrapBlock(BlockPos pos) {
        return BlockUtils.getBlock(pos) == Blocks.OBSIDIAN || BlockUtils.getBlock(pos) == Blocks.ENDER_CHEST;
    }

    public static boolean isSurroundBlock(BlockPos pos) {
        //some apes use anchors as surround blocks cus it has the same blast resistance as obsidian
        return BlockUtils.getBlock(pos) == Blocks.OBSIDIAN || BlockUtils.getBlock(pos) == Blocks.ENDER_CHEST || BlockUtils.getBlock(pos) == Blocks.RESPAWN_ANCHOR;
    }

    public static boolean canCrystal(PlayerEntity p) {
        BlockPos tpos = p.getBlockPos();
        for (Vec3d sp : surroundPositions) {
            BlockPos sb = tpos.add(sp.x, sp.y, sp.z);
            if (BlockUtils.getBlock(sb) == Blocks.AIR) return true;
        }
        return false;
    }

    public static void mineWeb(PlayerEntity p, int swordSlot) {
        if (p == null || swordSlot == -1) return;
        BlockPos pos = p.getBlockPos();
        BlockPos webPos = null;
        if (isWeb(pos)) webPos = pos;
        if (isWeb(pos.up())) webPos = pos.up();
        if (isWeb(pos.up(2))) webPos = pos.up(2);
        if (webPos == null) return;
        InvUtils.updateSlot(swordSlot);
        doRegularMine(webPos);
    }

    public static void doPacketMine(BlockPos targetPos) {
        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, targetPos, Direction.UP));
        Interaction.swingHand(false);
        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, targetPos, Direction.UP));
    }

    public static void doRegularMine(BlockPos targetPos) {
        mc.interactionManager.updateBlockBreakingProgress(targetPos, Direction.UP);
        Vec3d hitPos = new Vec3d(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
        Rotations.rotate(Rotations.getYaw(hitPos), Rotations.getPitch(hitPos), 50, () -> Interaction.swingHand(false));
    }
}
