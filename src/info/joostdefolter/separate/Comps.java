package info.joostdefolter.separate;

import java.util.Vector;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Comps extends Vector<Comp> {
	private static final long serialVersionUID = 76797250491777563L;

	int color;
	float wavepos;

	Comps() {
		super();
		color = Color.WHITE;
		wavepos = 0;
	}

	void insertFirst(Comp comp) {
		insertElementAt(comp, 0);
	}

	void removeLast() {
		removeElementAt(size() - 1);
	}

	void add(CompCode code) {
		add(new Comp(code));
	}

	boolean contains(CompCode code) {
		boolean found = false;
		
		for (int i = 0; i < size(); i++) {
			if (elementAt(i).code == code)
				found = true;
		}
		return found;
	}

	void updatePurity() {
		CMYK col;
		float tot = 0;
		float c = 0;
		float m = 0;
		float y = 0;
		float k = 0;
		
		for (int i = 0; i < size(); i++) {
			col = util.ColortoCMYK(util.getCompColor(elementAt(i).code));
			c += col.c;
			m += col.m;
			y += col.y;
			k += col.k;
			tot++;
		}
		if (tot > 0) {
			col = new CMYK();
			col.c = c / tot;
			col.m = m / tot;
			col.y = y / tot;
			col.k = k / tot;
			color = util.CMYKtoColor(col);
		}
	}

	void draw(Canvas canvas, RectF rect, Paint fillPaint) {
		fillPaint.setColor(color);
		canvas.drawRect(rect, fillPaint);
	}
}
