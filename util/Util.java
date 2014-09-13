package misc.scripts.runecrafter.util;

import misc.scripts.runecrafter.MiscRunecrafter;

import org.hexbot.api.methods.Menu;
import org.hexbot.api.methods.Settings;
import org.hexbot.api.methods.Skills;
import org.hexbot.api.methods.Walking;
import org.hexbot.api.methods.input.Mouse;
import org.hexbot.api.methods.node.Inventory;
import org.hexbot.api.util.Time;
import org.hexbot.api.wrapper.Locatable;
import org.hexbot.api.wrapper.Tile;
import org.hexbot.api.wrapper.node.InventoryItem;

public class Util {

	public static final int SETTING_SMALL_POUCH = 486;
	public static final int SETTING_MEDIUM_POUCH = 486;
	public static final int SETTING_LARGE_POUCH = 486;
	public static final int SETTING_GIANT_POUCH = 486;
	public static final int SETTING_POUCHES = 720;

	public static final int SETTING_SMALL_POUCH_VALUE = 2;
	public static final int SETTING_MEDIUM_POUCH_VALUE = 8;
	public static final int SETTING_LARGE_POUCH_VALUE = 32;

	public static boolean stepPath(Tile[] path){
		for(Tile tile : path){
			if(tile.isOnMap())
				return Walking.walk(tile);
		}
		return false;
	}

	public static boolean interact(Locatable l, String action){
		Mouse.click(l.getScreenLocation(), false);
		Time.sleep(700);
		return Menu.select(action);
	}
	
	public static boolean isUsingPouches(){
		return MiscRunecrafter.useSmallPouch 
				|| MiscRunecrafter.useMediumPouch 
				|| MiscRunecrafter.useLargePouch 
				|| MiscRunecrafter.useLargePouch;
	}

	public static boolean isPouchesFull(boolean useSmall, boolean useMedium, boolean useLarge){
		int value = 0;
		if(useSmall) value += SETTING_SMALL_POUCH_VALUE;
		if(useMedium) value += SETTING_MEDIUM_POUCH_VALUE;
		if(useLarge) value += SETTING_LARGE_POUCH_VALUE;
		return Settings.getAt(SETTING_POUCHES) == value;
	}

	public static boolean isPouchesEmpty(){
		return Settings.getAt(SETTING_POUCHES) == 0;
	}

	public static void emptyPouches(){
		//pouch may be in bank with essence, so try only 5 times to prevent infinite loop
		int i = 0;
		while(!isPouchesEmpty() && i < 5){
			InventoryItem smallPouch = Inventory.getItem(Values.ID_SMALL_POUCH);
			InventoryItem mediumPouch = Inventory.getItem(Values.ID_MEDIUM_POUCH);
			InventoryItem largePouch = Inventory.getItem(Values.ID_LARGE_POUCH);
			InventoryItem largePouchDecayed1 = Inventory.getItem(Values.ID_LARGE_POUCH_DECAYED);
			InventoryItem giantPouch = Inventory.getItem(Values.ID_GIANT_POUCH);
			if(smallPouch != null && smallPouch.interact("Empty")){
				Time.sleep(1000);
			}
			if(mediumPouch != null && mediumPouch.interact("Empty")){
				Time.sleep(1000);
			}
			if(largePouch != null && largePouch.interact("Empty")){
				Time.sleep(1000);
			}
			if(largePouchDecayed1 != null && largePouchDecayed1.interact("Empty")){
				Time.sleep(1000);
			}
			if(giantPouch != null && giantPouch.interact("Empty")){
				Time.sleep(1000);
			}
			i++;
		}
	}
	
	public static int getRuneCount(){
		int result = 0;
		switch(MiscRunecrafter.runeType){
		case AIR_FALADOR:
			return (int)(MiscRunecrafter.gainedExp / Values.XPRATE_AIR);
		case FIRE_CWARS:
			return (int)(MiscRunecrafter.gainedExp / Values.XPRATE_FIRE);
		case LAW_CWARS:
			return (int)(MiscRunecrafter.gainedExp / Values.XPRATE_LAW);
		}
		return 0;
	}

}
