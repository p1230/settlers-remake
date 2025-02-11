/*
 * Copyright (c) 2017
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package jsettlers.main.android.core.controls;

import java.util.Timer;
import java.util.TimerTask;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.widget.Toast;

import go.graphics.android.sound.AndroidSoundPlayer;
import java.util.function.Consumer;
import jsettlers.common.action.Action;
import jsettlers.common.action.EActionType;
import jsettlers.common.action.SetSpeedAction;
import jsettlers.common.menu.IStartedGame;
import jsettlers.main.android.R;
import jsettlers.main.android.gameplay.gamemenu.GameSpeedLiveData;

/**
 * GameMenu is a singleton within the scope of a started game
 */
public class GameMenu implements Consumer<IStartedGame> {
	public enum GameState {
		PLAYING,
		CONFIRM_QUIT,
		QUITTED
	}

	private final Context context;
	private final AndroidSoundPlayer soundPlayer;
	private final ActionControls actionControls;
	private final MutableLiveData<GameState> gameState = new MutableLiveData<>();
	private final MutableLiveData<Boolean> pausedState = new MutableLiveData<>();
	private final GameSpeedLiveData gameSpeedLiveData;
	private final boolean isMultiplayer;

	private Timer quitConfirmTimer;

	public GameMenu(
			Context context,
			AndroidSoundPlayer soundPlayer,
			ActionControls actionFireable,
			GameSpeedLiveData gameSpeedLiveData,
			boolean isMultiplayer) {
		this.context = context;
		this.soundPlayer = soundPlayer;
		this.actionControls = actionFireable;
		this.gameSpeedLiveData = gameSpeedLiveData;
		this.isMultiplayer = isMultiplayer;

		pausedState.postValue(false);
		gameState.postValue(GameState.PLAYING);
	}

	public LiveData<GameState> getGameState() {
		return gameState;
	}

	public LiveData<Boolean> isPausedState() {
		return pausedState;
	}

	public LiveData<Float> getGameSpeed() {
		return gameSpeedLiveData;
	}

	public void save() {
		actionControls.fireAction(new Action(EActionType.SAVE));
		Toast.makeText(context, R.string.game_menu_saved, Toast.LENGTH_SHORT).show();
	}

	// mute the game when pausing whether or not its currently visible
	public void pause() {
		actionControls.fireAction(new Action(EActionType.SPEED_SET_PAUSE));
		mute();

		pausedState.postValue(true);
	}

	// don't unmute here, MapFragment will unmute if its visible.
	public void unPause() {
		actionControls.fireAction(new Action(EActionType.SPEED_UNSET_PAUSE));

		pausedState.postValue(false);
	}

	public void quit() {
		quitConfirmTimer = new Timer();

		quitConfirmTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (quitConfirmTimer != null) {
					quitConfirmTimer = null;

					gameState.postValue(GameState.PLAYING);
				}
			}
		}, 3000);

		gameState.postValue(GameState.CONFIRM_QUIT);
	}

	public void quitConfirm() {
		// Trigger quit from here and callback in MainApplication broadcasts after quit is complete
		quitConfirmTimer = null;
		actionControls.fireAction(new Action(EActionType.EXIT));
	}

	public void mute() {
		soundPlayer.setPaused(true);
	}

	public void unMute() {
		soundPlayer.setPaused(false);
	}

	public void skipMinute() {
		actionControls.fireAction(new Action(EActionType.FAST_FORWARD));
	}

	public void setGameSpeed(float speed) {
		actionControls.fireAction(new SetSpeedAction(speed));
		gameSpeedLiveData.setValue(speed);
	}

	public boolean isMultiplayer() {
		return isMultiplayer;
	}

	/**
	 * IGameExitedListener implementation
	 */
	@Override
	public void accept(IStartedGame game) {
		gameState.postValue(GameState.QUITTED);
	}
}
