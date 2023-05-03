package meteordevelopment.meteorclient.systems.modules.world;

import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

import static meteordevelopment.meteorclient.utils.world.BlockInfo.*;
import static meteordevelopment.meteorclient.utils.entity.EntityInfo.*;

public class KillFx extends Module {
    public KillFx() {
        super(Categories.World, "kill-fx", "");
    }

    private boolean lightningOnceFlag = false;

    public void onKill(PlayerEntity player) {
        if (isActive()) {
            spawnLightning(player);
        }
    }

    private void spawnLightning(PlayerEntity player) {
        BlockPos blockPos = getBlockPos(player);

        double x = X(blockPos);
        double y = Y(blockPos);
        double z = Z(blockPos);

        LightningEntity lightningEntity = new LightningEntity(EntityType.LIGHTNING_BOLT, mc.world);

        lightningEntity.updatePosition(x, y, z);
        lightningEntity.refreshPositionAfterTeleport(x, y, z);

        mc.world.addEntity(lightningEntity.getId(), lightningEntity);

        if (!lightningOnceFlag) {
            mc.world.playSound(mc.player, x, y, z, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 10000.0F, 0.8F * 0.2F);
            mc.world.playSound(mc.player, x, y, z, SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.WEATHER, 2.0F, 0.5F * 0.2F);
            lightningOnceFlag = true;
        }
    }
}
