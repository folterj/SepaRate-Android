package info.joostdefolter.separate;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Handler;
import android.text.util.Linkify;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.TextView;

public class GameController implements ControlObserver {
	GameModel model;
	GameView view;
	MainGame activity;
	
	Dialog aboutDialog;
	GameStore gameStore;

	boolean activeControl = false;
	boolean aboutActive = false;
	
	boolean appPaused = false;
	
	GameController(GameModel model, DrawView drawView, MainGame activity) {
		this.model = model;
		this.activity = activity;
		
		gameStore = new GameStore(activity);
		
		view = new GameView(this, model, drawView, activity);
		
		model.registerControlObserver(this);
		model.init(gameStore);
		
		initAbout();
		showAbout(false);
		final Handler handler = new Handler();
	    handler.postDelayed(new Runnable() {
	      @Override
	      public void run() {
	    	  closeSplash();
	      }
	    }, 5000);
	}
	
	public void closeSplash() {
		aboutDialog.dismiss();
		model.setGameMode(GameMode.StartDemo);
	}
	
	public void setActiveControl(boolean set) {
		activeControl = set;
	}
	
	void touchEvent(float x, float y) {
		boolean updateView = false;
		boolean touchTop = (y < view.screensize.y / 2);
		boolean touchBottom = (y >= view.screensize.y / 2);
		int newValvePos = -1;
		
		if (touchBottom) {
			newValvePos = (int)(x / view.screensize.x * 4);
		}
		if (activeControl && !model.pauseMode) {
			if (newValvePos >= 0 && newValvePos != model.getValvePos()) {
				model.setValvePos(newValvePos);
				updateView = true;
			}
		}
		if (touchBottom && (model.gameMode == GameMode.Demo || model.gameMode == GameMode.Idle)) {
			// start game
			model.setGameMode(GameMode.StartPlay);
		}
		if (model.gameMode == GameMode.SelectLevel) {
			// level selection
			if (newValvePos >= 0) {
				// select
				model.setLevel(model.getValvePos() * 2 - 1);
				model.setObjectives();
			} else if (touchTop) {
				// start
				model.initLevel();
				model.setGameMode(GameMode.Brief);
			}
		}
		if (updateView) {
			view.update();
		}
	}
	
	void newGame() {
		model.setGameMode(GameMode.StartPlay);
	}
	
	void togglePauseGame() {
		model.togglePause();
		if (model.pauseMode) {
			setMenuResume();
		} else {
			setMenuPause();
		}
	}
	
	void setMenuResume() {
		MenuItem menuItem = activity.menu.findItem(R.id.game_pause);
		menuItem.setTitle(R.string.game_resume);
		menuItem.setIcon(R.drawable.ic_menu_game_resume);
	}
	
	void setMenuPause() {
		MenuItem menuItem = activity.menu.findItem(R.id.game_pause);
		menuItem.setTitle(R.string.game_pause);
		menuItem.setIcon(R.drawable.ic_menu_game_pause);
	}

	void pauseApp() {
		if (!appPaused) {
			appPaused = true;
			view.setRedrawMode(false);
			model.pauseApp();
		}
	}
	
	void resumeApp() {
		if (appPaused && !aboutActive) {
			appPaused = false;
			view.setRedrawMode(true);
			model.resumeApp();
		}
	}
	
	void initAbout() {
		aboutDialog = new Dialog(activity);
		aboutDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		aboutDialog.setContentView(R.layout.about);	
	}

	void showAbout(boolean cancelable) {
		pauseApp();
		aboutDialog.setCancelable(cancelable);
		aboutDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				aboutActive = false;
				resumeApp();
			}
		});
		aboutDialog.show();
		TextView aboutLink = (TextView)aboutDialog.findViewById(R.id.aboutLink);
		Linkify.addLinks(aboutLink, Linkify.ALL);
		aboutActive = true;
	}
}
