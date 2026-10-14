package com.yiran.minecraft.gtmqol.mixin.applied_energistics;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.me.storage.NetworkStorage;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.yiran.minecraft.gtmqol.integration.ae2.ISticky;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;

@Restriction(
        require = @Condition(type = Condition.Type.MOD, value = "ae2")
)
@Mixin(NetworkStorage.class)
public class NetworkStorage$HandleSticky$Mixin {
    @Definition(id = "priorityInventory", field = "Lappeng/me/storage/NetworkStorage;priorityInventory:Ljava/util/NavigableMap;")
    @Definition(id = "values", method = "Ljava/util/NavigableMap;values()Ljava/util/Collection;")
    @Definition(id = "iterator", method = "Ljava/util/Collection;iterator()Ljava/util/Iterator;")
    @Expression("this.priorityInventory.values().iterator()")
    @ModifyExpressionValue(method = "insert", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Iterator<List<MEStorage>> modifyOuterIterator(Iterator<List<MEStorage>> original, @Share("shouldStopMatching") LocalBooleanRef shouldStopMatching) {

        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return !shouldStopMatching.get() && original.hasNext();
            }

            @Override
            public List<MEStorage> next() {
                return original.next();
            }
        };
    }

    @Definition(id = "secondPassInventories", field = "Lappeng/me/storage/NetworkStorage;secondPassInventories:Ljava/util/List;")
    @Definition(id = "iterator", method = "Ljava/util/List;iterator()Ljava/util/Iterator;")
    @Expression("this.secondPassInventories.iterator()")
    @ModifyExpressionValue(method = "insert", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Iterator<MEStorage> modifyInnerIterator(Iterator<MEStorage> original, @Share("shouldStopMatching") LocalBooleanRef shouldStopMatching) {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return !shouldStopMatching.get() && original.hasNext();
            }

            @Override
            public MEStorage next() {
                return original.next();
            }
        };
    }

    @Inject(method = "insert", at = @At("HEAD"))
    private void resetShouldStopMatching(AEKey what, long amount, Actionable type, IActionSource src, CallbackInfoReturnable<Long> cir, @Share("shouldStopMatching") LocalBooleanRef shouldStopMatching) {
        shouldStopMatching.set(false);
    }

    @WrapOperation(method = "insert", at = @At(value = "INVOKE", target = "Lappeng/api/storage/MEStorage;insert(Lappeng/api/stacks/AEKey;JLappeng/api/config/Actionable;Lappeng/api/networking/security/IActionSource;)J", ordinal = 1))
    private long checkStickySecond(MEStorage instance, AEKey what, long amount, Actionable mode, IActionSource source, Operation<Long> original, @Share("shouldStopMatching") LocalBooleanRef shouldStopMatching) {
        var returnValue = original.call(instance, what, amount, mode, source);

        if (shouldStopMatching.get()) {
            return returnValue;
        }

        if (instance instanceof ISticky stickyHandler && stickyHandler.isSticky()) {
            shouldStopMatching.set(stickyHandler.shouldStick(what, source));
        }
        return returnValue;
    }

    @WrapOperation(method = "insert", at = @At(value = "INVOKE", target = "Lappeng/api/storage/MEStorage;insert(Lappeng/api/stacks/AEKey;JLappeng/api/config/Actionable;Lappeng/api/networking/security/IActionSource;)J", ordinal = 0))
    private long checkStickyFirst(MEStorage instance, AEKey what, long amount, Actionable mode, IActionSource source, Operation<Long> original, @Share("shouldStopMatching") LocalBooleanRef shouldStopMatching) {
        var returnValue = original.call(instance, what, amount, mode, source);

        if (shouldStopMatching.get()) {
            return returnValue;
        }

        if (instance instanceof ISticky stickyHandler && stickyHandler.isSticky()) {
            shouldStopMatching.set(stickyHandler.shouldStick(what, source));
        }
        return returnValue;
    }
}
