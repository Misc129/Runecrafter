package misc.scripts.runecrafter.workers;

import misc.scripts.runecrafter.MiscRunecrafter;
import misc.scripts.runecrafter.states.StateAirFalador;
import misc.scripts.runecrafter.states.StateFireCwars;
import misc.scripts.runecrafter.states.StateLawCwars;

import org.hexbot.api.methods.Camera;
import org.hexbot.api.methods.Game;
import org.hexbot.api.util.Time;
import org.hexbot.core.concurrent.script.Worker;

public class Traverse extends Worker{

	@Override
	public void run() {
		//System.out.println("Traverse");
		if(!Game.isRunning() && Game.getEnergy() > 70){
			Game.setRun(true);
			Time.sleep(1500);
		}
		switch(MiscRunecrafter.runeType){
		case AIR_FALADOR:
			StateAirFalador.traverse();
			break;
		case FIRE_CWARS:
			StateFireCwars.traverse();
			break;
		case LAW_CWARS:
			StateLawCwars.traverse();
			break;
		}

	}

	@Override
	public boolean validate() {
		return MiscRunecrafter.traversing;
	}

}
