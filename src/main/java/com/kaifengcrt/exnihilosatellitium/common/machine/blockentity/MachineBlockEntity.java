package com.kaifengcrt.exnihilosatellitium.common.machine.blockentity;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.config.ModConfig;
import com.kaifengcrt.exnihilosatellitium.common.item.HammerItem;
import com.kaifengcrt.exnihilosatellitium.common.item.MeshItem;
import com.kaifengcrt.exnihilosatellitium.common.machine.HammerRolls;
import com.kaifengcrt.exnihilosatellitium.common.machine.MachineKind;
import com.kaifengcrt.exnihilosatellitium.common.machine.SieveRolls;
import com.kaifengcrt.exnihilosatellitium.common.machine.UpgradeItem;
import com.kaifengcrt.exnihilosatellitium.common.machine.UpgradeType;
import com.kaifengcrt.exnihilosatellitium.common.machine.block.MachineBlock;
import com.kaifengcrt.exnihilosatellitium.common.machine.menu.ElectricMachineMenu;
import com.kaifengcrt.exnihilosatellitium.common.machine.menu.FuelMachineMenu;
import com.kaifengcrt.exnihilosatellitium.common.sieve.SieveRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Shared block entity for the four automation machines.
 *
 * <p>Electric machines (sieve / hammer) accept FE, four upgrade slots and have
 * to be fed by pipes or a player. Fuel machines burn vanilla furnace fuel and
 * deliberately have no upgrade slots — the cheaper entry tier.</p>
 *
 * <p>Every operation calls {@link SieveRolls} / {@link HammerRolls}, the same
 * functions the manual sieve and hammer use.</p>
 */
public class MachineBlockEntity extends BlockEntity implements MenuProvider, ContainerData {

    /** GUI data indices (also used by the screens). */
    public static final int DATA_WORK = 0;
    public static final int DATA_TICKS = 1;
    public static final int DATA_ENERGY = 2;
    public static final int DATA_ENERGY_MAX = 3;
    public static final int DATA_BURN = 4;
    public static final int DATA_BURN_MAX = 5;
    public static final int DATA_ACTIVE = 6;
    public static final int DATA_STATUS = 7;
    public static final int DATA_COUNT = 8;

    public static final int TOOL_SLOT = 0;
    public static final int INPUT_SLOT = 1;
    public static final int FUEL_SLOT = 2;
    public static final int UPGRADE_SLOT_START = 2;
    public static final int UPGRADE_SLOTS = 4;
    public static final int OUTPUT_SLOTS = 12;

    private static final int ELECTRIC_OUTPUT_START = UPGRADE_SLOT_START + UPGRADE_SLOTS;
    private static final int FUEL_OUTPUT_START = FUEL_SLOT + 1;
    private static final double MAX_STACK = 64.0D;

    private final boolean electric;
    private final MachineInventory inventory;
    private final MachineEnergy energy;

    private int work;
    private int burnHalfTicks;
    private int burnHalfTotal;
    private boolean active;

    protected MachineBlockEntity(final BlockEntityType<?> type, final BlockPos pos,
                                 final BlockState state, final boolean electric) {
        super(type, pos, state);
        this.electric = electric;
        this.inventory = new MachineInventory(this, electric ? 18 : 15);
        this.energy = electric ? new MachineEnergy(ModConfig.machineEnergyCapacity()) : null;
    }

    // ------------------------------------------------------------------ state

    public boolean isElectric() {
        return this.electric;
    }

    public MachineKind kind() {
        return this.getBlockState().getBlock() instanceof MachineBlock machine
                ? machine.kind() : MachineKind.SIEVE;
    }

    public MachineInventory getInventory() {
        return this.inventory;
    }

    /** {@code null} on fuel machines. */
    public MachineEnergy getEnergy() {
        return this.energy;
    }

    public int getWork() {
        return this.work;
    }

    public int getBurnHalfTicks() {
        return this.burnHalfTicks;
    }

