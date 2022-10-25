package info.joostdefolter.separate;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.media.AudioManager;
import android.media.SoundPool;

public class GameView implements GameObserver {
	GameModel model;
	GameController controller;
	
	DrawView drawView;
	UITimer redrawTimer;
	int redrawCounter = 0;
	
	SoundPool soundPool;
	int liqMoveSound;
	int liqOverflowSound;
	int startSound;
	int gameOverSound;
	
	boolean flashon = false;

	Point screensize = new Point();
	
	Paint linePaint = new Paint();
	Paint thinlinePaint = new Paint();
	Paint aalinePaint = new Paint();
	Paint fxlinePaint = new Paint();
	Paint fillPaint = new Paint();
	Paint textPaint = new Paint();
	Paint smalltextPaint = new Paint();
	Paint centertextPaint = new Paint();
	Paint largetextPaint = new Paint();
	Paint hugetextPaint = new Paint();

	RectF scoreRect, levelRect, textRect;
	RectF objRedRect, objYelRect, objBlueRect;

	RectF rect = new RectF();
	RectF rrect = new RectF();
	RectF rect0 = new RectF();
	RectF rrect0 = new RectF();
	Rect textbounds = new Rect();

	boolean initialised = false;

	GameView(GameController controller, GameModel model, DrawView drawView, Activity activity) {
		this.controller = controller;
		this.model = model;
		this.drawView = drawView;
		drawView.setViewReference(this);
		
		model.registerGameObserver(this);
		
		scoreRect = new RectF(0.0f, 0.0f, 0.45f, 0.45f);
		levelRect = new RectF(0.55f, 0.0f, 1, 0.45f);
		textRect = new RectF(0.0f, 0.4f, 1.0f, 0.6f);

		objRedRect = new RectF(0.65f, 0.2f, 1, 0.25f);
		objYelRect = new RectF(0.65f, 0.25f, 1, 0.3f);
		objBlueRect = new RectF(0.65f, 0.3f, 1, 0.35f);

		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		
		liqMoveSound = soundPool.load(activity, R.raw.stream_short, 1);
		liqOverflowSound = soundPool.load(activity, R.raw.stream, 1);
		startSound = soundPool.load(activity, R.raw.bonus, 1);
		gameOverSound = soundPool.load(activity, R.raw.gameover, 1);
		
		redrawTimer = new UITimer(redrawTimer_Timeout, 50, true);
		redrawTimer.start();
	}
	
	void setRedrawMode(boolean set) {
		if (set) {
			redrawTimer.start();
		} else {
			redrawTimer.stop();
		}
	}
	
	void initPaint(Canvas canvas) {
		screensize.x = canvas.getWidth();
		screensize.y = canvas.getHeight();
		int max = Math.max(screensize.x, screensize.y);
		float lineWidth = (float)max / 200;
		
		linePaint.setStyle(Style.STROKE);
		linePaint.setColor(Color.BLACK);
		linePaint.setStrokeWidth(lineWidth);

		thinlinePaint.set(linePaint);
		thinlinePaint.setStrokeWidth(1);

		aalinePaint.set(linePaint);
		aalinePaint.setAntiAlias(true);

		fxlinePaint.set(aalinePaint);
		fxlinePaint.setStrokeWidth((float)screensize.y / 500 * 4);

		fillPaint.setStyle(Style.FILL);
		fillPaint.setAntiAlias(true);
		fillPaint.setColor(Color.BLACK);

		textPaint.setStyle(Style.FILL);
		textPaint.setAntiAlias(true);
		textPaint.setColor(Color.BLACK);
		textPaint.setTextAlign(Align.LEFT);
		textPaint.setTextSize(max / 34);			// 480: 14
		
		smalltextPaint.set(textPaint);
		smalltextPaint.setTextSize(max / 60);		// 480: 8

		centertextPaint.set(textPaint);
		centertextPaint.setTextAlign(Align.CENTER);

		largetextPaint.set(textPaint);
		largetextPaint.setTextSize(max / 26);		// 480: 18

		hugetextPaint.set(centertextPaint);
		hugetextPaint.setTextSize(max / 6);			// 480: 80

		initialised = true;
	}
	
	private Runnable redrawTimer_Timeout = new Runnable() {
		public void run() {
			redrawCounter++;
			if (redrawCounter >= 10) {
				flashon = !flashon;
				redrawCounter = 0;
			}
			update();
		}
	};

	@Override
	public void update() {
		// will result in redraw
		drawView.invalidate();
	}
	
