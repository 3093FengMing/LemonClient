package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.AbstractBlockAccessor;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.BEntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

public class WebHoleFill extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // General
    private final Setting<Integer> horizontalRadius = sgGeneral.add(new IntSetting.Builder()
        .name("horizontal-radius")
        .description("Horizontal radius in which to search for holes.")
        .defaultValue(4)
        .min(0)
        .sliderMax(5)
        .build()
    );

    private final Setting<Integer> verticalRadius = sgGeneral.add(new IntSetting.Builder()
        .name("vertical-radius")
        .description("Vertical radius in which to search for holes.")
        .defaultValue(3)
        .min(0)
        .sliderMax(5)
        .build()
    );

    private final Setting<Double> fillRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("fill-range")
        .description("Range from target to hole for it to fill.")
        .defaultValue(1.5)
        .min(0)
        .sliderMin(0.5)
        .sliderMax(3)
        .build()
    );

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("target-range")
        .description("target-range")
        .defaultValue(7)
        .min(0)
        .sliderMin(1)
        .sliderMax(10)
        .build()
    );

    private final Setting<Double> rangePlace = sgGeneral.add(new DoubleSetting.Builder()
        .name("place-range")
        .description("place-range")
        .defaultValue(5)
        .min(0)
        .sliderMin(1)
        .sliderMax(6)
        .build()
    );

    private final Setting<SortPriority> priority = sgGeneral.add(new EnumSetting.Builder<SortPriority>()
        .name("target-priority")
        .description("How to select the player to target.")
        .defaultValue(SortPriority.LowestDistance)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate").description("Whether to rotate or not.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> onlyInHole = sgGeneral.add(new BoolSetting.Builder()
        .name("only-in-hole").description("will only fill hole when u are in a hole")
        .defaultValue(false)
        .build()
    );

    public WebHoleFill() {
        super(Categories.Combat, "web-hole-fill", "Prevents players from going into holes");
    }


    private final Pool<Hole> holePool = new Pool<>(Hole::new);
    private final List<Hole> holes = new ArrayList<>();

    private final BlockPos.Mutable renderPos = new BlockPos.Mutable();

    private final byte NULL = 0;

    private PlayerEntity target;


    @Override
    public void onActivate() {
        target = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        target = TargetUtils.getPlayerTarget(range.get(), priority.get());

        if ((onlyInHole.get() && (BEntityUtils.isSurrounded(mc.player, BEntityUtils.BlastResistantType.Any) || (BEntityUtils.isInHole(mc.player, true, BEntityUtils.BlastResistantType.Any)))) || (!onlyInHole.get())){
            for (Hole hole : holes) {

                if (target != null){


                    if (Math.sqrt(target.getPos().squaredDistanceTo(hole.blockPos.getX() + 0.5, hole.blockPos.getY() + 0.5, hole.blockPos.getZ() + 0.5)) <= fillRange.get()){

                        if(!BlockUtils.place(hole.blockPos, InvUtils.findInHotbar(Items.COBWEB), rotate.get(), 50,true)) {

                            return;
                        }
                    }

                }
            }
        }


        for (Hole hole : holes) holePool.free(hole);
        holes.clear();

        BlockIterator.register(horizontalRadius.get(), verticalRadius.get(), (blockPos, blockState) -> {
            if (!validHole(blockPos)) return;

            int blocks = 0;

            for (Direction direction : Direction.values()) {
                if (direction == Direction.UP) continue;

                BlockState state = mc.world.getBlockState(blockPos.offset(direction));

                if (state.getBlock() != Blocks.AIR) blocks++;
                else if (direction == Direction.DOWN) return;
            }

            if (blocks == 5) {
                holes.add(holePool.get().set(blockPos, NULL));
            }
        });
    }

    private boolean validHole(BlockPos pos) {
        //check for if player is in the hole
        if ((mc.player.getBlockPos().equals(pos))) return false;

        //check for if the target is in the hole
        if (target != null){
            if ((target.getBlockPos().equals(pos))) return false;
        }

        if (mc.world.getBlockState(pos).getBlock() == Blocks.COBWEB) return false;

        //range check
        if (Math.sqrt(mc.player.getPos().squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) >= rangePlace.get()) return false;

        return !((AbstractBlockAccessor) mc.world.getBlockState(pos).getBlock()).isCollidable();
    }

    private static class Hole {
        public BlockPos.Mutable blockPos = new BlockPos.Mutable();
        public byte exclude;

        public Hole set(BlockPos blockPos, byte exclude) {
            this.blockPos.set(blockPos);
            this.exclude = exclude;

            return this;
        }
    }
}
