package misc.scripts.runecrafter.states;

import java.awt.Point;

import misc.scripts.runecrafter.MiscRunecrafter;
import misc.scripts.runecrafter.util.Util;
import misc.scripts.runecrafter.util.Values;

import org.hexbot.api.methods.Calculations;
import org.hexbot.api.methods.Camera;
import org.hexbot.api.methods.Walking;
import org.hexbot.api.methods.helper.Bank;
import org.hexbot.api.methods.helper.ChatOptions;
import org.hexbot.api.methods.helper.Equipment;
import org.hexbot.api.methods.helper.Tab;
import org.hexbot.api.methods.input.Mouse;
import org.hexbot.api.methods.interactable.GameObjects;
import org.hexbot.api.methods.interactable.Players;
import org.hexbot.api.methods.node.Inventory;
import org.hexbot.api.util.Area;
import org.hexbot.api.util.Time;
import org.hexbot.api.wrapper.Tile;
import org.hexbot.api.wrapper.interactable.GameObject;
import org.hexbot.core.concurrent.script.Condition;

public enum StateFireCwars {

	TO_DUELARENA,
	TO_RUINS,
	TO_ALTAR,
	DO_RUNECRAFT,
	TO_CWARS;

	public static final Tile TILE_RUINS = new Tile(3312,3253,0);
	public static final Tile TILE_DUEL_ARENA_GATE = new Tile(3311,3235,0);
	public static final Tile TILE_ALTAR = new Tile(2583,4840,0);
	public static final Tile TILE_CWARS_BANK = new Tile(2443,3083,0);

	public static final Tile[] PATH_GATE_TO_RUINS = {
		TILE_RUINS,
		new Tile(3310,3249,0),
		new Tile(3309,3240,0)
	};

	public static final Area AREA_CWARS_BANK = new Area(new Tile(2436,3098,0),new Tile(2444,3082,0));
	public static final Area AREA_DUEL_ARENA = new Area(new Tile(3305,3246,0),new Tile(3328,3224,0));
	public static final Area AREA_ALTAR = new Area(new Tile(2568,4855,0), new Tile(2600,4820,0));
	
	public static final int CAMERAANGLE_DUELARENA_GATE = 75;
	public static final int CAMERAANGLE_RUINS = 340;

	public static final int ID_RUINS = 26189;
	public static final int ID_ALTAR = 24968;
	public static final int ID_CWARS_CHEST = 4483;

	public static final Point POINT_OPTION_TELE_DUELARENA = new Point(260,393);

