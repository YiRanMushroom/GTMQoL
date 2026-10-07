package com.yiran.minecraft.gtmqol.core.mixins.ae2;

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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;

/**
 * Sticky card: once a sticky storage bus that has this key partitioned has been offered it (accepted or not), the
 * remaining storages (the other priorities and the second pass of this one) are skipped. Ported from the v7 mixin.
 */
@Mixin(value = NetworkStorage.class, remap = false)
public class NetworkStorageMixin {

    @Inject(method = "insert", at = @At("HEAD"))
    private void gtmqol$resetStop(AEKey what, long amount, Actionable type, IActionSource src,
                                  CallbackInfoReturnable<Long> cir,
                                  @Share("stop") LocalBooleanRef stop) {
        stop.set(false);
    }

    @Definition(id = "priorityInventory",
                field = "Lappeng/me/storage/NetworkStorage;priorityInventory:Ljava/util/NavigableMap;")
    @Definition(id = "values", method = "Ljava/util/NavigableMap;values()Ljava/util/Collection;")
    @Definition(id = "iterator", method = "Ljava/util/Collection;iterator()Ljava/util/Iterator;")
    @Expression("this.priorityInventory.values().iterator()")
    @ModifyExpressionValue(method = "insert", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Iterator<List<MEStorage>> gtmqol$stopOuter(Iterator<List<MEStorage>> original,
                                                       @Share("stop") LocalBooleanRef stop) {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return !stop.get() && original.hasNext();
            }

            @Override
            public List<MEStorage> next() {
                return original.next();
            }
        };
    }

    @Definition(id = "secondPassInventories",
                field = "Lappeng/me/storage/NetworkStorage;secondPassInventories:Ljava/util/List;")
    @Definition(id = "iterator", method = "Ljava/util/List;iterator()Ljava/util/Iterator;")
    @Expression("this.secondPassInventories.iterator()")
    @ModifyExpressionValue(method = "insert", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Iterator<MEStorage> gtmqol$stopInner(Iterator<MEStorage> original,
                                                 @Share("stop") LocalBooleanRef stop) {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return !stop.get() && original.hasNext();
            }

            @Override
            public MEStorage next() {
                return original.next();
            }
        };
    }

    // First pass: the storages that prefer this key.
    @WrapOperation(method = "insert",
                   at = @At(value = "INVOKE",
                            target = "Lappeng/api/storage/MEStorage;insert(Lappeng/api/stacks/AEKey;JLappeng/api/config/Actionable;Lappeng/api/networking/security/IActionSource;)J",
                            ordinal = 0))
    private long gtmqol$stickFirstPass(MEStorage instance, AEKey what, long amount, Actionable mode,
                                       IActionSource source, Operation<Long> original,
                                       @Share("stop") LocalBooleanRef stop) {
        return gtmqol$insertAndCheck(instance, what, amount, mode, source, original, stop);
    }

    // Second pass: everything else.
    @WrapOperation(method = "insert",
                   at = @At(value = "INVOKE",
                            target = "Lappeng/api/storage/MEStorage;insert(Lappeng/api/stacks/AEKey;JLappeng/api/config/Actionable;Lappeng/api/networking/security/IActionSource;)J",
                            ordinal = 1))
    private long gtmqol$stickSecondPass(MEStorage instance, AEKey what, long amount, Actionable mode,
                                        IActionSource source, Operation<Long> original,
                                        @Share("stop") LocalBooleanRef stop) {
        return gtmqol$insertAndCheck(instance, what, amount, mode, source, original, stop);
    }

    private static long gtmqol$insertAndCheck(MEStorage instance, AEKey what, long amount, Actionable mode,
                                              IActionSource source, Operation<Long> original,
                                              LocalBooleanRef stop) {
        long inserted = original.call(instance, what, amount, mode, source);
        if (!stop.get() && instance instanceof ISticky sticky && sticky.isSticky()) {
            stop.set(sticky.shouldStick(what, source));
        }
        return inserted;
    }
}
