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
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class AreaBreakerItems {
    public static final ItemGroup ITEM_GROUP = Registry.register(
            Registries.ITEM_GROUP,
            Identifier.of("areabreaker", "main"),
            FabricItemGroup.builder()
                    .icon(() -> book(1))
                    .displayName(Text.literal("AreaBreaker"))
                    .entries((context, entries) -> {
                        entries.add(book(1));
                        entries.add(book(2));
                        entries.add(book(3));
                    })
                    .build()
    );

    private AreaBreakerItems() {}

    public static void initialize() {
        // Loading this class registers the creative tab.
    }

    private static ItemStack book(int tier) {
        ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK);
        String name = "Книга «Шахтёр " + (tier == 1 ? "I" : tier == 2 ? "II" : "III") + "»";
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name));

        ItemEnchantmentsComponent.Builder enchantments =
                new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
        add(enchantments, Enchantments.EFFICIENCY, tier == 1 ? 2 : tier == 2 ? 3 : 5);
        add(enchantments, Enchantments.FORTUNE, tier == 1 ? 1 : tier == 2 ? 2 : 3);
        add(enchantments, Enchantments.UNBREAKING, tier == 1 ? 2 : 3);
        if (tier == 3) add(enchantments, Enchantments.MENDING, 1);
        stack.set(DataComponentTypes.STORED_ENCHANTMENTS, enchantments.build());
        return stack;
    }

    private static void add(ItemEnchantmentsComponent.Builder builder,
                            net.minecraft.registry.RegistryKey<net.minecraft.enchantment.Enchantment> key,
                            int level) {
        builder.add(Registries.ENCHANTMENT.getEntry(Registries.ENCHANTMENT.getValueOrThrow(key)), level);
    }
}