	public static void traverse(){
		if(Camera.getPitch() > 150)
			Camera.setPitch(false);
		switch(MiscRunecrafter.stateFireCwars){
		case TO_DUELARENA:
			if(AREA_DUEL_ARENA.contains(Players.getLocal())){
				MiscRunecrafter.stateFireCwars = TO_RUINS;
				--MiscRunecrafter.countDuelingRing;
				break;
			}
			Bank.close();
			Tab.open(Tab.EQUIPMENT);
			Time.sleep(700,900);
			if(Equipment.getSlotWidget(Equipment.Slot.RING).interact("Operate")){
				Time.sleep(1200,1400);
				if(!AREA_DUEL_ARENA.contains(Players.getLocal())){//redundant check 
					Mouse.click(POINT_OPTION_TELE_DUELARENA);
					MiscRunecrafter.waitFor(new Condition(){
						@Override
						public boolean validate() {
							return AREA_DUEL_ARENA.contains(Players.getLocal());
						}}, 4000);
					Time.sleep(1300,1500);
				}
			}
			//			if(ChatOptions.canSelectAnOption()){
			//				//ChatOptions.getWithText("Al Kharid Duel Arena.").click();
			//				Mouse.click(POINT_OPTION_TELE_DUELARENA);
			//				MiscRunecrafter.waitFor(new Condition(){
			//					@Override
			//					public boolean validate() {
			//						return AREA_DUEL_ARENA.contains(Players.getLocal());
			//					}}, 5000);
			//				Time.sleep(800,900);
			//			}
			break;
		case TO_RUINS:
			if(AREA_ALTAR.contains(Players.getLocal())){
				MiscRunecrafter.stateFireCwars = TO_ALTAR;
				break;
			}
			//if player is outside of gate
			if(Players.getLocal().getLocation().getX() > 3311 && Players.getLocal().getLocation().getY() < 3239){
				//the gate has no ID, so turn to it and click on the tile behind it
				Camera.setCameraRotation(CAMERAANGLE_DUELARENA_GATE);
				TILE_DUEL_ARENA_GATE.click();
				Time.sleep(400,600);
				break;
			}
			if(Calculations.distanceTo(TILE_RUINS) > 7 && Util.stepPath(PATH_GATE_TO_RUINS)){
				Camera.setCameraRotation(CAMERAANGLE_RUINS);
				Time.sleep(400,600);
			}
			GameObject ruins = GameObjects.getNearest(ID_RUINS);
			if(ruins == null)
				break;
			if(ruins.isVisible()){
				ruins.click();
				Time.sleep(1000,1500);
			}
			else
				Camera.turnTo(ruins);
			break;
		case TO_ALTAR:
			GameObject altar1 = GameObjects.getNearest(ID_ALTAR);
			if(altar1 == null)
				break;
			if(altar1.isVisible() && Calculations.distanceTo(altar1) < 10){
				MiscRunecrafter.stateFireCwars = DO_RUNECRAFT;
				break;
			}
			Walking.walk(TILE_ALTAR);
			Camera.turnTo(altar1);
			//wait until player is close to altar tile
			MiscRunecrafter.waitFor(new Condition(){
				@Override
				public boolean validate() {
					return Calculations.distanceTo(TILE_ALTAR) < 5;
				}}, 3000);
			break;
		case DO_RUNECRAFT:
			//if inventory has no essence
			if(Inventory.getCount(Values.ID_RUNE_ESSENCE) == 0 && Inventory.getCount(Values.ID_PURE_ESSENCE) == 0){
				MiscRunecrafter.stateFireCwars = TO_CWARS;
				break;
			}
			GameObject altar2 = GameObjects.getNearest(ID_ALTAR);
			if(altar2 == null)
				break;
			if(altar2.isVisible() && altar2.interact("Craft-rune")){
				MiscRunecrafter.waitFor(new Condition(){
					@Override
					public boolean validate() {
						return Inventory.getCount(Values.ID_RUNE_ESSENCE) == 0 && Inventory.getCount(Values.ID_PURE_ESSENCE) == 0;
					}}, 4000);
				Time.sleep(1000,1500);//wait for runes to craft
			}
			else{
				Walking.walk(altar2);
				Camera.turnTo(altar2);
			}
			break;
		case TO_CWARS:
			GameObject chest = GameObjects.getNearest(ID_CWARS_CHEST);
			if(chest != null && Calculations.distanceTo(chest) < 50){
				if(Calculations.distanceTo(chest) < 3){
					MiscRunecrafter.stateFireCwars = TO_DUELARENA;
					MiscRunecrafter.traversing = false;
					MiscRunecrafter.banking = true;
					--MiscRunecrafter.countDuelingRing;
					break;
				}
				else{
					Camera.turnTo(chest);
					Walking.walk(TILE_CWARS_BANK);
					break;
				}
			}
			Tab.open(Tab.EQUIPMENT);
			Time.sleep(700,900);
			if(Equipment.getSlotWidget(Equipment.Slot.RING).interact("Operate")){
				Time.sleep(1000,1200);
				if(!AREA_CWARS_BANK.contains(Players.getLocal())){
					ChatOptions.getWithText("Castle Wars Arena.").click();
					MiscRunecrafter.waitFor(new Condition(){
						@Override
						public boolean validate() {
							return AREA_CWARS_BANK.contains(Players.getLocal());
						}}, 5000);
					Time.sleep(1000, 1500);
				}
			}
			break;
		}
	}

}
