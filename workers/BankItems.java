package misc.scripts.runecrafter.workers;

import java.util.ArrayList;
import java.util.List;

import misc.scripts.runecrafter.MiscRunecrafter;
import misc.scripts.runecrafter.util.Util;
import misc.scripts.runecrafter.util.Values;

import org.hexbot.api.methods.Calculations;
import org.hexbot.api.methods.Camera;
import org.hexbot.api.methods.Walking;
import org.hexbot.api.methods.helper.Bank;
import org.hexbot.api.methods.interactable.GameObjects;
import org.hexbot.api.methods.interactable.Players;
import org.hexbot.api.methods.node.Inventory;
import org.hexbot.api.util.Time;
import org.hexbot.api.wrapper.Tile;
import org.hexbot.api.wrapper.interactable.GameObject;
import org.hexbot.api.wrapper.node.InventoryItem;
import org.hexbot.api.wrapper.node.Item;
import org.hexbot.core.concurrent.script.Condition;
import org.hexbot.core.concurrent.script.Worker;

public class BankItems extends Worker{

	public static final Tile TILE_CWARS_BANK = new Tile(2443,3083,0);
	public static final int ID_CWARS_CHEST = 4483;

	@Override
	public void run() {
		if(Camera.getPitch() > 150)
			Camera.setPitch(false);

		if(!MiscRunecrafter.areaBank.contains(Players.getLocal())){
			Walking.walk(MiscRunecrafter.tileBank);
			return;//try again
		}

		if(MiscRunecrafter.useDuelingRing && needDuelingRing()){
			if(replaceDuelingRing()){
				Time.sleep(400,600);
			}
			else{
				return;//try again
			}
		}

		if(validateInventory()){
			if(!Bank.isOpen()){
				MiscRunecrafter.banking = false;
				MiscRunecrafter.traversing = true;
				return;
			}
			else{
				if(Bank.close())
					Time.sleep(400,600);
				return;
			}
		}

		if(!Bank.isOpen()){
			if(openBank()){
				while(Players.getLocal().isMoving())
					Time.sleep(300);
				Time.sleep(400,600);
			}
			else
				return;//try again
		}

		List<InventoryItem> otherItems = getOtherItems();
		if(otherItems != null && otherItems.size() > 0){
			for(InventoryItem item : otherItems){
				if(Bank.deposit(item.getId(), item.getStackSize()))
					Time.sleep(1000,1200);
			}
		}

		for(final Item item : getMissingItems()){
			final int beforeCount = Inventory.getCount(item.getId());
			if(Bank.withdraw(item.getId(), item.getStackSize())){
				//wait for item to be withdrawn
				MiscRunecrafter.waitFor(new Condition(){
					@Override
					public boolean validate() {
						return Inventory.getCount(item.getId()) > beforeCount;
					}}, 3000);
			}
		}

		if(Util.isUsingPouches() && !Util.isPouchesFull(MiscRunecrafter.useSmallPouch, MiscRunecrafter.useMediumPouch, MiscRunecrafter.useLargePouch)){
			fillPouches();
		}
	}

	@Override
	public boolean validate() {
		return MiscRunecrafter.banking;
	}

	public static boolean validateInventory(){
		for(final Item item : MiscRunecrafter.inventoryItems){
			if(Inventory.getCount(item.getId()) < item.getStackSize())
				return false;
		}
		if(Util.isUsingPouches() && !Util.isPouchesFull(MiscRunecrafter.useSmallPouch, MiscRunecrafter.useMediumPouch, MiscRunecrafter.useLargePouch))
			return false;
		return true;
	}

	private static boolean openBank(){
		if(MiscRunecrafter.useDuelingRing){
			GameObject chest = GameObjects.getNearest(ID_CWARS_CHEST);
			if(chest == null)
				return false;
			if(Calculations.distanceTo(chest) > 3)
				Walking.walk(chest);
			Camera.turnTo(chest);
			return chest.interact("Use");
		}
		else
			return Bank.open();
	}

