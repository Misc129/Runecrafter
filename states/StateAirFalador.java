package misc.scripts.runecrafter.states;

import misc.scripts.runecrafter.MiscRunecrafter;
import misc.scripts.runecrafter.util.Util;
import misc.scripts.runecrafter.util.Values;

import org.hexbot.api.methods.Calculations;
import org.hexbot.api.methods.Camera;
import org.hexbot.api.methods.Walking;
import org.hexbot.api.methods.interactable.GameObjects;
import org.hexbot.api.methods.interactable.Players;
import org.hexbot.api.methods.node.Inventory;
import org.hexbot.api.util.Area;
import org.hexbot.api.util.Time;
import org.hexbot.api.wrapper.Tile;
import org.hexbot.api.wrapper.interactable.GameObject;
import org.hexbot.api.wrapper.node.InventoryItem;
import org.hexbot.api.wrapper.node.Item;
import org.hexbot.core.concurrent.script.Condition;

public enum StateAirFalador {

	TO_RUINS,//Vwest bank to ruins
	TO_ALTAR,//inside of area to the altar
	DO_RUNECRAFT,//runecraft the runes
	//TO_FALADOR,//altar to falador square (tele)
	EXIT_ALTAR,
	TO_BANK;//varrock square to bank

	public static final Area AREA_ALTAR = new Area(new Tile(2837,4840,0), new Tile(2850,4827,0));
	public static final Area AREA_BANK = new Area(new Tile(3009,3360,0),new Tile(3018,3355,0));
	public static final Area AREA_FALADOR = new Area();

	public static final Tile TILE_RUINS = new Tile(2987,3294,0);
	public static final Tile TILE_ALTAR = new Tile(2843,4832,0);
	public static final Tile TILE_BANK = new Tile(3014,3355,0);

	public static final Tile[] PATH_BANK_TO_RUINS = {TILE_RUINS,
		new Tile(2991,3299,0),
		new Tile(2994,3304,0),
		new Tile(2999,3313,0),
		new Tile(3005,3320,0),
		new Tile(3006,3330,0),
		new Tile(3007,3342,0),
		new Tile(3007,3351,0)
	};
	public static final Tile[] PATH_RUINS_TO_BANK = {
		TILE_BANK,
		new Tile(3007,3351,0),
		new Tile(3007,3342,0),
		new Tile(3006,3330,0),
		new Tile(3005,3320,0),
		new Tile(2999,3313,0),
		new Tile(2994,3304,0),
		new Tile(2991,3299,0)
	};

	public static final int ID_ALTAR = 24964;
	public static final int ID_RUINS = 26185;
	public static final int ID_PORTAL = 24951;

	public static void traverse(){
		if(Camera.getPitch() > 150)
			Camera.setPitch(false);
		switch (MiscRunecrafter.stateAirFalador){
		case TO_RUINS:{
			if(AREA_ALTAR.contains(Players.getLocal())){
				MiscRunecrafter.stateAirFalador = StateAirFalador.TO_ALTAR;
				break;
			}
			Util.stepPath(PATH_BANK_TO_RUINS);
			GameObject ruins = GameObjects.getNearest(ID_RUINS);
			if(ruins == null)
				break;
			if(ruins.getLocation().isOnMap()){
				Camera.turnTo(ruins);
			}
			if(ruins.isVisible()){
				ruins.click();
				Time.sleep(1000,1500);
			}
			break;
		}
		/* air altar is right next to entrance...not necassary? */
		case TO_ALTAR:
			GameObject altar1 = GameObjects.getNearest(ID_ALTAR);
			if(altar1 == null)
				break;
			if(altar1.isVisible() && Calculations.distanceTo(altar1) < 10){
				MiscRunecrafter.stateAirFalador = StateAirFalador.DO_RUNECRAFT;
				break;
			}
			Walking.walk(TILE_ALTAR);
			Camera.turnTo(altar1);
			//wait until player is close to altar tile
			MiscRunecrafter.waitFor(new Condition(){
				@Override
				public boolean validate() {
					return Calculations.distanceTo(TILE_ALTAR) < 5;
				}}, 5000);
			break;
		case DO_RUNECRAFT:
			//if inventory has no essence
			if(Inventory.getCount(Values.ID_RUNE_ESSENCE) == 0 && Inventory.getCount(Values.ID_PURE_ESSENCE) == 0){
				MiscRunecrafter.stateAirFalador = StateAirFalador.EXIT_ALTAR;
				break;
			}
			GameObject altar2 = GameObjects.getNearest(ID_ALTAR);
			if(altar2 == null)
				break;
			Camera.turnTo(altar2);
			if(altar2.isVisible() && altar2.interact("Craft-rune")){
				MiscRunecrafter.waitFor(new Condition(){
					@Override
					public boolean validate() {
						return Inventory.getCount(Values.ID_RUNE_ESSENCE) == 0 && Inventory.getCount(Values.ID_PURE_ESSENCE) == 0;
					}}, 4000);
				Time.sleep(1000,1500);//wait for runes to craft
			}
			else
				Camera.turnTo(altar2);
			break;
		case EXIT_ALTAR:
			if(!AREA_ALTAR.contains(Players.getLocal())){
				MiscRunecrafter.stateAirFalador = TO_BANK;
				break;
			}
			GameObject portal = GameObjects.getNearest(ID_PORTAL);
			if(portal == null)
				break;
			Camera.turnTo(portal);
			if(portal.isVisible() && portal.interact("Use")){
				//wait for player to exit portal
				MiscRunecrafter.waitFor(new Condition(){
					@Override
					public boolean validate() {
						return !AREA_ALTAR.contains(Players.getLocal());
					}}, 4000);
			}
			break;
		case TO_BANK:
			if(AREA_BANK.contains(Players.getLocal())){
				MiscRunecrafter.stateAirFalador = TO_RUINS;
				MiscRunecrafter.traversing = false;
				MiscRunecrafter.banking = true;
				break;
			}
			Util.stepPath(PATH_RUINS_TO_BANK);
			break;
		default:
			break;
		}
	}
}
