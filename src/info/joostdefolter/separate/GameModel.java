package info.joostdefolter.separate;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import android.graphics.RectF;

public class GameModel {
	GameStore gameStore;
	ArrayList<GameObserver> gameObservers = new ArrayList<GameObserver>();
	ArrayList<ControlObserver> controlObservers = new ArrayList<ControlObserver>();
	
	UITimer moveTimer;
	UITimer displayTimer;
	
	Random rnd = new Random();

	boolean liqmoveSoundOk;
	boolean liqoverflowSoundOk;
	boolean startSoundOk;
	boolean gameoverSoundOk;
	Valve valve;
	Cont feed;
	ArrayList<Cont> cont;
	Vector<LevelPurity> levelPurities = new Vector<LevelPurity>();
	int maxCont;
	Cont curcont;
	int totcomps;
	GameMode gameMode = GameMode.Idle;
	int displayn = 0;
	int score = 0;
	int extrascore = 0;
	int deltascore = 0;
	int level = 0;
	int hiscore = 0;
	int hilevel = 0;
	float objRedPurity, objYelPurity, objBluePurity;
	float redPurity, yelPurity, bluePurity;
	boolean recentObjRed, recentObjYel, recentObjBlue;
	int moveInterval;
	int defMoveInterval = 2000;
	boolean pauseMode = false;
	int demoCont = -1;
	int guideCont = -1;
	
	boolean moveTimerState = false;
	boolean displayTimerState = false;
	
	GameModel() {
		float x;

		moveTimer = new UITimer(moveTimer_Timeout, moveInterval, true);
		displayTimer = new UITimer(displayTimer_Timeout, 5000, true);
		
		maxCont = CompCode.Size.getValue()-1;
		cont = new ArrayList<Cont>(maxCont);
		feed = new Cont(new RectF(0.45f, 0.0f, 0.55f, 0.4f), maxCont + 2, true);
		for (int i = 0; i < maxCont; i++) {
			x = 0.1f + i * 0.2f;
			cont.add(new Cont(new RectF(x, 0.6f, x + 0.18f, 1), 10, false));
		}
		valve = new Valve(new RectF(0.1f, 0.4f, 0.9f, 0.6f), maxCont);
	}
	
	void init(GameStore gameStore) {
		this.gameStore = gameStore;
		hiscore = gameStore.hiscore;
		hilevel = gameStore.hilevel;
		setGameMode(GameMode.Splash);
	}

	void reset() {
		moveInterval = defMoveInterval;
		moveTimer.setInterval(moveInterval);
		setValvePos(0);
		levelPurities.clear();
		if (gameMode == GameMode.StartPlay)
			level = 1;
		else
			level = 0;
		score = 0;
		extrascore = 0;
		initLevel();
	}

	void initLevel() {
		feed.clear();
		for (int i = 0; i < cont.size(); i++) {
			cont.get(i).reset();
		}
		guideCont = -1;
		totcomps = 1;
		redPurity = 0;
		yelPurity = 0;
		bluePurity = 0;
		setObjectives();
	}

	void nextLevel() {
		extrascore += score;
		extrascore += (level * 10000);
		score = 0;
		level++;
		initLevel();
		setObjectives();
	}
	
	void setLevel(int level) {
		this.level = level;
	}
	
	int getValvePos() {
		return valve.pos;
	}
	
	void setValvePos(int pos) {
		valve.pos = pos;
		curcont = cont.get(pos);
	}

	void setObjectives() {
		if (level < 0)
			level = 0;
		if (level == 0) {
			objRedPurity = 0.2f;
			objYelPurity = 0;
			objBluePurity = 0.2f;
		} else {
			objRedPurity = 0.5f + (float) (level - 1) / 10;
			if (objRedPurity > 1)
				objRedPurity = 1;
			objYelPurity = 0.5f + (float) (level - 1) / 10;
			if (objYelPurity > 1)
				objYelPurity = 1;
			objBluePurity = 0.5f + (float) (level - 1) / 10;
			if (objBluePurity > 1)
				objBluePurity = 1;
		}
		moveInterval = (int)(defMoveInterval / (1 + (float)(level - 1) / 5));
		moveTimer.setInterval(moveInterval);
	}

	void startGameTimers() {
		moveTimer.reset();
		displayTimer.reset();
	}

	void stopGameTimers() {
		moveTimer.stop();
		displayTimer.stop();
	}
	
	void togglePause() {
		// game pause
		if (gameMode == GameMode.Play) {
			// only while playing
			pauseMode = !pauseMode;
			if (pauseMode) {
				stopGameTimers();
			} else {
				startGameTimers();
			}
		}
	}
	