	@Override
	public void playSound(Sounds sound) {
		switch (sound) {
			case LiqMove:		soundPool.play(liqMoveSound, 1.0f, 1.0f, 0, 0, 1.0f);		break;
			case LiqOverflow:	soundPool.play(liqOverflowSound, 1.0f, 1.0f, 0, 0, 1.0f);	break;
			case Start:			soundPool.play(startSound, 1.0f, 1.0f, 0, 0, 1.0f);			break;
			case GameOver:		soundPool.play(gameOverSound, 1.0f, 1.0f, 0, 0, 1.0f);		break;
			default:	break;
		}
	}

	void redraw(Canvas canvas) {
		if (model.gameMode == GameMode.Splash) {
			drawSplash(canvas);
		} else {
			drawGame(canvas);
		}
	}
	
	void drawSplash(Canvas canvas) {
		canvas.drawColor(Color.WHITE);		
	}
	
	void drawGame(Canvas canvas) {
		String s[];
		String sobj = "";
		String text;
		boolean sflash = false;
		boolean flashon0;
		
		if (!initialised) {
			initPaint(canvas);
		}

		canvas.drawColor(Color.WHITE);
		
		sobj = "Target\n";
		if (model.objRedPurity > 0)
			sobj += String.format("Red:%.0f ", model.objRedPurity * 100);
		if (model.objYelPurity > 0)
			sobj += String.format("Yellow:%.0f ", model.objYelPurity * 100);
		if (model.objBluePurity > 0)
			sobj += String.format("Blue:%.0f ", model.objBluePurity * 100);

		if (model.gameMode == GameMode.Idle || model.gameMode == GameMode.Demo) {
			s = new String[] { "Control flow by selecting position",
					"Order components by colour",
					"WHITE space contains nothing",
					"Beware of BLACK contaminant!",
					"Careful not to overflow!"};
		} else if (model.gameMode == GameMode.SelectLevel) {
			s = new String[] { "Select level below",
					"Then select START above" };
			sflash = true;
			for (int i = 0; i < model.maxCont; i++) {
				if (i == 0)
					model.cont.get(i).setCustString(util.getLevelString(i));
				else
					model.cont.get(i).setCustString(
							"LEVEL " + util.getLevelString(i * 2 - 1));
			}
		} else if (model.gameMode == GameMode.Brief) {
			if (model.level > 0)
				s = new String[] { sobj };
			else
				s = new String[] { sobj,
						"Select the suggested position" };
			sflash = true;
		} else if (model.gameMode == GameMode.NextLevel) {
			s = new String[] { "Level " + util.getLevelString(model.level)
					+ " Completed!" };
			sflash = true;
		} else if (model.gameMode == GameMode.GameOver) {
			s = new String[] { "GAME OVER" };
		} else if (model.gameMode == GameMode.GameOver_Hiscore) {
			s = new String[] { "GAME OVER", "NEW HISCORE!" };
		} else if (model.gameMode == GameMode.Debrief) {
			s = new String[model.levelPurities.size() + 1];
			s[0] = "LEVEL TOTALS";
			for (int i = 0; i < model.levelPurities.size(); i++) {
				s[i + 1] = String.format(
						"Level:%s Score:%d\n Red:%.0f Yellow:%.0f Blue:%.0f",
						util.getLevelString(model.levelPurities.get(i).level),
						model.levelPurities.get(i).totScore,
						model.levelPurities.get(i).totRedPurity * 100,
						model.levelPurities.get(i).totYelPurity * 100,
						model.levelPurities.get(i).totBluePurity * 100);
			}
		} else if (model.pauseMode) {
			s = new String[] { "PAUSED" };
			sflash = true;
		} else {
			s = new String[0];
		}

		// draw feed
		model.feed.draw(canvas, screensize, linePaint, thinlinePaint, aalinePaint, fxlinePaint, fillPaint, textPaint, smalltextPaint, centertextPaint, flashon);
		if (model.level == 0 && model.gameMode != GameMode.SelectLevel) {
			// draw help arrow
			for (int i = 0; i < model.maxCont; i++) {
				model.cont.get(i).setCustString("");
			}
			if (model.guideCont >= 0
					&& (model.gameMode == GameMode.Play || model.gameMode == GameMode.Brief || model.gameMode == GameMode.Demo)) {
				if (model.getValvePos() == model.guideCont || flashon)
					model.cont.get(model.guideCont).setCustString("arrow1");
				else
					model.cont.get(model.guideCont).setCustString("arrow2");
			}
		}
		// draw valve
		model.valve.draw(canvas, screensize, aalinePaint, fillPaint, model.curcont, model.feed.getTopComp().color);
		// draw cont's
		for (int i = 0; i < model.maxCont; i++) {
			if (model.gameMode == GameMode.Play || model.gameMode == GameMode.Demo)
				flashon0 = flashon;
			else
				flashon0 = false;
			model.cont.get(i).draw(canvas, screensize, linePaint, thinlinePaint, aalinePaint, fxlinePaint, fillPaint, textPaint, smalltextPaint, centertextPaint, flashon0);
		}

		if (model.gameMode != GameMode.Demo && model.gameMode != GameMode.Idle) {
			// display score
			largetextPaint.setColor(Color.BLUE);
			util.virtToReal(scoreRect, screensize, rect);
			rect.left += 2;
			text = "SCORE";
			largetextPaint.getTextBounds(text, 0, text.length(), textbounds);
			rect.top += textbounds.height() + 2;
			canvas.drawText(text, rect.left, rect.top, largetextPaint);
			text = String.format("%d", model.getTotScore());
			largetextPaint.getTextBounds(text, 0, text.length(), textbounds);
			rect.top += textbounds.height() * 1.2f;
			canvas.drawText(text, rect.left, rect.top, largetextPaint);
			// display level
			util.virtToReal(levelRect, screensize, rect);
			text = "LEVEL";
			largetextPaint.getTextBounds(text, 0, text.length(), textbounds);
			rect.top += textbounds.height() + 2;
			canvas.drawText(text, rect.right - textbounds.width() - 4, rect.top, largetextPaint);
			text = util.getLevelString(model.level);
			largetextPaint.getTextBounds(text, 0, text.length(), textbounds);
			rect.top += textbounds.height() * 1.2f;
			canvas.drawText(text, rect.right - textbounds.width() - 4, rect.top, largetextPaint);
			// display objectives progress
			if (model.objRedPurity > 0) {
				fillPaint.setColor(util.getCompTextColor(CompCode.Red));
				util.virtToReal(objRedRect, screensize, rrect);
				util.copyRect(rrect, rrect0);
				if (model.redPurity < model.objRedPurity) {
					rrect0.right = rrect0.left + rrect0.width() * (model.redPurity / model.objRedPurity);
				}
				rrect0.top = rrect0.bottom - largetextPaint.getTextSize();
				canvas.drawRect(rrect0, fillPaint);
				largetextPaint.setColor(util.getCompColor(CompCode.Red));
				canvas.drawText("Red:", rrect.left, rrect.bottom, largetextPaint);
				text = String.format("%.0f%%", model.objRedPurity * 100);
				largetextPaint.getTextBounds(text, 0, text.length(), textbounds);
				canvas.drawText(text, rrect.right - textbounds.width() - 2, rrect.bottom, largetextPaint);
			}
			if (model.objYelPurity > 0) {
				fillPaint.setColor(util.getCompTextColor(CompCode.Yellow));
				util.virtToReal(objYelRect, screensize, rrect);
				util.copyRect(rrect, rrect0);
				if (model.yelPurity < model.objYelPurity) {
					rrect0.right = rrect0.left + rrect0.width() * (model.yelPurity / model.objYelPurity);
				}
				rrect0.top = rrect0.bottom - largetextPaint.getTextSize();
				canvas.drawRect(rrect0, fillPaint);
				largetextPaint.setColor(util.getCompColor(CompCode.Yellow));
				canvas.drawText("Yellow:", rrect.left, rrect.bottom, largetextPaint);
				text = String.format("%.0f%%", model.objYelPurity * 100);
				largetextPaint.getTextBounds(text, 0, text.length(), textbounds);
				canvas.drawText(text, rrect.right - textbounds.width() - 2, rrect.bottom, largetextPaint);
			}
			if (model.objBluePurity > 0) {
				fillPaint.setColor(util.getCompTextColor(CompCode.Blue));
				util.virtToReal(objBlueRect, screensize, rrect);
				util.copyRect(rrect, rrect0);
				if (model.bluePurity < model.objBluePurity) {
					rrect0.right = rrect0.left + rrect0.width() * (model.bluePurity / model.objBluePurity);
				}
				rrect0.top = rrect0.bottom - largetextPaint.getTextSize();
				canvas.drawRect(rrect0, fillPaint);
				largetextPaint.setColor(util.getCompColor(CompCode.Blue));
				canvas.drawText("Blue:", rrect.left, rrect.bottom, largetextPaint);
				text = String.format("%.0f%%", model.objBluePurity * 100);
				largetextPaint.getTextBounds(text, 0, text.length(), textbounds);
				canvas.drawText(text, rrect.right - textbounds.width() - 2, rrect.bottom, largetextPaint);
			}
		} else if (model.hiscore > 0) {
			// display hiscore
			largetextPaint.setColor(Color.rgb(255, 0, 255));
			util.virtToReal(scoreRect, screensize, rect);
			rect.left += 2;
			text = "HISCORE";
			largetextPaint.getTextBounds(text, 0, text.length(), textbounds);
			rect.top += textbounds.height() + 2;
			canvas.drawText(text, rect.left, rect.top, largetextPaint);
			text = String.format("%d(%s)",model.hiscore, util.getLevelString(model.hilevel));
			largetextPaint.getTextBounds(text, 0, text.length(), textbounds);
			rect.top += textbounds.height() * 1.2f;
			canvas.drawText(text, rect.left, rect.top, largetextPaint);
		}
		if (s.length > 0) {
			if (model.displayn >= s.length) {
				// move to next game mode after displayed all strings
				model.displayn = 0;
				model.updateGameMode();
			}
			// display text
			DrawString(canvas, s[model.displayn], sflash && flashon);
		}

		if (model.gameMode == GameMode.Demo || model.gameMode == GameMode.Idle) {
			// 'PLAY'
			if (flashon)
				hugetextPaint.setColor(Color.rgb(255,192,203));
			else
				hugetextPaint.setColor(Color.RED);
			canvas.drawText("PLAY",
					util.virtToRealX(0.5f, screensize),
					util.virtToRealY(0.85f, screensize),
					hugetextPaint);		 
		} else if (model.gameMode == GameMode.SelectLevel) {
			// 'START'
			if (flashon)
				hugetextPaint.setColor(Color.rgb(255,192,203));
			else
				hugetextPaint.setColor(Color.RED);
			canvas.drawText("START",
					util.virtToRealX(0.5f, screensize),
					util.virtToRealY(0.2f, screensize),
					hugetextPaint);
		}
	}
	
