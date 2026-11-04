package dev.areabreaker;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class AreaBreakerItems {
    public static final ItemGroup ITEM_GROUP = Registry.register(
            Registries.ITEM_GROUP,
            Identifier.of("areabreaker", "main"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(Items.ENCHANTED_BOOK))
                    .displayName(Text.literal("AreaBreaker"))
                    .entries((context, entries) -> {
                        entries.add(book(1, context.lookup()));
                        entries.add(book(2, context.lookup()));
                        entries.add(book(3, context.lookup()));
                    })
                    .build()
    );

    private AreaBreakerItems() {}

    public static void initialize() {
        // Loading this class registers the creative tab.
    }

    private static ItemStack book(int tier, RegistryWrapper.WrapperLookup registries) {
        ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK);
        String name = "Книга «Шахтёр " + (tier == 1 ? "I" : tier == 2 ? "II" : "III") + "»";
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name));

        ItemEnchantmentsComponent.Builder enchantments =
                new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
        add(enchantments, registries, Enchantments.EFFICIENCY, tier == 1 ? 2 : tier == 2 ? 3 : 5);
        add(enchantments, registries, Enchantments.FORTUNE, tier == 1 ? 1 : tier == 2 ? 2 : 3);
        add(enchantments, registries, Enchantments.UNBREAKING, tier == 1 ? 2 : 3);
        if (tier == 3) add(enchantments, registries, Enchantments.MENDING, 1);
        stack.set(DataComponentTypes.STORED_ENCHANTMENTS, enchantments.build());
        return stack;
    }

    private static void add(ItemEnchantmentsComponent.Builder builder,
                            RegistryWrapper.WrapperLookup registries,
                            RegistryKey<net.minecraft.enchantment.Enchantment> key,
                            int level) {
        builder.add(registries.getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(key), level);
    }
}
