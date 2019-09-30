package kim.kohlhaas.sone.harmony;

public interface ToneSet {

    Tone getTone(int index);
    Temperament getTemperament();
    int getToneCount();
    Tone getNearestTone(double frequency);
    public int getToneOffset();
        
    public int getIndex(Tone tone);
    
    int getNearestIndex(double frequency);
    
    double getGradientIndex(double frequency);
    
    double getTonePrecision(double frequency);

}