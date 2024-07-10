package info.joostdefolter.separate;

public enum CompCode {
	Empty(0),
	Red(1),
	Yellow(2),
	Blue(3),
	Black(4),
	Size(5);
	
	private final int value;
	CompCode(int value) {
        this.value = value;
    }
	
	public int getValue() {
	    return value;
	}
	
	public static CompCode getEnum(int i) {
		switch (i) {
			case 1: return Red;
			case 2: return Yellow;
			case 3: return Blue;
			case 4: return Black;
		}

		return Empty;
	}
}
