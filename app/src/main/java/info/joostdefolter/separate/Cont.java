package info.joostdefolter.separate;

import java.util.Random;
import java.util.Vector;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

public class Cont extends Vector<Comps> {
	private static final long serialVersionUID = -2863492380541539285L;

	Vector<PointF> bubbles = new Vector<>();
	Random rnd = new Random();
	int[] max;
	
	RectF rect;
	RectF rrect = new RectF();
	RectF rect0 = new RectF();
	RectF rrect0 = new RectF();
	Rect textbounds = new Rect();

	int maxComp;
	boolean feed;
	float purity;
	CompCode purityComp;
	int purityColor;
	int dscore;
	String custString;
	boolean purityChanged;

	Cont() {
		super();
		rect = new RectF();
		maxComp = 0;
		feed = false;
		init();
		reset();
	}

	Cont(RectF rect, int maxComp, boolean feed) {
		super();
		this.rect = rect;
		this.maxComp = maxComp;
		this.feed = feed;
		init();
		reset();
	}
	
	void init() {
		max = new int[CompCode.Size.getValue()];		
	}

	void reset() {
		clear();
		purity = 0;
		purityComp = CompCode.Empty;
		purityColor = util.getCompColor(purityComp);
		purityChanged = false;
		bubbles.clear();
		dscore = 0;
		custString = "";
	}

	void addComp(Comps comps) {
		if (!isOverFull() && (comps.size() > 0 || feed))
			add(comps);
		updatePurity();
	}

	boolean checkMove(Cont cont) {
		int maxcomp;
		boolean moved = false;
		
		while (isOverFull()) {
			cont.addComp(elementAt(0));
			if (elementAt(0).size() > 0)
				moved = true;
			this.remove(0);
		}

		for (int i = 0; i < size() - 1; i++) {
			maxcomp = elementAt(i).size();
			if (maxcomp > 1) {
				elementAt(i + 1).insertFirst(elementAt(i).elementAt(maxcomp - 1));
				elementAt(i).removeLast();
			}
		}

		return moved;
	}

	void setDScore(int dscore0) {
		dscore = dscore0;
	}

	void setCustString(String custString) {
		this.custString = custString;
	}

	Comps getTopComp() {
		if (isFull()) {
			return elementAt(0);
		}
		return new Comps();
	}

	boolean isFull() {
		return size() >= maxComp;
	}

	boolean isOverFull() {
		return size() > maxComp;
	}

	void updatePurity() {
		CMYK col;
		float c, m, y, k;
		int maxi, maxval, totmax;
		float purity0 = purity;
		CompCode purityComp0 = purityComp;

		for (int i = 0; i < CompCode.Size.getValue(); i++) {
			max[i] = 0;
		}
		for (int i = 0; i < size(); i++) {
			elementAt(i).updatePurity();
			for (int j = 0; j < elementAt(i).size(); j++) {
				max[elementAt(i).elementAt(j).code.getValue()]++;
			}
		}
		c = 0;
		m = 0;
		y = 0;
		k = 0;
		maxval = 0;
		totmax = 0;
		maxi = 0;
		for (int i = 0; i < CompCode.Size.getValue(); i++) {
			if (i != CompCode.Black.getValue() && i != CompCode.Empty.getValue()) {
				if (max[i] > maxval) {
					maxval = max[i];
					maxi = i;
				}
				col = util.ColortoCMYK(util.getCompColor(CompCode.getEnum(i)));
				c += col.c * max[i];
				m += col.m * max[i];
				y += col.y * max[i];
				k += col.k * max[i];
				totmax += max[i];
			}
		}
		if (max[CompCode.Black.getValue()] == 0) {
			if (totmax > 0) {
				purity = (float) maxval / maxComp;
				if (purity > purity0)
					purityComp = CompCode.getEnum(maxi);
				col = new CMYK();
				col.c = c / totmax;
				col.m = m / totmax;
				col.y = y / totmax;
				col.k = k / totmax;
				purityColor = util.CMYKtoColor(col);
			}
		} else {
			purity = 0;
			purityComp = CompCode.Black;
			purityColor = util.getCompColor(purityComp);
		}
		purityChanged = (purityComp != purityComp0);
	}

	void resetDisplay() {
		dscore = 0;
		custString = "";
		purityChanged = false;
	}