    public int getBurnHalfTotal() {
        return this.burnHalfTotal;
    }

    public int ticksPerOperation() {
        final double multiplier = this.electric ? this.speedMultiplier() : 1.0D;
        return Math.max(1, (int) Math.round(ModConfig.machineTicksPerOperation() / multiplier));
    }

    /** All installed upgrades of one type, best tier first. */
    private java.util.List<UpgradeItem> upgradesOf(final UpgradeType type) {
        final java.util.List<UpgradeItem> list = new java.util.ArrayList<>();
        if (!this.electric) {
            return list;
        }
        for (int i = 0; i < UPGRADE_SLOTS; i++) {
            if (this.inventory.getItem(UPGRADE_SLOT_START + i).getItem()
                    instanceof UpgradeItem upgrade && upgrade.type() == type) {
                list.add(upgrade);
            }
        }
        list.sort(java.util.Comparator.comparingInt(UpgradeItem::tier).reversed());
        return list;
    }

    /** Number of installed upgrade items (0..4); drives the FE surcharge. */
    public int upgradeCount() {
        int count = 0;
        if (this.electric) {
            for (int i = 0; i < UPGRADE_SLOTS; i++) {
                if (this.inventory.getItem(UPGRADE_SLOT_START + i).getItem() instanceof UpgradeItem) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Speed multipliers stack with diminishing returns: the best upgrade counts
     * fully, every further one adds 80% of the previous bonus.
     */
    public double speedMultiplier() {
        double multiplier = 1.0D;
        double weight = 1.0D;
        for (final UpgradeItem upgrade : this.upgradesOf(UpgradeType.SPEED)) {
            multiplier += (UpgradeItem.speedMultiplier(upgrade.tier()) - 1.0D) * weight;
            weight *= 0.8D;
        }
        return multiplier;
    }

    /**
     * Expected extra rolls. Fortune stacks diminishing: tier I/II/III adds 1/2/3,
     * each further upgrade adds half of the previous value.
     */
    public double fortuneBonus() {
        double bonus = 0.0D;
        double weight = 1.0D;
        for (final UpgradeItem upgrade : this.upgradesOf(UpgradeType.FORTUNE)) {
            bonus += upgrade.tier() * weight;
            weight *= 0.8D;
        }
        return bonus;
    }

    /** Whole extra rolls plus one probabilistic roll for the fractional part. */
    private int fortuneExtraRolls() {
        final double bonus = this.fortuneBonus();
        int rolls = (int) Math.floor(bonus);
        if (this.level != null && this.level.random.nextDouble() < bonus - rolls) {
            rolls++;
        }
        return rolls;
    }

    public int energyPerTick() {
        final int base = ModConfig.machineEnergyPerTick();
        // Each installed upgrade adds 25% draw on top of the base.
        return (int) Math.round(base * (1.0D + 0.25D * Math.max(0, this.upgradeCount() - 1)));
    }

    public int burnHalfPerOperation() {
        return ModConfig.machineBurnPerOperation() * 2;
    }

    private int burnHalfPerTick() {
        return Math.max(1, this.burnHalfPerOperation() / this.ticksPerOperation());
    }



    public int outputSlotStart() {
        return this.electric ? ELECTRIC_OUTPUT_START : FUEL_OUTPUT_START;
    }

    public int outputSlotEnd() {
        return this.outputSlotStart() + OUTPUT_SLOTS;
    }

    // ---------------------------------------------------------------- ticking

    public void serverTick() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        this.relocateInvalidSlotItems();
        if (!this.canWork()) {
            if (this.work != 0) {
                this.work = 0;
                this.setChanged();
            }
            if (this.active) {
                this.active = false;
                this.setChanged();
            }
            return;
        }
        if (!this.consumePowerPerTick()) {
            if (this.work != 0) {
                this.work = 0;
                this.setChanged();
            }
            if (this.active) {
                this.active = false;
                this.setChanged();
            }
            return;
        }
        this.active = true;
        this.work++;
        if (this.work >= this.ticksPerOperation()) {
            this.work = 0;
            this.completeOperation();
        }
        this.setChanged();
    }

    private boolean canWork() {
        if (!this.hasTool() || !this.hasOutputSpace()) {
            return false;
        }
        final ItemStack input = this.inventory.getItem(INPUT_SLOT);
        return !input.isEmpty() && this.isValidInput(input);
    }

    private boolean consumePowerPerTick() {
        if (this.electric) {
            final int cost = this.energyPerTick();
            if (cost <= 0) {
                return true;
            }
            return this.energy.drain(cost);
        }
        if (this.burnHalfTicks <= 0 && !this.loadFuel()) {
            return false;
        }
        final int drain = this.burnHalfPerTick();
        if (this.burnHalfTicks < drain) {
            return false;
        }
        this.burnHalfTicks -= drain;
        return true;
    }

    /** Loads one fuel item when the current burn ran out. */
    private boolean loadFuel() {
        final ItemStack fuel = this.inventory.getItem(FUEL_SLOT);
        final int burnTicks = fuel.getBurnTime(RecipeType.SMELTING);
        if (burnTicks <= 0) {
            return false;
        }
        final ItemStack remainder = fuel.getCraftingRemainingItem();
        fuel.shrink(1);
        this.inventory.setItem(FUEL_SLOT, fuel.isEmpty() ? ItemStack.EMPTY : fuel);
        if (!remainder.isEmpty()) {
            this.inventory.addItem(remainder);
            if (!remainder.isEmpty() && this.level != null) {
                Block.popResource(this.level, this.worldPosition, remainder);
            }
        }
        this.burnHalfTicks = burnTicks * 2;
        this.burnHalfTotal = this.burnHalfTicks;
        this.setChanged();
        return true;
    }

    private void completeOperation() {
        final ItemStack input = this.inventory.getItem(INPUT_SLOT).copy();
        final int fortune = this.fortuneExtraRolls();
        final List<ItemStack> outputs = this.kind() == MachineKind.SIEVE
                ? SieveRolls.roll(input, fortune, this.level.random)
                : HammerRolls.roll(input, fortune, this.level.random);

        this.inventory.removeItem(INPUT_SLOT, 1);
        this.damageTool();

        for (final ItemStack stack : outputs) {
            if (!this.insertOutput(stack)) {
                Block.popResource(this.level, this.worldPosition.above(), stack);
            }
        }
        this.level.playSound(null, this.worldPosition,
                this.kind() == MachineKind.SIEVE ? SoundEvents.SAND_BREAK : SoundEvents.STONE_HIT,
                SoundSource.BLOCKS, 0.35F,
                this.kind() == MachineKind.SIEVE ? 0.9F : 0.7F);
        this.setChanged();
    }

    /**
     * Safety net for misplaced items (shift-click, pipes, commands). An item that
     * is not allowed in its current input-side slot is first moved to a slot that
     * does accept it (e.g. an upgrade that landed in the mesh slot goes to an
     * upgrade slot); only when no such slot exists is it popped out.
     */
    private void relocateInvalidSlotItems() {
        for (int slot = 0; slot < this.outputSlotStart(); slot++) {
            final ItemStack stack = this.inventory.getItem(slot);
            if (stack.isEmpty() || this.canPlaceInSlot(slot, stack)) {
                continue;
            }
            final int target = this.findSlotFor(stack, slot);
            if (target >= 0) {
                this.inventory.setItem(target, stack);
                this.inventory.setItem(slot, ItemStack.EMPTY);
                ExNihiloSatellitium.LOGGER.debug("[machine] moved {} from slot {} to {}",
                        stack.getItem(), slot, target);
            } else {
                this.inventory.setItem(slot, ItemStack.EMPTY);
                if (this.level != null) {
                    Block.popResource(this.level, this.worldPosition.above(), stack);
                }
                ExNihiloSatellitium.LOGGER.debug("[machine] no slot for {}, popped out",
                        stack.getItem());
            }
        }
    }

    /** First empty input-side slot that accepts this stack, excluding {@code skip}. */
    private int findSlotFor(final ItemStack stack, final int skip) {
        for (int slot = 0; slot < this.outputSlotStart(); slot++) {
            if (slot != skip && this.inventory.getItem(slot).isEmpty()
                    && this.canPlaceInSlot(slot, stack)) {
                return slot;
            }
        }
        return -1;
    }

    private boolean hasTool() {
        final ItemStack tool = this.inventory.getItem(TOOL_SLOT);
        return switch (this.kind()) {
            case SIEVE -> tool.getItem() instanceof MeshItem;
            case HAMMER -> tool.getItem() instanceof HammerItem;
        };
    }

    private void damageTool() {
        final ItemStack tool = this.inventory.getItem(TOOL_SLOT);
        if (tool.isEmpty() || !tool.isDamageableItem()) {
            return;
        }
        final int damage = tool.getDamageValue() + 1;
        if (damage >= tool.getMaxDamage()) {
            this.inventory.setItem(TOOL_SLOT, ItemStack.EMPTY);
            if (this.level != null) {
                this.level.playSound(null, this.worldPosition, SoundEvents.ITEM_BREAK,
                        SoundSource.BLOCKS, 0.8F, 1.0F);
            }
        } else {
            tool.setDamageValue(damage);
            this.inventory.setItem(TOOL_SLOT, tool);
        }
    }

    private boolean hasOutputSpace() {
        for (int slot = this.outputSlotStart(); slot < this.outputSlotEnd(); slot++) {
            final ItemStack stack = this.inventory.getItem(slot);
            if (stack.isEmpty() || stack.getCount() < Math.min(MAX_STACK, stack.getMaxStackSize())) {
                return true;
            }
        }
        return false;
    }

    /** Merges into the output buffer; {@code false} when every output slot is full. */
    private boolean insertOutput(final ItemStack stack) {
        ItemStack remaining = stack;
        for (int slot = this.outputSlotStart(); slot < this.outputSlotEnd(); slot++) {
            final ItemStack existing = this.inventory.getItem(slot);
            if (existing.isEmpty()) {
                this.inventory.setItem(slot, remaining);
                return true;
            }
            if (ItemStack.isSameItemSameComponents(existing, remaining)) {
                final int limit = Math.min(existing.getMaxStackSize(), (int) MAX_STACK);
                final int room = limit - existing.getCount();
                if (room > 0) {
                    final int moved = Math.min(room, remaining.getCount());
                    existing.grow(moved);
                    remaining.shrink(moved);
                    this.inventory.setItem(slot, existing);
                    if (remaining.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return remaining.isEmpty();
    }

    // ------------------------------------------------------------ slot rules

    public boolean canPlaceInSlot(final int slot, final ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        if (slot == TOOL_SLOT) {
            return switch (this.kind()) {
                case SIEVE -> stack.getItem() instanceof MeshItem;
                case HAMMER -> stack.getItem() instanceof HammerItem;
            };
        }
        if (slot == INPUT_SLOT) {
            return this.isValidInput(stack);
        }
        if (this.electric) {
            return slot >= UPGRADE_SLOT_START && slot < UPGRADE_SLOT_START + UPGRADE_SLOTS
                    && stack.getItem() instanceof UpgradeItem;
        }
        if (slot == FUEL_SLOT) {
            return stack.getBurnTime(RecipeType.SMELTING) > 0;
        }
        // Output slots are machine-owned: players and pipes may only take from them.
        return false;
    }

    private boolean isValidInput(final ItemStack stack) {
        return switch (this.kind()) {
            case SIEVE -> !SieveRegistry.dropsFor(stack).isEmpty();
            case HAMMER -> HammerRolls.isCrushable(stack);
        };
    }

    // -------------------------------------------------------------- menu/data

    @Override
    public Component getDisplayName() {
        // The block's own name carries the kind (sieve / hammer) and the power
        // tier, so the GUI title always matches the item the player placed.
        return this.getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(final int id, final Inventory playerInventory,
                                            final Player player) {
        return this.electric
                ? new ElectricMachineMenu(id, playerInventory, this)
                : new FuelMachineMenu(id, playerInventory, this);
    }

    public boolean stillValid(final Player player) {
        if (this.level == null || this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(this.worldPosition.getX() + 0.5D,
                this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public int get(final int index) {
        return switch (index) {
            case DATA_WORK -> this.work;
            case DATA_TICKS -> this.ticksPerOperation();
            case DATA_ENERGY -> this.energy == null ? 0 : this.energy.getEnergyStored();
            case DATA_ENERGY_MAX -> this.energy == null ? 0 : this.energy.getMaxEnergyStored();
            case DATA_BURN -> this.burnHalfTicks;
            case DATA_BURN_MAX -> this.burnHalfTotal;
            case DATA_ACTIVE -> this.active ? 1 : 0;
            case DATA_STATUS -> this.currentStatus();
            default -> 0;
        };
    }

    /**
     * Why the machine is or is not running, for the GUI status line.
     * 0 running, 1 no tool, 2 no input, 3 no energy, 4 output full, 5 no fuel.
     */
    public int currentStatus() {
        if (!this.hasTool()) {
            return 1;
        }
        if (!this.hasOutputSpace()) {
            return 4;
        }
        final ItemStack input = this.inventory.getItem(INPUT_SLOT);
        if (input.isEmpty() || !this.isValidInput(input)) {
            return 2;
        }
        if (this.electric) {
            return this.energyPerTick() > 0 && this.energy.getEnergyStored() < this.energyPerTick()
                    ? 3 : 0;
        }
        if (this.burnHalfTicks < this.burnHalfPerTick() && !this.hasFuel()) {
            return 5;
        }
        return 0;
    }

    private boolean hasFuel() {
        return this.inventory.getItem(FUEL_SLOT).getBurnTime(RecipeType.SMELTING) > 0;
    }

    @Override
    public void set(final int index, final int value) {
        // Read-only container data: the server owns every value.
    }

    @Override
    public int getCount() {
        return DATA_COUNT;
    }

    // --------------------------------------------------------------- save/load

    @Override
    protected void saveAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        final ListTag items = new ListTag();
        for (int slot = 0; slot < this.inventory.getContainerSize(); slot++) {
            final ItemStack stack = this.inventory.getItem(slot);
            if (!stack.isEmpty()) {
                if (stack.save(registries) instanceof CompoundTag entry) {
                    entry.putByte("Slot", (byte) slot);
                    items.add(entry);
                }
            }
        }
        tag.put("Items", items);
        tag.putInt("Work", this.work);
        tag.putInt("BurnHalfTicks", this.burnHalfTicks);
        tag.putInt("BurnHalfTotal", this.burnHalfTotal);
        if (this.energy != null) {
            tag.putInt("Energy", this.energy.getEnergyStored());
        }
    }

    @Override
    protected void loadAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        for (int slot = 0; slot < this.inventory.getContainerSize(); slot++) {
            this.inventory.setItem(slot, ItemStack.EMPTY);
        }
        final ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < items.size(); i++) {
            final CompoundTag entry = items.getCompound(i);
            final int slot = entry.getByte("Slot") & 255;
            if (slot < this.inventory.getContainerSize()) {
                this.inventory.setItem(slot, ItemStack.parseOptional(registries, entry));
            }
        }
        this.work = tag.getInt("Work");
        this.burnHalfTicks = tag.getInt("BurnHalfTicks");
        this.burnHalfTotal = tag.getInt("BurnHalfTotal");
        if (this.energy != null) {
            this.energy.setEnergyStored(tag.getInt("Energy"));
        }
    }

    @Override
    public CompoundTag getUpdateTag(final HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
