package info.joostdefolter.separate;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;

public class Valve {
	RectF rect;
	int maxpos;
	int pos;

	Valve(RectF rect, int maxpos) {
		this.rect = rect;
		this.maxpos = maxpos;
		pos = 0;
	}

	void draw(Canvas canvas, Point screensize, Paint aalinePaint, Paint fillPaint, Cont curcont, int color) {
		fillPaint.setColor(color);
		aalinePaint.setColor(Color.DKGRAY);

		float xfeed = rect.left + rect.width() / 2;
		float yfeed = rect.top;
		float xcont = curcont.rect.left + curcont.rect.width() / 2;
		float ycont = curcont.rect.top - 0.02f;

		float x1a = util.virtToRealX(xfeed - 0.02f, screensize);
		float y1a = util.virtToRealY(yfeed - 0.005f, screensize);
		float x2a = util.virtToRealX(xcont - 0.02f, screensize);
		float y2a = util.virtToRealY(ycont, screensize);

		float x1b = util.virtToRealX(xfeed + 0.02f, screensize);
		float y1b = util.virtToRealY(yfeed - 0.005f, screensize);
		float x2b = util.virtToRealX(xcont + 0.02f, screensize);
		float y2b = util.virtToRealY(ycont, screensize);

		Path path = new Path();
		path.moveTo(x1a, y1a);
		path.lineTo(x2a, y2a);
		path.lineTo(x2b, y2b);
		path.lineTo(x1b, y1b);
		canvas.drawPath(path, fillPaint);

		canvas.drawLine(x1a, y1a, x2a, y2a, aalinePaint);
		canvas.drawLine(x1b, y1b, x2b, y2b, aalinePaint);
	}
}
