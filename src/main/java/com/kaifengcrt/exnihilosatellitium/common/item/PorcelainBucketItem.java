package com.kaifengcrt.exnihilosatellitium.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
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
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Porcelain bucket (瓷桶, M6): one universal 1000 mB container that accepts any
 * fluid, matching Ex Nihilo Omnia's porcelain bucket rather than a barrel block.
 *
 * <p>Contents use Forge's standard item-tank NBT ({@code Fluid: {FluidName,
 * Amount}}), provided by {@link FluidHandlerItemStack}, so barrels, crucibles
 * and pipes can fill or drain the bucket through the normal
 * {@code ForgeCapabilities.FLUID_HANDLER_ITEM}. Right-clicking a fluid source
 * scoops 1000 mB while the bucket is empty.</p>
 */
public class PorcelainBucketItem extends Item {

    public static final int CAPACITY = 1000;

    public PorcelainBucketItem(final Properties properties) {
        super(properties);
    }

    /** Stored payload, or {@link FluidStack#EMPTY}. */
    public static FluidStack fluidOf(final ItemStack stack) {
        final CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(FluidHandlerItemStack.FLUID_NBT_KEY)) {
            return FluidStack.EMPTY;
        }
        final FluidStack fluid = FluidStack.loadFluidStackFromNBT(
                tag.getCompound(FluidHandlerItemStack.FLUID_NBT_KEY));
        return fluid == null ? FluidStack.EMPTY : fluid;
    }

    /** Writes the contents in the same NBT layout the capability reads. */
    public static void setContents(final ItemStack stack, final FluidStack fluid) {
        final FluidHandlerItemStack handler = new FluidHandlerItemStack(stack, CAPACITY);
        handler.drain(CAPACITY, IFluidHandler.FluidAction.EXECUTE);
        if (!fluid.isEmpty()) {
            handler.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(final ItemStack stack, @Nullable final CompoundTag nbt) {
        return new FluidHandlerItemStack(stack, CAPACITY);
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
    public void appendHoverText(final ItemStack stack, @Nullable final Level level,
                                final List<Component> tooltip, final TooltipFlag flag) {
        final FluidStack fluid = fluidOf(stack);
        if (!fluid.isEmpty()) {
            tooltip.add(Component.translatable("item.exnihilosatellitium.porcelain_bucket.contents",
                    fluid.getDisplayName(), fluid.getAmount()).withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("item.exnihilosatellitium.porcelain_bucket.hint")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
