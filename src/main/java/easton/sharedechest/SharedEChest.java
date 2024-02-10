package easton.sharedechest;

import easton.sharedechest.mixin.ServerAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.io.File;
import java.io.IOException;

public class SharedEChest implements ModInitializer {

	public static final ScreenHandlerType<SharedEnderChestHandler> SHARED_E_HANDLER;
	public static SimpleInventory sharedInventory = new SimpleInventory(27);
	public static final Identifier BUTTON_PRESS_ID = new Identifier("sharedechest", "button_press");

	static {
		SHARED_E_HANDLER = ScreenHandlerRegistry.registerSimple(new Identifier("sharedechest", "sharedechesthandler"), SharedEnderChestHandler::new);
	}

	@Override
	public void onInitialize() {
		ServerPlayNetworking.registerGlobalReceiver(BUTTON_PRESS_ID, (server, player, handler, buf, responseSender) -> {
			boolean shared = buf.readBoolean();

			if (player.currentScreenHandler instanceof SharedEnderChestHandler secHandler) {
				if (shared) {
					secHandler.inventory = sharedInventory;
				} else {
					secHandler.inventory = ((SharedEnderChestHandler) player.currentScreenHandler).personalInv;
				}
				secHandler.createSlots(player.getInventory());
			}
		});

	}

	public static void loadSharedInv(MinecraftServer server) throws IOException {
		File overworldFile = ((ServerAccessor)server).getSession().getWorldDirectory(ServerWorld.OVERWORLD).toFile();
		File file = new File(overworldFile, "shared_ender_chest.dat");
		if (file.exists()) {
			NbtCompound compound = NbtIo.readCompressed(file.toPath(), NbtSizeTracker.ofUnlimitedBytes());
			NbtList list = compound.getList("inv", 10);
			readNbtList(list, sharedInventory);
		}
	}

	public static void saveSharedInv(MinecraftServer server) throws IOException {
		File overworldFile = ((ServerAccessor)server).getSession().getWorldDirectory(ServerWorld.OVERWORLD).toFile();
		File file = File.createTempFile("shared_ender_chest", ".dat", overworldFile);
		NbtCompound nbtCompound = new NbtCompound();
		nbtCompound.put("inv", toNbtList(sharedInventory));
		NbtIo.writeCompressed(nbtCompound, file.toPath());
		File file2 = new File(overworldFile, "shared_ender_chest.dat");
		File file3 = new File(overworldFile, "shared_ender_chest.dat_old");
		Util.backupAndReplace(file2.toPath(), file.toPath(), file3.toPath());
	}

	public static void readNbtList(NbtList nbtList, SimpleInventory inv) {
		int j;
		for(j = 0; j < inv.size(); ++j) {
			inv.setStack(j, ItemStack.EMPTY);
		}

		for(j = 0; j < nbtList.size(); ++j) {
			NbtCompound nbtCompound = nbtList.getCompound(j);
			int k = nbtCompound.getByte("Slot") & 255;
			if (k >= 0 && k < inv.size()) {
				inv.setStack(k, ItemStack.fromNbt(nbtCompound));
			}
		}
	}

	public static NbtList toNbtList(SimpleInventory inv) {
		NbtList nbtList = new NbtList();
		for(int i = 0; i < inv.size(); ++i) {
			ItemStack itemStack = inv.getStack(i);
			if (!itemStack.isEmpty()) {
				NbtCompound nbtCompound = new NbtCompound();
				nbtCompound.putByte("Slot", (byte)i);
				itemStack.writeNbt(nbtCompound);
				nbtList.add(nbtCompound);
			}
		}
		return nbtList;
	}
}
