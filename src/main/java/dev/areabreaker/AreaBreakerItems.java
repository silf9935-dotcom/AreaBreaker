package dev.areabreaker;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.component.DataComponentTypes;
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
                    .icon(() -> book("Книга «Шахтёр I»"))
                    .displayName(Text.literal("AreaBreaker"))
                    .entries((context, entries) -> {
                        entries.add(book("Книга «Шахтёр I»"));
                        entries.add(book("Книга «Шахтёр II»"));
                        entries.add(book("Книга «Шахтёр III»"));
                    })
                    .build()
    );

    private AreaBreakerItems() {}

    public static void initialize() {
        // Loading this class registers the mod creative tab.
    }

    private static ItemStack book(String name) {
        ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name));
        return stack;
    }
}
