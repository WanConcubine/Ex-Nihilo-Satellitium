package com.kaifengcrt.exnihilosatellitium.common.machine.blockentity;

import com.kaifengcrt.exnihilosatellitium.ExNihiloSatellitium;
import com.kaifengcrt.exnihilosatellitium.common.config.ConfigValues;
import com.kaifengcrt.exnihilosatellitium.common.item.HammerTool;
import com.kaifengcrt.exnihilosatellitium.common.item.HammerItem;
import com.kaifengcrt.exnihilosatellitium.common.item.MeshItem;
import com.kaifengcrt.exnihilosatellitium.common.machine.HammerRolls;
import com.kaifengcrt.exnihilosatellitium.common.machine.MachineEnergyBuffer;
import com.kaifengcrt.exnihilosatellitium.common.machine.MachineKind;
import com.kaifengcrt.exnihilosatellitium.common.machine.SieveRolls;
import com.kaifengcrt.exnihilosatellitium.common.machine.UpgradeItem;
import com.kaifengcrt.exnihilosatellitium.common.machine.UpgradeType;
import com.kaifengcrt.exnihilosatellitium.common.machine.block.MachineBlock;
import com.kaifengcrt.exnihilosatellitium.common.machine.menu.ElectricMachineMenu;
import com.kaifengcrt.exnihilosatellitium.common.machine.menu.FuelMachineMenu;
import com.kaifengcrt.exnihilosatellitium.common.sieve.SieveRegistry;
import com.kaifengcrt.exnihilosatellitium.platform.machine.MachineHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
    public static final int DATA_AUTO_OUTPUT = 8;
    public static final int DATA_OUTPUT_MASK = 9;
    public static final int DATA_COUNT = 10;

    public static final int TOOL_SLOT = 0;
    public static final int INPUT_SLOT = 1;
    public static final int FUEL_SLOT = 2;
    public static final int UPGRADE_SLOT_START = 2;
    public static final int UPGRADE_SLOTS = 4;
    public static final int OUTPUT_SLOTS = 12;

    private static final int ELECTRIC_OUTPUT_START = UPGRADE_SLOT_START + UPGRADE_SLOTS;
    private static final int FUEL_OUTPUT_START = FUEL_SLOT + 1;
    private static final double MAX_STACK = 64.0D;

    /** One bit per {@link Direction#get3DDataValue()}; the auto-output target faces. */
    public static final int FACE_DOWN = 1 << Direction.DOWN.get3DDataValue();
    public static final int ALL_FACES = 0x3F;
    /** How often the machine retries pushing leftovers into its output faces. */
    private static final int EJECT_INTERVAL = 20;

    private final boolean electric;
    private final MachineInventory inventory;
    private final MachineEnergyBuffer energy;

    private int work;
    private int burnHalfTicks;
    private int burnHalfTotal;
    private boolean active;

    /** Auto-output configuration: master switch plus the faces to push into. */
    private boolean autoOutput;
    private int outputMask = FACE_DOWN;
    private int ejectCounter;

    protected MachineBlockEntity(final BlockEntityType<?> type, final BlockPos pos,
                                 final BlockState state, final boolean electric) {
        super(type, pos, state);
        this.electric = electric;
        this.inventory = new MachineInventory(this, electric ? 18 : 15);
        this.energy = electric ? new MachineEnergyBuffer(ConfigValues.machineEnergyCapacity()) : null;
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
    public MachineEnergyBuffer getEnergyBuffer() {
        return this.energy;
    }

    public int getWork() {
        return this.work;
    }

    public boolean isActive() {
        return this.active;
    }

    /** Called when the working flag changes; syncs the BE so the client can emit particles. */
    private void setActive(final boolean value) {
        if (this.active == value) {
            return;
        }
        this.active = value;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(),
                    this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public int getBurnHalfTicks() {
        return this.burnHalfTicks;
    }

    public int getBurnHalfTotal() {
        return this.burnHalfTotal;
    }

    public int ticksPerOperation() {
        final double multiplier = this.electric ? this.speedMultiplier() : 1.0D;
        return Math.max(1, (int) Math.round(ConfigValues.machineTicksPerOperation() / multiplier));
    }

    /** All installed upgrades of one type, best tier first. */
    private java.util.List<UpgradeItem> upgradesOf(final UpgradeType type) {
        final java.util.List<UpgradeItem> list = new java.util.ArrayList<>();
        if (!this.electric) {
            return list;
        }
        for (int i = 0; i < UPGRADE_SLOTS; i++) {
            final ItemStack stack = this.inventory.getItem(UPGRADE_SLOT_START + i);
            if (stack.getItem() instanceof UpgradeItem upgrade && upgrade.type() == type) {
                // each item in the stack is one upgrade, not one per slot; hammer
                // machines accept only one fortune item
                final int count = type == UpgradeType.FORTUNE
                        && this.kind() == MachineKind.HAMMER
                        ? Math.min(1, stack.getCount()) : stack.getCount();
                for (int n = 0; n < count; n++) {
                    list.add(upgrade);
                }
            }
        }
        list.sort(java.util.Comparator.comparingInt(UpgradeItem::tier).reversed());
        return list;
    }

    /** Number of installed upgrade items (0..4); drives the FE surcharge. */
    public int upgradeCount() {
        return this.upgradesOf(UpgradeType.SPEED).size() + this.upgradesOf(UpgradeType.FORTUNE).size();
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
     * Fortune stacking by proc chance: the first fortune item always procs, each
     * further one has 80% of the previous chance (100%, 80%, 64%, ...). When an
     * item procs it adds its tier as extra independent rolls.
     */
    public int fortuneExtraRolls() {
        int rolls = 0;
        double chance = 1.0D;
        for (final UpgradeItem upgrade : this.upgradesOf(UpgradeType.FORTUNE)) {
            if (this.level != null && this.level.random.nextDouble() < chance) {
                rolls += upgrade.tier();
            }
            chance *= 0.8D;
        }
        return rolls;
    }

    public int energyPerTick() {
        final int base = ConfigValues.machineEnergyPerTick();
        // Each installed upgrade adds 25% draw on top of the base.
        return (int) Math.round(base * (1.0D + 0.25D * Math.max(0, this.upgradeCount() - 1)));
    }

    public int burnHalfPerOperation() {
        return ConfigValues.machineBurnPerOperation() * 2;
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

    // ---------------------------------------------------------- auto output

    public boolean isAutoOutput() {
        return this.autoOutput;
    }

    /** Bit mask of faces the machine ejects into; see {@link #FACE_DOWN}. */
    public int getOutputMask() {
        return this.outputMask;
    }

    public boolean outputsTo(final Direction direction) {
        return (this.outputMask & (1 << direction.get3DDataValue())) != 0;
    }

    /**
     * Applies the GUI configuration. Auto output needs at least one face, so an
     * empty mask falls back to the bottom — the direction a chest is normally
     * placed in. Enabling the switch pushes whatever is already buffered.
     */
    public void setOutputConfig(final int mask, final boolean autoOutput) {
        final int clampedMask = mask & ALL_FACES;
        final int effectiveMask = autoOutput && clampedMask == 0 ? FACE_DOWN : clampedMask;
        if (this.outputMask == effectiveMask && this.autoOutput == autoOutput) {
            return;
        }
        this.outputMask = effectiveMask;
        this.autoOutput = autoOutput;
        this.setChanged();
        if (autoOutput) {
            this.pushOutputs();
        }
    }

    /**
     * Pushes the output buffer into the inventories behind the enabled faces.
     * One machine operation can fill several slots; every enabled face gets a
     * chance, and whatever the neighbour refuses stays in the buffer.
     */
    private void pushOutputs() {
        if (!this.autoOutput || this.outputMask == 0 || this.level == null
                || this.level.isClientSide) {
            return;
        }
        boolean changed = false;
        for (final Direction direction : Direction.values()) {
            if (!this.outputsTo(direction)) {
                continue;
            }
            for (int slot = this.outputSlotStart(); slot < this.outputSlotEnd(); slot++) {
                final ItemStack stack = this.inventory.getItem(slot);
                if (stack.isEmpty()) {
                    continue;
                }
                // The capability lookup lives behind the platform facade; a face
                // without an inventory simply returns the stack unchanged.
                final ItemStack remainder = MachineHooks.insertIntoNeighbour(this.level,
                        this.worldPosition, direction, stack.copy());
                if (remainder.getCount() == stack.getCount()) {
                    continue;
                }
                this.inventory.setItem(slot, remainder.isEmpty() ? ItemStack.EMPTY : remainder);
                changed = true;
            }
        }
        if (changed) {
            this.setChanged();
        }
    }

    // ---------------------------------------------------------------- ticking

    public void serverTick() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        this.relocateInvalidSlotItems();
        if (this.autoOutput && ++this.ejectCounter >= EJECT_INTERVAL) {
            this.ejectCounter = 0;
            this.pushOutputs();
        }
        if (!this.canWork()) {
            if (this.work != 0) {
                this.work = 0;
                this.setChanged();
            }
            this.setActive(false);
            return;
        }
        if (!this.consumePowerPerTick()) {
            if (this.work != 0) {
                this.work = 0;
                this.setChanged();
            }
            this.setActive(false);
            return;
        }
        this.setActive(true);
        this.work++;
        if (this.work >= this.ticksPerOperation()) {
            this.work = 0;
            this.completeOperation();
        }
        this.setChanged();
    }

    private boolean canWork() {
        if (!this.hasTool() || !this.hasToolEnergy() || !this.hasOutputSpace()) {
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
        this.consumeToolUse();

        for (final ItemStack stack : outputs) {
            if (!this.insertOutput(stack)) {
                Block.popResource(this.level, this.worldPosition.above(), stack);
            }
        }
        if (this.autoOutput) {
            this.pushOutputs();
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
            case HAMMER -> tool.getItem() instanceof HammerTool;
        };
    }

    /** True when the installed hammer can pay its next operation's cost. */
    private boolean hasToolEnergy() {
        if (this.kind() != MachineKind.HAMMER) {
            return true;
        }
        final ItemStack tool = this.inventory.getItem(TOOL_SLOT);
        if (!(tool.getItem() instanceof HammerTool hammer)) {
            return false;
        }
        return hammer.energyPerUse() <= 0
                || MachineHooks.getItemEnergy(tool) >= hammer.energyPerUse();
    }

    /** Sieve meshes wear durability; hammer tools either wear or pay energy. */
    private void consumeToolUse() {
        if (this.kind() == MachineKind.SIEVE) {
            this.damageTool();
            return;
        }
        final ItemStack tool = this.inventory.getItem(TOOL_SLOT);
        if (tool.isEmpty() || !(tool.getItem() instanceof HammerTool hammer)) {
            return;
        }
        if (hammer instanceof HammerItem) {
            this.damageTool();
            return;
        }
        if (hammer.energyPerUse() > 0) {
            MachineHooks.extractItemEnergy(tool, hammer.energyPerUse());
            this.inventory.setItem(TOOL_SLOT, tool);
            this.syncOpenMenus();
        }
    }

    /** Pushes the changed tool stack into any open machine GUI. */
    private void syncOpenMenus() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        this.level.players().forEach(player -> {
            if (player.containerMenu instanceof com.kaifengcrt.exnihilosatellitium.common.machine.menu.AbstractMachineMenu menu
                    && menu.owner() == this) {
                menu.broadcastChanges();
            }
        });
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
            this.syncOpenMenus();
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
                case HAMMER -> stack.getItem() instanceof HammerTool;
            };
        }
        if (slot == INPUT_SLOT) {
            return this.isValidInput(stack);
        }
        if (this.electric) {
            // one slot per upgrade type: speed in slot 2, fortune in slot 3,
            // slots 4/5 are reserved for future upgrade types.
            if (!(stack.getItem() instanceof UpgradeItem upgrade)) {
                return false;
            }
            if (slot == UPGRADE_SLOT_START) {
                return upgrade.type() == UpgradeType.SPEED;
            }
            if (slot == UPGRADE_SLOT_START + 1) {
                return upgrade.type() == UpgradeType.FORTUNE;
            }
            return false;
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
            case DATA_ENERGY -> this.energy == null ? 0 : this.energy.stored();
            case DATA_ENERGY_MAX -> this.energy == null ? 0 : this.energy.capacity();
            case DATA_BURN -> this.burnHalfTicks;
            case DATA_BURN_MAX -> this.burnHalfTotal;
            case DATA_ACTIVE -> this.active ? 1 : 0;
            case DATA_STATUS -> this.currentStatus();
            case DATA_AUTO_OUTPUT -> this.autoOutput ? 1 : 0;
            case DATA_OUTPUT_MASK -> this.outputMask;
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
            return this.energyPerTick() > 0 && this.energy.stored() < this.energyPerTick()
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
        tag.putBoolean("Active", this.active);
        tag.putInt("BurnHalfTicks", this.burnHalfTicks);
        tag.putInt("BurnHalfTotal", this.burnHalfTotal);
        tag.putBoolean("AutoOutput", this.autoOutput);
        tag.putInt("OutputMask", this.outputMask);
        if (this.energy != null) {
            tag.putInt("Energy", this.energy.stored());
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
        this.active = tag.getBoolean("Active");
        this.burnHalfTicks = tag.getInt("BurnHalfTicks");
        this.burnHalfTotal = tag.getInt("BurnHalfTotal");
        this.autoOutput = tag.getBoolean("AutoOutput");
        this.outputMask = tag.contains("OutputMask") ? tag.getInt("OutputMask") & ALL_FACES : FACE_DOWN;
        if (this.energy != null) {
            this.energy.setStored(tag.getInt("Energy"));
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
