package jsettlers.main.android.mainmenu.gamesetup;

import android.app.Activity;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import jsettlers.common.menu.IJoinPhaseMultiplayerGameConnector;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.main.android.core.AndroidPreferences;
import jsettlers.main.android.core.GameStarter;

/**
 * Created by Tom Pratt on 07/10/2017.
 */

public class JoinMultiPlayerSetupViewModel extends MultiPlayerSetupViewModel {

	public JoinMultiPlayerSetupViewModel(GameStarter gameStarter, AndroidPreferences androidPreferences, IJoinPhaseMultiplayerGameConnector connector, MapLoader mapLoader) {
		super(gameStarter, androidPreferences, connector, mapLoader);

		setAllPlayerSlotsEnabled(false);
	}

	/**
	 * ViewModel factory
	 */
	public static class Factory implements ViewModelProvider.Factory {

		private final Activity activity;
		private final String mapId;

		public Factory(Activity activity, String mapId) {
			this.activity = activity;
			this.mapId = mapId;
		}

		@Override
		public <T extends ViewModel> T create(Class<T> modelClass) {
			GameStarter gameStarter = (GameStarter) activity.getApplication();
			MapLoader mapLoader = gameStarter.getMapList().getMapById(mapId);
			IJoinPhaseMultiplayerGameConnector joinPhaseMultiplayerGameConnector = gameStarter.getJoinPhaseMultiplayerConnector();

			if (joinPhaseMultiplayerGameConnector == null) {
				throw new MultiPlayerConnectorUnavailableException();
			}

			if (modelClass == JoinMultiPlayerSetupViewModel.class) {
				return (T) new JoinMultiPlayerSetupViewModel(gameStarter, new AndroidPreferences(activity), joinPhaseMultiplayerGameConnector, mapLoader);
			}
			throw new RuntimeException("JoinMultiPlayerSetupViewModel.Factory doesn't know how to create a: " + modelClass.toString());
		}
	}
}
