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

package jsettlers.main.android.mainmenu.settings;

import jsettlers.main.android.core.AndroidPreferences;

/**
 * Created by tompr on 03/03/2017.
 */
public class SettingsPresenter {

	private final SettingsView view;
	private final AndroidPreferences androidPreferences;

	public SettingsPresenter(SettingsView view, AndroidPreferences androidPreferences) {
		this.view = view;
		this.androidPreferences = androidPreferences;
	}

	public void bindView() {
		view.setPlayerName(androidPreferences.getPlayerName());
		view.setServerAddress(androidPreferences.getServer());
		view.setPlayAllMusic(androidPreferences.isPlayAllMusic());
	}

	public void playerNameEdited(String playerName) {
		androidPreferences.setPlayerName(playerName);
		view.setPlayerName(playerName);
	}

	public void serverAddressEdited(String serverAddress) {
		androidPreferences.setServer(serverAddress);
		view.setServerAddress(serverAddress);
	}

	public void playAllMusicEdited(boolean playAll) {
		androidPreferences.setPlayAllMusic(playAll);
		view.setPlayAllMusic(playAll);
	}
}
