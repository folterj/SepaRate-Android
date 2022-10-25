package info.joostdefolter.separate;

public class CMYK {
	float c;
	float m;
	float y;
	float k;

	public CMYK() {
		c = 0;
		m = 0;
		y = 0;
		k = 0;
	}

	public CMYK(float c, float m, float y, float k) {
		this.c = c;
		this.m = m;
		this.y = y;
		this.k = k;
	}
}
