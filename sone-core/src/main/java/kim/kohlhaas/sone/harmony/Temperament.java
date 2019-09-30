package kim.kohlhaas.sone.harmony;

public interface Temperament {

    int getToneCount();

    Tone getTone(int index);
    
    Tone getNearestTone(double frequency);
    
    int getNearestIndex(double frequency);
    
    double getGradientIndex(double frequency);
    
    double getTonePrecision(double frequency);
    
    double getFrequency(Tone tone);
    
    int getOctaveToneCount();
    
    int getIndex(Tone tone);
    
    Tone getPitchShiftedTone(Tone tone, int semitones);

}