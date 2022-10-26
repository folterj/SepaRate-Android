package info.joostdefolter.separate;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class GameStore {
	SharedPreferences prefs;
	
	int hiscore;
	int hilevel;
	
	GameStore(Activity activity) {
		prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		hiscore = prefs.getInt("hiscore", 0);
		hilevel = prefs.getInt("hilevel", 0);
	}
	
	void setHiscore(int hiscore) {
		this.hiscore = hiscore;
		SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("hiscore", hiscore);
        editor.apply();
	}
	
	void setHilevel(int hilevel) {
		this.hilevel = hilevel;
		SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("hilevel", hilevel);
        editor.apply();
	}
}
