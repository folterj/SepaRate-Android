package info.joostdefolter.separate;

public class LevelPurity {
	int level;
	float totRedPurity;
	float totYelPurity;
	float totBluePurity;
	int totScore;
	boolean completed;

	LevelPurity() {
		level = 0;
		totRedPurity = 0;
		totYelPurity = 0;
		totBluePurity = 0;
		totScore = 0;
		completed = false;
	}
}
