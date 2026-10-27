package com.kaifengcrt.exnihiloimitatio.compat;

import com.kaifengcrt.exnihiloimitatio.ExNihiloImitatio;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

/**
 * 「月光魔法」（{@code moonlight_magic}）软联动入口。
 *
 * <p>对方没有公开的 {@code api} 包，因此这里刻意<b>不直接 import 对方的任何类</b>，
 * 只通过 mod id 和注册名查找。好处：
 * <ul>
 *   <li>没有编译期耦合 —— 对方改内部结构不会让我们编译失败；</li>
 *   <li>没装月光魔法时本模组照常启动；</li>
 *   <li>版本不匹配时最多是联动内容缺失，不会整体崩溃。</li>
 * </ul>
 *
 * <p>真正的联动内容（筛网、堆肥、配方）在 M8 用数据包 + 这里的查找方法实现。
 */
public final class MoonlightCompat {

    public static final String MOD_ID = "moonlight_magic";

    /** 月光魔法里适合做联动锚点的注册名（物品）。 */
    public static final String MOONLIGHT_THREAD = "moonlight_thread";
    public static final String POOL_DUST = "pool_dust";
    public static final String MOONLIGHT_POWER_BOTTLE = "moonlight_power_bottle";
    public static final String MOON_FLAX_SEEDS = "moon_flax_seeds";

    private MoonlightCompat() {
    }

    /** 月光魔法是否已加载。 */
    public static boolean isLoaded() {
        return ModList.get() != null && ModList.get().isLoaded(MOD_ID);
    }

    /**
     * 按注册名取对方物品。
     *
     * @return 物品；未安装、注册名不存在或注册表尚未就绪时返回 {@code null}
     */
    @Nullable
    public static Item item(final String path) {
        if (!isLoaded()) {
            return null;
        }
        return ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(MOD_ID, path));
    }

    /** 对方是否存在该物品。 */
    public static boolean hasItem(final String path) {
        return item(path) != null;
    }

    /** 在 common setup 阶段打印联动状态，方便日志排查。 */
    public static void logStatus() {
        if (isLoaded()) {
            ExNihiloImitatio.LOGGER.info("[compat] 月光魔法已加载，软联动启用。");
        } else {
            ExNihiloImitatio.LOGGER.info("[compat] 未检测到月光魔法，本模组独立运行。");
        }
    }
}
