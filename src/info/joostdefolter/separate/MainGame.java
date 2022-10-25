package info.joostdefolter.separate;

import info.joostdefolter.separate.DrawView;
import info.joostdefolter.separate.R;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

public class MainGame extends Activity {
	private DrawView drawView;
	Menu menu;
	GameModel model;
	GameController controller;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_game);
		
		drawView = (DrawView)findViewById(R.id.mainview);
		
		model = new GameModel();
		controller = new GameController(model, drawView, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main_game, menu);
		this.menu = menu;
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.game_new:
	        	controller.newGame();
	            return true;
	        case R.id.game_pause:
	        	controller.togglePauseGame();
	            return true;
	        case R.id.about:
	        	controller.showAbout(true);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public void onDestroy() {
		controller.pauseApp();
		super.onDestroy();		
	}
	
	@Override
	public void onPause() {
		controller.pauseApp();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		controller.resumeApp();
	}
}