	private static List<Item> getMissingItems(){
		ArrayList<Item> result = new ArrayList<Item>();
		for(final Item item : MiscRunecrafter.inventoryItems){
			int currentCount = Inventory.getCount(item.getId());
			if(currentCount < item.getStackSize()){
				result.add(new Item(item.getId(), item.getStackSize() - currentCount));
			}
		}
		return result;
	}

	//get items that do not match inventoryItems
	private static List<InventoryItem> getOtherItems(){
		ArrayList<InventoryItem> result = new ArrayList<InventoryItem>();
		for(InventoryItem inventoryItem : Inventory.getAll()){
			boolean match = false;
			for(Item item2 : MiscRunecrafter.inventoryItems){
				if(inventoryItem.getId() == item2.getId())
					match = true;
			}
			if(!match){
				result.add(inventoryItem);
			}
		}
		return result;
	}

	private static boolean needDuelingRing(){
		if(MiscRunecrafter.runeType == Values.RuneType.FIRE_CWARS)
			return MiscRunecrafter.countDuelingRing <= 1;
		else 
			return MiscRunecrafter.countDuelingRing <= 0;
	}

	public static boolean replaceDuelingRing(){
		if(openBank())
			Time.sleep(400,600);
		if(Inventory.isFull() && Bank.depositAll())
			Time.sleep(400,600);
		int idRing = -1;
		int i = 0;
		//need at least dueling ring (2) for trip to duel arena and back to cwars
		//IDS_DUELING_RING[0] is dueling ring(1), index 2 is dueling ring(2) .. etc
		if(MiscRunecrafter.runeType == Values.RuneType.FIRE_CWARS)
			i = 1;
		for(i = 0; i < Values.IDS_DUELING_RING.length; i++){
			if(Bank.getItem(Values.IDS_DUELING_RING[i]) != null){
				idRing = Values.IDS_DUELING_RING[i];
				break;
			}
		}
		if(!Bank.withdraw(idRing, 1))
			return false;
		else
			Time.sleep(400,600);
		if(!Bank.close())
			return false;
		else
			Time.sleep(400,600);
		InventoryItem useRing = Inventory.getItem(idRing);
		if(useRing == null)
			return false;
		if(useRing.interact("Wear")){
			MiscRunecrafter.countDuelingRing = i + 1;
			return true;
		}
		else
			return false;
	}

	public static void fillPouches(){
		if(openBank())
			Time.sleep(400,600);
		//		else
		//			return false;
		if(Inventory.getCount(Values.ID_PURE_ESSENCE,Values.ID_RUNE_ESSENCE) == 0){
			if(MiscRunecrafter.usePureEssence && Bank.withdraw(Values.ID_PURE_ESSENCE, 0)){
				Time.sleep(400,600);
			}
			else if(MiscRunecrafter.useRuneEssence && Bank.withdraw(Values.ID_RUNE_ESSENCE, 0)){
				Time.sleep(400,600);
			}
			else
				return;
		}
		if(Bank.close())
			Time.sleep(700,900);
		if(MiscRunecrafter.useSmallPouch){
			InventoryItem smallPouch = Inventory.getItem(Values.ID_SMALL_POUCH);
			if(smallPouch != null && smallPouch.interact("Fill"))
				Time.sleep(700,800);
		}
		if(MiscRunecrafter.useMediumPouch){
			InventoryItem mediumPouch = Inventory.getItem(Values.ID_MEDIUM_POUCH);
			if(mediumPouch != null && mediumPouch.interact("Fill"))
				Time.sleep(700,800);
		}
		if(MiscRunecrafter.useLargePouch){
			InventoryItem largePouch = Inventory.getItem(Values.ID_LARGE_POUCH);
			InventoryItem largePouchDecayed1 = Inventory.getItem(Values.ID_LARGE_POUCH_DECAYED);
			if(largePouch != null && largePouch.interact("Fill"))
				Time.sleep(700,800);
			if(largePouchDecayed1 != null && largePouchDecayed1.interact("Fill"))
				Time.sleep(700,800);
		}
		if(MiscRunecrafter.useGiantPouch){
		}
	}

}