	void draw(Canvas canvas, Point screensize,
			Paint linePaint, Paint thinlinePaint, Paint aalinePaint, Paint fxlinePaint, Paint fillPaint, Paint textPaint, Paint smalltextPaint, Paint centertextPaint,
			boolean flashon) {
		float compheight;
		float blocksize = centertextPaint.getTextSize() / 1.2f;
		String s;
		float cposx = rect.left + rect.width() / 2;
		float x1, y1, x2, y2;
		
		linePaint.setColor(Color.DKGRAY);

		util.virtToReal(rect, screensize, rrect);
		if (feed) {
			// feed cont
			compheight = rect.height() / maxComp;
			util.copyRect(rect, rect0);
			rect0.bottom = rect0.top + compheight;
			if (size() > 2 || flashon) {
				for (int i = size() - 1; i >= 0; i--) {
					util.virtToReal(rect0, screensize, rrect0);
					elementAt(i).draw(canvas, rrect0, fillPaint);
					if (i > 0) {
						drawInterfaceFX(canvas, screensize, fxlinePaint, rrect0, elementAt(i-1));
					}
					rect0.top += compheight;
					rect0.bottom += compheight;
				}
			}
			canvas.drawRect(rrect, linePaint);

			util.copyRect(rect, rect0);
			rect0.top += compheight;
			rect0.bottom = rect0.top + compheight * (maxComp - 2);
			util.virtToReal(rect0, screensize, rrect0);
			// column: semi-transparent
			fillPaint.setColor(Color.DKGRAY);
			fillPaint.setAlpha(64);
			canvas.drawRect(rrect0, fillPaint);
			fillPaint.setAlpha(255);
		} else {
			// normal cont
			if (size() > 0) {
				compheight = rect.height() / (maxComp + 1);
				util.copyRect(rect, rect0);
				rect0.top = rect0.bottom - compheight * size();
				if (isFull() && flashon)
					fillPaint.setColor(util.altColor(purityColor));
				else
					fillPaint.setColor(purityColor);
				util.virtToReal(rect0, screensize, rrect0);
				canvas.drawRect(rrect0, fillPaint);
				if (!isOverFull() && (!isFull() || !flashon)) {
					drawInterfaceFX(canvas, screensize, fxlinePaint, rrect0, elementAt(0));
				}
				drawBubbleFX(canvas, screensize, fillPaint);
			}
			if (!isOverFull()) {
				// scale lines / numbers
				thinlinePaint.setColor(Color.LTGRAY);
				compheight = rect.height() / (maxComp + 1);
				util.copyRect(rect, rect0);
				rect0.top = rect0.bottom - compheight * maxComp;
				util.virtToReal(rect0, screensize, rrect0);
				canvas.drawLine(rrect0.left, rrect0.top, rrect0.right, rrect0.top, thinlinePaint);
				rect0.bottom = rect.bottom;
				rect0.top = rect0.bottom - compheight * maxComp / 2;
				util.virtToReal(rect0, screensize, rrect0);
				canvas.drawLine(rrect0.left, rrect0.top, rrect0.right, rrect0.top, thinlinePaint);
				for (int i = 1; i <= 10; i++) {
					rect0.bottom = rect.bottom;
					rect0.top = rect0.bottom - compheight * maxComp * i / 10;
					util.virtToReal(rect0, screensize, rrect0);
					if (i != 5 && i != 10) {
						canvas.drawLine(rrect0.right - rrect0.width() / 5,
										(int)rrect0.top,
										rrect0.right,
										(int)rrect0.top, thinlinePaint);
					}
					s = String.format("%d", i * 10);
					smalltextPaint.setColor(thinlinePaint.getColor());
					smalltextPaint.getTextBounds(s, 0, s.length(), textbounds);
					canvas.drawText(s, rrect0.right - textbounds.width() - linePaint.getStrokeWidth(), rrect0.top + textbounds.height() + 2, smalltextPaint);
				}
			}

			linePaint.setColor(Color.BLACK);
			Path path = new Path();
			path.moveTo((int)rrect.left, (int)rrect.top);
			path.lineTo((int)rrect.left, (int)rrect.bottom);
			path.lineTo((int)rrect.right, (int)rrect.bottom);
			path.lineTo((int)rrect.right, (int)rrect.top);
			canvas.drawPath(path, linePaint);
		}

		if (!isOverFull()) {
			if (!feed && (!purityChanged || flashon)) {
				util.copyRect(rrect, rrect0);
				rrect0.bottom = rrect.bottom - blocksize * 0.5f;
				if (purity > 0) {
					// draw small pure colour block
					fillPaint.setColor(util.getCompColor(purityComp));
					rrect0.top = rrect0.bottom - blocksize;
					rrect0.right = rrect0.left + blocksize;
					canvas.drawRect(rrect0, fillPaint);
				}
				// draw purity percentage
				centertextPaint.setColor(util.getCompTextColor(purityComp));
				if (purity > 0)
					s = String.format("%.0f%%", purity * 100);
				else
					s = "-";
				canvas.drawText(s, rrect.centerX(), rrect0.bottom, centertextPaint);
			}
			if (custString.contains("arrow")) {
				// draw guide arrow
				if (custString.contains("2"))
					aalinePaint.setColor(Color.BLACK);
				else
					aalinePaint.setColor(Color.DKGRAY);
				x1 = cposx;
				y1 = rect.top - rect.height() / 4;
				x2 = cposx;
				y2 = rect.top - rect.height() / 8;
				canvas.drawLine(util.virtToRealX(x1, screensize),
						util.virtToRealY(y1, screensize),
						util.virtToRealX(x2, screensize),
						util.virtToRealY(y2, screensize), aalinePaint);
				y1 = y2 - rect.width() / 8;
				x1 = x2 - rect.width() / 8;
				canvas.drawLine(util.virtToRealX(x1, screensize),
						util.virtToRealY(y1, screensize),
						util.virtToRealX(x2, screensize),
						util.virtToRealY(y2, screensize), aalinePaint);
				x1 = x2 + rect.width() / 8;
				canvas.drawLine(util.virtToRealX(x1, screensize),
						util.virtToRealY(y1, screensize),
						util.virtToRealX(x2, screensize),
						util.virtToRealY(y2, screensize), aalinePaint);
				custString = "";
			}
			if (!custString.isEmpty()) {
				// draw custom string
				textPaint.setColor(Color.BLACK);
				canvas.drawText(custString, rrect.left + 2, rrect.centerY(), textPaint);
			} else if (dscore != 0) {
				// draw delta score
				s = String.format("%d", dscore);
				if (dscore > 0)
					s = "+" + s;
				centertextPaint.setColor(util.getCompTextColor(purityComp));
				canvas.drawText(s, rrect.centerX(), rrect.centerY(), centertextPaint);
			}
		}
	}

