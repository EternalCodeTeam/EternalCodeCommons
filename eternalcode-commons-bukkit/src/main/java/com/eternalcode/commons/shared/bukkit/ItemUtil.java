package com.eternalcode.commons.shared.bukkit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class ItemUtil {

    private ItemUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void removeItem(Player player, Material type, int amount) {
        if (amount <= 0) {
            return;
        }
        PlayerInventory inventory = player.getInventory();

        int size = inventory.getSize();

        for (int slot = 0; slot < size; slot++) {
            ItemStack is = inventory.getItem(slot);

            if (is == null) {
                continue;
            }

            if (type != is.getType()) {
                continue;
            }

            int newAmount = is.getAmount() - amount;
            if (newAmount > 0) {
                is.setAmount(newAmount);
                break;
            }

            inventory.clear(slot);

            amount = -newAmount;
            if (amount == 0) {
                break;
            }
        }
    }

    public static void giveItem(Player player, ItemStack itemStack) {
        if (hasSpace(player.getInventory(), itemStack)) {
            player.getInventory().addItem(itemStack);
            player.updateInventory();

            return;
        }

        player.getLocation().getWorld().dropItemNaturally(player.getLocation(), itemStack);
    }

    @Internal
    private static boolean hasSpace(Inventory inventory, ItemStack itemStack) {
        if (inventory.firstEmpty() != -1) {
            return true;
        }

        for (ItemStack itemInv : inventory.getContents()) {
            if (itemInv == null) {
                continue;
            }

            if (!itemInv.isSimilar(itemStack)) {
                continue;
            }

            if (itemInv.getMaxStackSize() > itemInv.getAmount()) {
                return true;
            }
        }

        return false;
    }

}