	void pauseApp() {
		moveTimerState = moveTimer.isEnabled();
		displayTimerState = displayTimer.isEnabled();
		stopGameTimers();
	}
	
	void resumeApp() {
		if (moveTimerState)
			moveTimer.start();
		if (displayTimerState)
			displayTimer.start();
	}

	void setGameMode(GameMode gameMode0) {
		gameMode = gameMode0;

		if (gameMode == GameMode.StartPlay || gameMode == GameMode.StartDemo) {
			if (gameMode == GameMode.StartPlay) {
				setActiveControl(true);
			}
			reset();
		}
		
		if (gameMode == GameMode.Brief) {
			recentObjRed = true;
			recentObjYel = true;
			recentObjBlue = true;
			playSound(Sounds.Start);
		}

		if (gameMode == GameMode.StartPlay) {
			gameMode = GameMode.SelectLevel;
			setValvePos(1);
		} else if (gameMode == GameMode.StartDemo) {
			gameMode = GameMode.Demo;
		}

		if (gameMode == GameMode.Brief || gameMode == GameMode.Demo) {
			startGameTimers();
		} else if (gameMode == GameMode.SelectLevel
				|| gameMode == GameMode.NextLevel
				|| gameMode == GameMode.GameOver || gameMode == GameMode.Idle) {
			stopGameTimers();
		}

		if (gameMode == GameMode.GameOver) {
			setActiveControl(false);
			storeLevelPurity(false);
			if (getTotScore() > hiscore && level > 0) {
				hiscore = getTotScore();
				hilevel = level;

				gameStore.setHiscore(hiscore);
				gameStore.setHilevel(hilevel);
				
				gameMode = GameMode.GameOver_Hiscore;
			}
			playSound(Sounds.GameOver);
		}
		
		displayTimer.reset();
		displayn = 0;
		updateView();
	}

	void updateGameMode() {
		switch (gameMode) {
		case Splash:
			setGameMode(GameMode.StartDemo);
			break;
		case NextLevel:
			nextLevel();
			setGameMode(GameMode.Brief);
			break;
		case Brief:
			recentObjRed = false;
			recentObjYel = false;
			recentObjBlue = false;
			setGameMode(GameMode.Play);
			break;
		case GameOver:
		case GameOver_Hiscore:
			setGameMode(GameMode.Debrief);
			break;
		case Debrief:
		case Idle:
			setGameMode(GameMode.StartDemo);
			break;
		default:
			break;
		}
	}

	private Runnable moveTimer_Timeout = new Runnable() {
		public void run() {
			Comps comps = new Comps();
			CompCode code = CompCode.Empty;
			int demoi;
			int demoDx;
			int mincomps;
			int maxcomps = 0;
			boolean stopRed = false;
			boolean stopYellow = false;
			boolean stopBlue = false;
			boolean stopBlack = false;

			for (int i = 0; i < maxCont; i++) {
				cont.get(i).resetDisplay();
				if (cont.get(i).isFull()) {
					switch (cont.get(i).purityComp) {
						case Red:		stopRed = true;		break;
						case Yellow:	stopYellow = true;	break;
						case Blue:		stopBlue = true;	break;
						case Black:		stopBlack = true;	break;
					}
				}
			}
			
			if (!stopRed) maxcomps++;
			if (!stopYellow) maxcomps++;
			if (!stopBlue) maxcomps++;
			if (!stopBlack) maxcomps++;

			if (totcomps <= 0) {
				totcomps = 0;
				mincomps = 2;
				if (mincomps > maxcomps) {
					mincomps = maxcomps;
				}
				while (totcomps < mincomps) {
					totcomps = 0;
					comps.clear();
					if (rnd.nextDouble() < 0.3 && !stopRed) {
						comps.add(CompCode.Red);
						totcomps++;
					}
					if (rnd.nextDouble() < 0.3 && !stopYellow) {
						comps.add(CompCode.Yellow);
						totcomps++;
					}
					if (rnd.nextDouble() < 0.3 && !stopBlue) {
						comps.add(CompCode.Blue);
						totcomps++;
					}
					if (rnd.nextDouble() < 0.1 && !stopBlack) {
						comps.add(CompCode.Black);
						totcomps++;
					}
				}
			} else {
				totcomps--;
			}
			feed.addComp(comps);

			// move comp from feed to curcont
			if (feed.checkMove(curcont)) {
				if (gameMode != GameMode.Demo && gameMode != GameMode.Idle) {
					
					playSound(Sounds.LiqMove);
					updateScore();
					checkObjectives();
				}
				if (curcont.isOverFull()) {
					if (gameMode != GameMode.Demo && gameMode != GameMode.Idle) {
						playSound(Sounds.LiqOverflow);
						setGameMode(GameMode.GameOver);
					} else {
						setGameMode(GameMode.Idle);
					}
				}
			}

			if (gameMode == GameMode.Demo || level == 0) {
				// demo/help movement
				demoCont = 1;
				for (int i = 0; i < feed.size(); i++) {
					if (feed.get(i).size() > 0) {
						code = feed.get(i).get(0).code;
						break;
					}
				}
				if (code != CompCode.Empty) {
					demoi = 0;
					// find best cont
					while (demoi < maxCont - 1
							&& cont.get(demoi).purityComp != code
							&& cont.get(demoi).purityComp != CompCode.Empty) {
						demoi++;
					}
					demoCont = demoi;
					guideCont = demoi;
				}
				// only move valve one position per comp move
				demoDx = (int)Math.signum(demoCont - valve.pos);
				// if cont is full
				if (cont.get(valve.pos + demoDx).isFull()) {
					if (demoDx != 0)
						demoDx = 0;
					else if (valve.pos < maxCont - 1)
						demoDx = 1;
					else
						demoDx = -1;
				}
				if (gameMode == GameMode.Demo && demoDx != 0) {
					setValvePos(valve.pos + demoDx);
				}
			}
			updateView();
		}
	};

