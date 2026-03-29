package org.alku.catalyst.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.alku.catalyst.config.CatalystConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class FastShieldAnimationMixin {

    @Shadow public abstract boolean isUsingItem();
    @Shadow public abstract ItemStack getUseItem();

    @Inject(method = "getUseItemRemainingTicks", at = @At("RETURN"), cancellable = true)
    private void onGetUseItemRemainingTicks(CallbackInfoReturnable<Integer> cir) {
        if (!CatalystConfig.getInstance().fastShieldEnabled) {
            return;
        }
        if (!this.isUsingItem()) {
            return;
        }
        ItemStack useItem = this.getUseItem();
        if (useItem.isEmpty()) {
            return;
        }
        if (useItem.getUseAnimation() != UseAnim.BLOCK) {
            return;
        }
        cir.setReturnValue(Math.max(0, cir.getReturnValue() - 10));
    }
}
