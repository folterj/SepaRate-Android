package info.joostdefolter.separate;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

public class DrawView extends View {
	GameView view;

	public DrawView(Context context) {
		super(context);
	}

	public DrawView(Context context, AttributeSet attributes) {
		super(context, attributes);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
			if (view != null) {
				view.controller.touchEvent(event.getX(), event.getY());
			}
		}
		return true; // consume event
	}

	public void setViewReference(GameView view) {
		this.view = view;
	}

	@Override
	protected void onDraw(@NonNull Canvas canvas) {
		super.onDraw(canvas);
		
		if (view != null) {
			view.redraw(canvas);
		}
	}
}