	private Runnable displayTimer_Timeout = new Runnable() {
		public void run() {
			displayn++;
			updateView();
		}
	};

	int getTotScore() {
		return score + extrascore;
	}

	void updateScore() {
		if (gameMode != GameMode.Play && gameMode != GameMode.NextLevel)
			return;

		int purity;
		int newscore = 0;
		for (int i = 0; i < maxCont; i++) {
			purity = (int) Math.round(cont.get(i).purity * 100);
			newscore += (purity * 100);
			if (cont.get(i).purity >= 1)
				newscore += 10000;
		}
		deltascore = newscore - score;
		if (deltascore != 0)
			curcont.setDScore(deltascore);
		score = newscore;
	}

	void storeLevelPurity(boolean levelCompleted) {
		LevelPurity levelPurity = new LevelPurity();

		levelPurity.level = level;
		levelPurity.totScore = score;
		if (levelCompleted) {
			levelPurity.totScore += (level * 10000);
		}
		for (int i = 0; i < cont.size(); i++) {
			switch (cont.get(i).purityComp) {
				case Red:		levelPurity.totRedPurity += cont.get(i).purity;		break;
				case Yellow:	levelPurity.totYelPurity += cont.get(i).purity;		break;
				case Blue:		levelPurity.totBluePurity += cont.get(i).purity;	break;
				default:	break;
			}
			if (cont.get(i).purity >= 1) {
				levelPurity.totScore += 10000;
			}
		}
		levelPurity.completed = levelCompleted;
		levelPurities.add(levelPurity);
	}

	void checkObjectives() {
		float purity;
		float redPurity0 = redPurity;
		float yelPurity0 = yelPurity;
		float bluePurity0 = bluePurity;
		
		redPurity = 0;
		yelPurity = 0;
		bluePurity = 0;
		for (int i = 0; i < maxCont; i++) {
			purity = cont.get(i).purity;
			switch (cont.get(i).purityComp) {
				case Red:		redPurity += purity;	break;
				case Yellow:	yelPurity += purity;	break;
				case Blue:		bluePurity += purity;	break;
				default:	break;
			}
		}
		if (redPurity >= objRedPurity && yelPurity >= objYelPurity && bluePurity >= objBluePurity) {
			storeLevelPurity(true);
			setGameMode(GameMode.NextLevel);
		}
		recentObjRed = (redPurity0 < objRedPurity && redPurity >= objRedPurity);
		recentObjYel = (yelPurity0 < objYelPurity && yelPurity >= objYelPurity);
		recentObjBlue = (bluePurity0 < objBluePurity && bluePurity >= objBluePurity);
		if (recentObjRed || recentObjYel || recentObjBlue) {
			displayTimer.reset();
		}
	}

	void registerGameObserver(GameObserver observer) {
		gameObservers.add(observer);
	}

	void unregisterGameObserver(GameObserver observer) {
		gameObservers.remove(observer);
	}
	
	void updateView() {
		for(GameObserver observer : gameObservers) {
			observer.update();
		}
	}

	void playSound(Sounds sound) {
		for(GameObserver observer : gameObservers) {
			observer.playSound(sound);
		}
	}

	void registerControlObserver(ControlObserver observer) {
		controlObservers.add(observer);
	}

	void unregisterControlObserver(ControlObserver observer) {
		controlObservers.remove(observer);
	}
	
	void setActiveControl(boolean set) {
		for(ControlObserver observer : controlObservers) {
			observer.setActiveControl(set);
		}
	}
}
