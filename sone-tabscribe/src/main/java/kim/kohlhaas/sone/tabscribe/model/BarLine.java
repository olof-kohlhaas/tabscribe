package kim.kohlhaas.sone.tabscribe.model;

public class BarLine implements Comparable<BarLine> {
	
	public static enum Type {
		SINGLE
	}
	
	private Type type;
	private Double millisecond;
	private Double measureMilliOffset;
	private Double measureMilliDuration;
	private Integer beatsPerMeasure;
	
	public BarLine (Double millisecond) {
		this(millisecond, Type.SINGLE);
	}
	
	public BarLine (Double millisecond, Type type) {
		this(millisecond, type, 0.0, 1.0, 4);
	}
	
	public BarLine (Double millisecond, Type type, Double measureMilliOffset, Double measureMilliDuration, int beatsPerMeasure) {
		this.millisecond = millisecond;
		this.type = type;
		this.measureMilliOffset = measureMilliOffset;
		this.measureMilliDuration = measureMilliDuration;
		this.beatsPerMeasure = beatsPerMeasure;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Double getMillisecond() {
		return millisecond;
	}

	public void setMillisecond(Double millisecond) {
		this.millisecond = millisecond;
	}

	public Double getMeasureMilliOffset() {
		return measureMilliOffset;
	}

	public void setMeasureMilliOffset(Double measureMilliOffset) {
		this.measureMilliOffset = measureMilliOffset;
	}

	public Double getMeasureMilliDuration() {
		return measureMilliDuration;
	}

	public void setMeasureMilliDuration(Double measureMilliDuration) {
		this.measureMilliDuration = measureMilliDuration;
	}

	public Integer getBeatsPerMeasure() {
		return beatsPerMeasure;
	}

	public void setBeatsPerMeasure(Integer beatsPerMeasure) {
		this.beatsPerMeasure = beatsPerMeasure;
	}

	@Override
	public int compareTo(BarLine barLine) {
		if (barLine == null) return 1;
		return getMillisecond().compareTo(barLine.getMillisecond());
	}
	
	@Override
	public String toString() {
		return "type: " + type + ", millisecond: " + millisecond; 
	}
	
}
