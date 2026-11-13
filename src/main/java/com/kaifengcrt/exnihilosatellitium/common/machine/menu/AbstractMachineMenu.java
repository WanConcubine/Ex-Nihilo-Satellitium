package com.kaifengcrt.exnihilosatellitium.common.machine.menu;

import com.kaifengcrt.exnihilosatellitium.common.machine.MachineKind;
import com.kaifengcrt.exnihilosatellitium.common.machine.UpgradeItem;
import com.kaifengcrt.exnihilosatellitium.common.machine.UpgradeType;
import com.kaifengcrt.exnihilosatellitium.common.machine.block.MachineBlock;
import com.kaifengcrt.exnihilosatellitium.common.machine.blockentity.MachineBlockEntity;
import com.kaifengcrt.exnihilosatellitium.common.machine.blockentity.MachineInventory;
import com.kaifengcrt.exnihilosatellitium.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Shared slot layout for the automation machines.
 *
 * <p>Machine area (top 94 px): tool + input on the left, a 3x3 output buffer on
 * the right. Electric machines add four upgrade slots along the bottom and an
 * energy bar; fuel machines add one fuel slot and a flame gauge instead.</p>
 *
 * <p>Client menus always read their synced values from a writable
 * {@link SimpleContainerData}; the server menu reads straight from the block
 * entity. Using the block entity as the client-side data source silently freezes
 * the gauges, because {@code ContainerData#set} is a no-op there.</p>
 */
public abstract class AbstractMachineMenu extends AbstractContainerMenu {

    public static final int TOOL_Y = 20;
    public static final int INPUT_Y = 44;
    public static final int LOWER_Y = 68;
    public static final int OUTPUT_X = 99;
    public static final int OUTPUT_Y = 20;
    public static final int INVENTORY_Y = 104;
    public static final int HOTBAR_Y = 162;

    /**
     * Machine slots must enforce {@link Container#canPlaceItem}: in 1.21.1 the
     * base {@link Slot#mayPlace} returns {@code true} unconditionally, so a plain
     * Slot lets shift-click/drag put anything anywhere (upgrades in the mesh slot).
     */
    private static final class MachineSlot extends Slot {

        private MachineSlot(final Container container, final int slot, final int x, final int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(final ItemStack stack) {
            return this.container.canPlaceItem(this.getContainerSlot(), stack);
        }

        @Override
        public int getMaxStackSize(final ItemStack stack) {
            if (this.container instanceof MachineInventory inventory
                    && inventory.owner().kind() == MachineKind.HAMMER
                    && this.getContainerSlot() == MachineBlockEntity.UPGRADE_SLOT_START + 1) {
                return 1;
            }
            return super.getMaxStackSize(stack);
        }
    }

    protected final MachineBlockEntity owner;
    protected final Container machine;
    protected final ContainerData data;
    protected final boolean electric;
    protected final MachineKind kind;

    private final PagedOutputContainer outputView;
    private final int machineSlotCount;
    /** Current output page; the server owns the value and clients mirror it. */
    private int page;

    protected AbstractMachineMenu(final MenuType<?> type, final int id,
                                  final Inventory playerInventory,
                                  final MachineBlockEntity owner, final MachineKind kind,
                                  final boolean electric) {
        super(type, id);
        this.owner = owner;
        this.electric = electric;
        this.kind = kind;
        this.machine = owner != null ? owner.getInventory()
                : new SimpleContainer(MachineBlockEntity.maxInventorySize(electric));
        this.data = playerInventory.player.level().isClientSide
                ? new SimpleContainerData(MachineBlockEntity.DATA_COUNT)
                : (owner != null ? owner : new SimpleContainerData(MachineBlockEntity.DATA_COUNT));
        this.outputView = new PagedOutputContainer(this.machine,
                MachineBlockEntity.outputStart(electric), MachineBlockEntity.OUTPUT_SLOTS,
                this::page);
        this.machineSlotCount = MachineBlockEntity.outputStart(electric)
                + MachineBlockEntity.OUTPUT_SLOTS;

        this.addSlot(new MachineSlot(this.machine, MachineBlockEntity.TOOL_SLOT, 26, TOOL_Y));
        this.addSlot(new MachineSlot(this.machine, MachineBlockEntity.INPUT_SLOT, 26, INPUT_Y));

        if (electric) {
            for (int i = 0; i < MachineBlockEntity.UPGRADE_SLOTS; i++) {
                this.addSlot(new MachineSlot(this.machine,
                        MachineBlockEntity.UPGRADE_SLOT_START + i, 26 + i * 18, LOWER_Y));
            }
        } else {
            this.addSlot(new MachineSlot(this.machine, MachineBlockEntity.FUEL_SLOT, 26, LOWER_Y));
        }

        // 3 rows x 4 columns = 12 visible output slots; the view maps them onto
        // the current page of the machine's flat output buffer.
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 4; column++) {
                this.addSlot(new MachineSlot(this.outputView, row * 4 + column,
                        OUTPUT_X + column * 18, OUTPUT_Y + row * 18));
            }
        }

        this.addDataSlots(this.data);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9,
                        8 + column * 18, INVENTORY_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInventory, column, 8 + column * 18, HOTBAR_Y));
        }
    }

    public MachineBlockEntity owner() {
        return this.owner;
    }

    public ContainerData data() {
        return this.data;
    }

    public MachineKind kind() {
        return this.kind;
    }

    /** Number of output pages; 1 without capacity upgrades. */
    public int pages() {
        if (this.owner != null) {
            return this.owner.outputPages();
        }
        if (!this.electric) {
            return 1;
        }
        // The block entity should always be present on the client; keep a
        // fallback for the one tick where the menu exists before the chunk has
        // delivered its block entity.
        final ItemStack capacity = this.machine.getItem(MachineBlockEntity.CAPACITY_SLOT);
        if (capacity.getItem() instanceof UpgradeItem upgrade
                && upgrade.type() == UpgradeType.CAPACITY) {
            return 1 + Math.min(MachineBlockEntity.MAX_CAPACITY_PAGES, capacity.getCount());
        }
        return 1;
    }

    /** Clamped current page, so removing a capacity upgrade never leaves a stale page. */
    public int page() {
        final int max = Math.max(0, this.pages() - 1);
        if (this.page < 0) {
            this.page = 0;
        } else if (this.page > max) {
            this.page = max;
        }
        return this.page;
    }

    public void setPage(final int page) {
        this.page = Math.max(0, Math.min(page, Math.max(0, this.pages() - 1)));
    }

    /**
     * Page buttons use the vanilla container-button channel: the client updates
     * its own page immediately for responsiveness and sends the absolute page;
     * the server validates it, stores the same page and re-sends the whole menu
     * content through the paged view. A full resync (instead of a diff) is
     * required because the client's cached slots for the target page may be
     * older than the slot indices the diff compares against.
     */
    @Override
    public boolean clickMenuButton(final Player player, final int id) {
        if (this.owner == null || !this.owner.stillValid(player)) {
            return false;
        }
        if (id < 0 || id >= this.pages()) {
            return false;
        }
        this.page = id;
        this.sendAllDataToRemote();
        return true;
    }

    public int machineSlotCount() {
        return this.machineSlotCount;
    }

    /**
     * Ghost item shown in an empty machine slot. Only the machine slots are
     * queried; output slots return {@link ItemStack#EMPTY}.
     */
    public ItemStack ghostFor(final int index) {
        if (index == MachineBlockEntity.TOOL_SLOT) {
            return new ItemStack(this.kind == MachineKind.SIEVE
                    ? ModItems.SILK_MESH.get() : ModItems.IRON_HAMMER.get());
        }
        if (index == MachineBlockEntity.INPUT_SLOT) {
            return new ItemStack(this.kind == MachineKind.SIEVE
                    ? Items.SAND : Items.COBBLESTONE);
        }
        if (this.electric) {
            if (index == MachineBlockEntity.UPGRADE_SLOT_START) {
                return new ItemStack(ModItems.SPEED_UPGRADE_1.get());
            }
            if (index == MachineBlockEntity.UPGRADE_SLOT_START + 1) {
                return new ItemStack(ModItems.FORTUNE_UPGRADE_1.get());
            }
            if (index == MachineBlockEntity.CAPACITY_SLOT) {
                return new ItemStack(ModItems.CAPACITY_UPGRADE.get());
            }
            return ItemStack.EMPTY;
        }
        if (index == MachineBlockEntity.FUEL_SLOT) {
            return new ItemStack(Items.COAL);
        }
        return ItemStack.EMPTY;
    }

    /** Hover text for an empty machine slot; {@code null} for outputs. */
    public Component ghostHint(final int index) {
        if (this.ghostFor(index).isEmpty()) {
            return null;
        }
        if (index == MachineBlockEntity.TOOL_SLOT) {
            return Component.translatable(this.kind == MachineKind.SIEVE
                    ? "gui.exnihilosatellitium.slot.mesh"
                    : "gui.exnihilosatellitium.slot.hammer");
        }
        if (index == MachineBlockEntity.INPUT_SLOT) {
            return Component.translatable(this.kind == MachineKind.SIEVE
                    ? "gui.exnihilosatellitium.slot.input.sieve"
                    : "gui.exnihilosatellitium.slot.input.hammer");
        }
        if (this.electric) {
            if (index == MachineBlockEntity.UPGRADE_SLOT_START) {
                return Component.translatable("gui.exnihilosatellitium.slot.speed_only");
            }
            if (index == MachineBlockEntity.CAPACITY_SLOT) {
                return Component.translatable("gui.exnihilosatellitium.slot.capacity_only");
            }
            return Component.translatable(this.kind == MachineKind.HAMMER
                    ? "gui.exnihilosatellitium.slot.fortune_only_1"
                    : "gui.exnihilosatellitium.slot.fortune_only");
        }
        return Component.translatable("gui.exnihilosatellitium.slot.fuel");
    }

    /** Hover hint for an empty machine slot; {@code null} for anything else. */
    public Component hintFor(final Slot slot) {
        if (slot == null || slot.hasItem() || slot.container != this.machine) {
            return null;
        }
        return this.ghostHint(slot.index);
    }

    @Override
    public boolean stillValid(final Player player) {
        return this.owner == null || this.owner.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        final Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        final ItemStack stack = slot.getItem();
        final ItemStack copy = stack.copy();
        final int machineSlots = this.machineSlotCount;
        if (index < machineSlots) {
            if (!this.moveItemStackTo(stack, machineSlots, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, machineSlots, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return copy;
    }

    /** Client side: locate the machine BE from the block position sent by the server. */
    protected static MachineBlockEntity blockEntityAt(final Inventory playerInventory,
                                                      final BlockPos pos) {
        return playerInventory.player.level().getBlockEntity(pos) instanceof MachineBlockEntity machine
                ? machine : null;
    }

    private static MachineKind kindAt(final Inventory playerInventory, final BlockPos pos) {
        return playerInventory.player.level().getBlockState(pos).getBlock()
                instanceof MachineBlock machine ? machine.kind() : MachineKind.SIEVE;
    }

    /** Client factory payload: the block entity plus the machine kind for ghost hints. */
    protected record ClientContext(MachineBlockEntity owner, MachineKind kind) {
    }

    /** Client factory helper: tolerate a missing payload instead of NPE-ing the client. */
    protected static ClientContext clientContext(final Inventory playerInventory,
                                                 final RegistryFriendlyByteBuf buffer) {
        if (buffer == null) {
            return new ClientContext(null, MachineKind.SIEVE);
        }
        final BlockPos pos = buffer.readBlockPos();
        return new ClientContext(blockEntityAt(playerInventory, pos), kindAt(playerInventory, pos));
    }
}