	void DrawString(Canvas canvas, String s, boolean flash) {
		String s1[] = s.split("\n");
		String s2[];
		String s0;
		float x, y;
		float ty = 0;

		util.virtToReal(textRect, screensize, rrect);		
		for (int i=0;i<s1.length;i++) {
			largetextPaint.getTextBounds(s1[i], 0, s1[i].length(), textbounds);
			ty += textbounds.height();
		}
		y = rrect.top + (rrect.height() - ty) / 2;
		
		for (int i1=0;i1<s1.length;i1++) {
			largetextPaint.getTextBounds(s1[i1], 0, s1[i1].length(), textbounds);
			x = rrect.left + (rrect.width() - textbounds.width()) / 2;
			s2 = s1[i1].split(" ");
			for (int i2=0;i2<s2.length;i2++) {
				s0 = s2[i2];

				if (flash)
					largetextPaint.setColor(Color.rgb(173, 216, 230));
				else
					largetextPaint.setColor(Color.rgb(0, 0, 139));
				
				if (s0.toLowerCase().contains("red")) {
					if (flash)
						largetextPaint.setColor(util.getCompTextColor(CompCode.Red));
					else
						largetextPaint.setColor(util.getCompColor(CompCode.Red));
				}
				if (s0.toLowerCase().contains("yellow")) {
					if (flash)
						largetextPaint.setColor(util.getCompTextColor(CompCode.Yellow));
					else
						largetextPaint.setColor(util.getCompColor(CompCode.Yellow));
				}
				if (s0.toLowerCase().contains("blue")) {
					if (flash)
						largetextPaint.setColor(util.getCompTextColor(CompCode.Blue));
					else
						largetextPaint.setColor(util.getCompColor(CompCode.Blue));
				}
				if (s0.toLowerCase().contains("black")) {
					if (flash)
						largetextPaint.setColor(Color.DKGRAY);
					else
						largetextPaint.setColor(Color.BLACK);
				}
				if (s0.toLowerCase().contains("white")) {
					if (flash)
						largetextPaint.setColor(Color.WHITE);
					else
						largetextPaint.setColor(Color.LTGRAY);
				}
				if (	s0.toLowerCase().contains("start") ||
						s0.toLowerCase().contains("game") ||
						s0.toLowerCase().contains("over") ||
						s0.toLowerCase().contains("colour"))
					if (flash)
						largetextPaint.setColor(Color.rgb(255,192,203));
					else
						largetextPaint.setColor(Color.RED);
				canvas.drawText(s0, x, y, largetextPaint);
				largetextPaint.getTextBounds(s0, 0, s0.length(), textbounds);
				x += (textbounds.width() + largetextPaint.getTextSize() / 3);
				if (i2 == s2.length-1) {
					y += textbounds.height() * 1.5f;
				}
			}
		}
	}
}
