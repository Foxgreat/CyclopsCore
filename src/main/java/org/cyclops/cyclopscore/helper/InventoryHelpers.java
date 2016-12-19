package org.cyclops.cyclopscore.helper;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import javax.annotation.Nullable;

/**
 * Contains helper methods involving {@link net.minecraft.inventory.IInventory}S.
 * @author immortaleeb
 *
 */
public class InventoryHelpers {

	/**
	 * Erase a complete inventory
	 * @param inventory inventory to clear
	 */
	public static void clearInventory(IInventory inventory) {
	    for (int i = 0; i < inventory.getSizeInventory(); i++) {
	        inventory.setInventorySlotContents(i, ItemStack.EMPTY);
	    }
	}
	
	/**
	 * Try adding a new item stack originating from the given original stack to the same original stack.
	 * The original item stack should not have it's stack-size decreased yet, this method does this.
	 * Otherwise it will add the new stack to another inventory slot and in the worst case drop it on the floor.
	 * @param player The player.
	 * @param originalStack The original item stack from which the new item stack originated.
	 * @param newStackPart The new item stack.
	 */
	public static void tryReAddToStack(EntityPlayer player, @Nullable ItemStack originalStack, ItemStack newStackPart) {
		if (!player.capabilities.isCreativeMode) {
        	if(originalStack != null && originalStack.getCount() - 1 == 0) {
        		player.inventory.setInventorySlotContents(player.inventory.currentItem, newStackPart);
        	} else {
                if(originalStack != null) {
					originalStack.shrink(1);
				}
        		if(!player.inventory.addItemStackToInventory(newStackPart)) {
        			player.dropItem(newStackPart, false);
        		}
        	}
        }
	}
	
	/**
	 * Validate the NBT storage of the given inventory in the given item.
	 * Should be called in constructors of inventories.
	 * @param inventory The inventory.
	 * @param itemStack The item stack to read/write.
	 * @param tagName The tag name to read from.
	 */
	public static void validateNBTStorage(IInventory inventory, ItemStack itemStack, String tagName) {
		NBTTagCompound tag = itemStack.getTagCompound();
		if(tag == null) {
			tag = new NBTTagCompound();
			itemStack.setTagCompound(tag);
		}
		if(!tag.hasKey(tagName)) {
			tag.setTag(tagName, new NBTTagList());
		}
		readFromNBT(inventory, tag, tagName);
	}
	
	/**
	 * Read an inventory from NBT.
	 * @param inventory The inventory.
	 * @param data The tag to read from.
	 * @param tagName The tag name to read from.
	 */
	public static void readFromNBT(IInventory inventory, NBTTagCompound data, String tagName) {
        NBTTagList nbttaglist = data.getTagList(tagName, MinecraftHelpers.NBTTag_Types.NBTTagCompound.ordinal());
        
        for(int j = 0; j < inventory.getSizeInventory(); j++) {
        	inventory.setInventorySlotContents(j, ItemStack.EMPTY);
        }

        for(int j = 0; j < nbttaglist.tagCount(); j++) {
            NBTTagCompound slot = nbttaglist.getCompoundTagAt(j);
            int index;
            if(slot.hasKey("index")) {
                index = slot.getInteger("index");
            } else {
                index = slot.getByte("Slot");
            }
            if(index >= 0 && index < inventory.getSizeInventory()) {
            	inventory.setInventorySlotContents(index, new ItemStack(slot));
            }
        }
    }
	
	/**
	 * Write the given inventory to NBT.
	 * @param inventory The inventory.
	 * @param data The tag to write to.
	 * @param tagName The tag name to write into.
	 */
	public static void writeToNBT(IInventory inventory, NBTTagCompound data, String tagName) {
        NBTTagList slots = new NBTTagList();
        for(byte index = 0; index < inventory.getSizeInventory(); ++index) {
        	ItemStack itemStack = inventory.getStackInSlot(index);
            if(!itemStack.isEmpty() && itemStack.getCount() > 0) {
                NBTTagCompound slot = new NBTTagCompound();
                slot.setInteger("index", index);
                slots.appendTag(slot);
                itemStack.writeToNBT(slot);
            }
        }
        data.setTag(tagName, slots);
    }
	
	/**
	 * Get the item stack from the given index in the player inventory.
	 * @param player The player.
	 * @param itemIndex The index of the item in the inventory.
	 * @return The item stack.
	 */
	public static ItemStack getItemFromIndex(EntityPlayer player, int itemIndex) {
		return player.inventory.mainInventory.get(itemIndex);
	}

	/**
	 * Try to add the given item to the given slot.
	 * @param inventory The inventory.
	 * @param slot The slot to add to.
	 * @param itemStack The item to try to put in the production slot.
	 * @return If the item could be added or joined in the production slot.
	 */
	public static boolean addToSlot(IInventory inventory, int slot, ItemStack itemStack) {
		return addToSlot(inventory, slot, itemStack, false);
	}
	
	/**
     * Try to add the given item to the given slot.
     * @param inventory The inventory.
     * @param slot The slot to add to.
     * @param itemStack The item to try to put in the production slot.
	 * @param simulate If the operation should be simulated.
     * @return If the item could be added or joined in the production slot.
     */
    public static boolean addToSlot(IInventory inventory, int slot, ItemStack itemStack, boolean simulate) {
        ItemStack produceStack = inventory.getStackInSlot(slot);
        if(produceStack.isEmpty()) {
        	inventory.setInventorySlotContents(slot, itemStack);
            return true;
        } else {
            if(produceStack.getItem() == itemStack.getItem()
               && produceStack.getMaxStackSize() >= produceStack.getCount() + itemStack.getCount()) {
				if(!simulate) {
					produceStack.grow(itemStack.getCount());
				}
                return true;
            }
        }
        return false;
    }

}
