package dev.areabreaker;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class AreaBreakerItems {
    public static final Item MINER_I = register("miner_i");
    public static final Item MINER_II = register("miner_ii");
    public static final Item MINER_III = register("miner_iii");

    public static final ItemGroup ITEM_GROUP = Registry.register(
            Registries.ITEM_GROUP,
            Identifier.of("areabreaker", "main"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(MINER_I))
                    .displayName(Text.literal("AreaBreaker"))
                    .entries((context, entries) -> {
                        entries.add(new ItemStack(MINER_I));
                        entries.add(new ItemStack(MINER_II));
                        entries.add(new ItemStack(MINER_III));
                    })
                    .build()
    );

    private AreaBreakerItems() {}

    public static void initialize() {
        // Loading this class registers the mod books and creative tab.
    }

    private static Item register(String path) {
        Identifier id = Identifier.of("areabreaker", path);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        return Registry.register(Registries.ITEM, id,
                new EnchantedBookItem(new Item.Settings().registryKey(key)));
    }
}