	void drawInterfaceFX(Canvas canvas, Point screensize, Paint fxlinePaint, RectF rrect, Comps comps) {
		float x,y,lastx,lasty,y0;
		float x1 = rrect.left;
		float x2 = rrect.right;
		float amp = (float)screensize.y / 500;
		float width = rrect.width() / 10;
		
		if (feed)
			y0 = rrect.bottom;
		else
			y0 = rrect.top;

		if (!feed)
			fxlinePaint.setColor(purityColor);
		else
			fxlinePaint.setColor(comps.color);

		lastx = 0;
		lasty = 0;
		for (x=x1;x<x2;x++) {
			y = (float) (y0 + amp * Math.sin((x - x1) / width + comps.wavepos));
			if (x == x1) {
				lastx = x;
				lasty = y;
			}
			canvas.drawLine(lastx, lasty, x, y, fxlinePaint);
			lastx = x;
			lasty = y;
		}
		comps.wavepos += 1.0f;
		while (comps.wavepos >= 2 * Math.PI)
			comps.wavepos -= (float)(2 * Math.PI);
	}

	void drawBubbleFX(Canvas canvas, Point screensize, Paint fillPaint) {
		float rad = (float)Math.min(screensize.x,screensize.y) / 100;
		float x,y;
		float margin = 0.02f;
		
		fillPaint.setColor(Color.WHITE);
		
		float height = rect.height() * size() / maxComp;
		if (height > rect.height())
			height = rect.height();

		while (bubbles.size() < height * 20)
			bubbles.add(new PointF(rnd.nextFloat() * rect.width(), 0));

		for (int i=0;i<bubbles.size();i++) {
			bubbles.get(i).y += 0.005f;
			bubbles.get(i).x += (rnd.nextFloat() - 0.5f) / 200;
			if (bubbles.get(i).x < margin) {
				bubbles.get(i).x = margin;
			}
			if (bubbles.get(i).x > rect.width() - margin) {
				bubbles.get(i).x = rect.width() - margin;
			}
			if (bubbles.get(i).y > height) {
				bubbles.get(i).x = rnd.nextFloat() * rect.width();
				bubbles.get(i).y = 0;
			}
		}
		for (int i=0;i<bubbles.size();i++) {
			x = rect.left + bubbles.get(i).x;
			y = rect.bottom - bubbles.get(i).y;
			canvas.drawCircle(util.virtToRealX(x,screensize), util.virtToRealY(y,screensize), rad, fillPaint);
		}
	}
}
