package info.joostdefolter.separate;

import android.graphics.Color;
import android.graphics.Point;
import android.graphics.RectF;

public class util {
	static float virtToRealX(float x,Point screensize) {
		return x*screensize.x;
	}

	static float virtToRealY(float y,Point screensize) {
		return y*screensize.y;
	}

	static RectF virtToReal(RectF virtRect,Point screensize) {
		return new RectF(virtRect.left*screensize.x,
						virtRect.top*screensize.y,
						virtRect.right*screensize.x,
						virtRect.bottom*screensize.y);
	}
	
	static void virtToReal(RectF virtRect,Point screensize,RectF realRect) {
		realRect.left = virtRect.left*screensize.x;
		realRect.top = virtRect.top*screensize.y;
		realRect.right = virtRect.right*screensize.x;
		realRect.bottom = virtRect.bottom*screensize.y;
	}
	
	static void copyRect(RectF srcRect,RectF dstRect) {
		dstRect.left = srcRect.left;
		dstRect.top = srcRect.top;
		dstRect.right = srcRect.right;
		dstRect.bottom = srcRect.bottom;		
	}

	static int getCompColor(CompCode code) {
		int color;
		switch (code) {
			case Red:		color = Color.RED;				break;
			case Yellow:	color = Color.rgb(255,215,0);	break;
			case Blue:		color = Color.rgb(0,100,255);	break;
			case Black:		color = Color.BLACK;			break;
			default:		color = Color.LTGRAY;			break;
		}
		return color;
	}

	static int getCompTextColor(CompCode code) {
		int color;
		switch (code) {
			case Red:		color = Color.rgb(255,192,203);	break;
			case Yellow:	color = Color.YELLOW;			break;
			case Blue:		color = Color.rgb(173,216,230);	break;
			case Black:		color = Color.GRAY;				break;
			default:		color = Color.WHITE;			break;
		}
		return color;
	}

	static int lighterColor(int color) {
		return Color.rgb((Color.red(color) + 255) / 2,(Color.green(color) + 255) / 2,(Color.blue(color) + 255) / 2);
	}

	static int darkerColor(int color) {
		return Color.rgb(Color.red(color) / 2,Color.green(color) / 2,Color.blue(color) / 2);
	}

	static int altColor(int color) {
		int r = Color.red(color);
		int g = Color.green(color);
		int b = Color.blue(color);
		if (r == 0 && g == 0 && b == 0) {
			return lighterColor(color);
		}
		return darkerColor(color);
	}

	static int CMYKtoColor(CMYK cmyk) {
		int r = (int)((1.0f-cmyk.c)*(1.0f-cmyk.k)*255.0f);
		int g = (int)((1.0f-cmyk.m)*(1.0f-cmyk.k)*255.0f);
		int b = (int)((1.0f-cmyk.y)*(1.0f-cmyk.k)*255.0f);
		return Color.rgb(r,g,b);
	}

	static CMYK ColortoCMYK(int color) {
		CMYK cmyk = new CMYK();
		float c = (float)(255 - Color.red(color))/255;
		float m = (float)(255 - Color.green(color))/255;
		float y = (float)(255 - Color.blue(color))/255;

		float min = (float)Math.min(c,Math.min(m,y));
		if (min == 1.0f) {
			cmyk.c = 0.0f;
			cmyk.m = 0.0f;
			cmyk.y = 0.0f;
			cmyk.k = 1.0f;
		} else {
			cmyk.c = (c-min)/(1-min);
			cmyk.m = (m-min)/(1-min);
			cmyk.y = (y-min)/(1-min);
			cmyk.k = min;
		}
		return cmyk;
	}

	static String getLevelString(int level) {
		String s = "";
		if (level > 0)
			s = String.format("%d",level);
		else
			s = "HELP";
		return s;
	}
}
