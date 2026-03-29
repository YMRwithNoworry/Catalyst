package org.alku.catalyst.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.alku.catalyst.config.CatalystConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class FastShieldMixin {

    @Shadow public abstract boolean isUsingItem();
    @Shadow public abstract ItemStack getUseItem();

    @Inject(method = "isBlocking", at = @At("HEAD"), cancellable = true)
    private void onIsBlocking(CallbackInfoReturnable<Boolean> cir) {
        if (CatalystConfig.getInstance().fastShieldEnabled) {
            if (this.isUsingItem() && !this.getUseItem().isEmpty()) {
                Item item = this.getUseItem().getItem();
                if (item.getUseAnimation(this.getUseItem()) == UseAnim.BLOCK) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
