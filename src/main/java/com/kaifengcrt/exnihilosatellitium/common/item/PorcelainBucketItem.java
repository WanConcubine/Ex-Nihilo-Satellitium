package com.kaifengcrt.exnihilosatellitium.common.item;

import com.kaifengcrt.exnihilosatellitium.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

/**
 * Porcelain bucket (瓷桶, M6): one universal 1000 mB container that accepts any
 * fluid, matching Ex Nihilo Omnia's porcelain bucket rather than a barrel block.
 *
 * <p>The contents live in the {@code exnihilosatellitium:fluid_contents} data
 * component and the block capability ({@link PorcelainBucketFluidHandler}) lets
 * buckets, barrels and pipes fill or drain it. Right-clicking a fluid source
 * scoops 1000 mB while the bucket is empty.</p>
 */
public class PorcelainBucketItem extends Item {

    public static final int CAPACITY = 1000;

    public PorcelainBucketItem(final Properties properties) {
        super(properties);
    }

    /** Stored payload, or null when empty. */
    public static ModDataComponents.FluidContents contents(final ItemStack stack) {
        return stack.get(ModDataComponents.FLUID_CONTENTS.get());
    }

    public static FluidStack fluidOf(final ItemStack stack) {
        final ModDataComponents.FluidContents contents = contents(stack);
        if (contents == null || contents.isEmpty()) {
            return FluidStack.EMPTY;
        }
        final Fluid fluid = BuiltInRegistries.FLUID.getOptional(contents.fluid()).orElse(null);
        return fluid == null || fluid == Fluids.EMPTY
                ? FluidStack.EMPTY
                : new FluidStack(fluid, contents.amount());
    }

    public static void setContents(final ItemStack stack, final FluidStack fluid) {
        if (fluid.isEmpty()) {
            stack.remove(ModDataComponents.FLUID_CONTENTS.get());
        } else {
            stack.set(ModDataComponents.FLUID_CONTENTS.get(),
                    new ModDataComponents.FluidContents(
                            BuiltInRegistries.FLUID.getKey(fluid.getFluid()), fluid.getAmount()));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player,
                                                  final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (!fluidOf(stack).isEmpty()) {
            return InteractionResultHolder.pass(stack);
        }
        final BlockHitResult hit = BucketItem.getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }
        final BlockPos pos = hit.getBlockPos();
        if (!level.mayInteract(player, pos)) {
            return InteractionResultHolder.pass(stack);
        }
        final FluidState state = level.getFluidState(pos);
        if (state.isEmpty()) {
            return InteractionResultHolder.pass(stack);
        }
        final Fluid fluid = state.getType();
        if (fluid == Fluids.EMPTY) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide) {
            setContents(stack, new FluidStack(fluid, CAPACITY));
            level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(final ItemStack stack, final TooltipContext context,
                                final List<Component> tooltip, final TooltipFlag flag) {
        final FluidStack fluid = fluidOf(stack);
        if (!fluid.isEmpty()) {
            tooltip.add(Component.translatable("item.exnihilosatellitium.porcelain_bucket.contents",
                    fluid.getHoverName(), fluid.getAmount()).withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("item.exnihilosatellitium.porcelain_bucket.hint")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
