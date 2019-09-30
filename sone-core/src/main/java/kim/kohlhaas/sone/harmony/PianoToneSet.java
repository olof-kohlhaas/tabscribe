package kim.kohlhaas.sone.harmony;

public abstract class PianoToneSet implements ToneSet {

    private Temperament temperament;
    private int toneOffset;
    private int toneCount;
    
    public PianoToneSet(Temperament temperament, int toneOffset, int toneCount) {
        this.temperament = temperament;
        this.toneOffset = toneOffset;
        this.toneCount = toneCount;
    }
    
    /* (non-Javadoc)
     * @see kim.kohlhaas.sone.harmony.ToneSet#getTone(int)
     */
    @Override
    public Tone getTone(int index) {
        return temperament.getTone(index + this.toneOffset);
    }
    
    /* (non-Javadoc)
     * @see kim.kohlhaas.sone.harmony.ToneSet#getToneCount()
     */
    @Override
    public int getToneCount() {
        return this.toneCount;
    }
    
    @Override
    public int getToneOffset() {
        return this.toneOffset;
    }
    
    @Override
    public int getIndex(Tone tone) {
        return temperament.getIndex(tone) - this.toneOffset;
    }    
    
    @Override
    public Temperament getTemperament() {
        return temperament;
    }
    
    @Override
    public Tone getNearestTone(double frequency) {
        return temperament.getNearestTone(frequency);
    }

    @Override
    public int getNearestIndex(double frequency) {
        return temperament.getNearestIndex(frequency) - this.toneOffset;
    }

    @Override
    public double getGradientIndex(double frequency) {
        return temperament.getGradientIndex(frequency) - this.toneOffset;
    }

    @Override
    public double getTonePrecision(double frequency) {
        return temperament.getTonePrecision(frequency);
    }

    public abstract Tone getKeyTone(int keyNumber);
    public abstract int getKeyNumber(int index);
    public abstract int getIndex(int keyNumber);
    
}
