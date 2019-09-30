package kim.kohlhaas.sone.synthesis;

import javax.sound.sampled.AudioFormat;

import kim.kohlhaas.sone.harmony.ToneSet;

public class GuitarCharacter {
    
    private AudioFormat format;
    private NoiseGen characterNoiseGen;
    private long seed;
    private double[] characterNoise;
    
    public GuitarCharacter(AudioFormat format, double deepestFrequency, long seed) {
        this.format = format;
        this.seed = seed;
        this.characterNoiseGen = new NoiseGenRnd(seed);
        
        int noiseLength = (int) Math.ceil((format.getSampleRate() / deepestFrequency));
        
        characterNoise = new double[noiseLength];
        
        for (int i = 0; i < characterNoise.length; i++) {
            characterNoise[i] = characterNoiseGen.bipolar();
        }
    }
    
    public GuitarCharacter(AudioFormat format, ToneSet toneSet, long seed) {
        this(format, toneSet.getTone(0).getFrequency(), seed);        
    }
    
    public double getCharacterSample(int index) {
        return characterNoise[index];
    }
    
}
