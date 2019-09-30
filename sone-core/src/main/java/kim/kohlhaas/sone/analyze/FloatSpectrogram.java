package kim.kohlhaas.sone.analyze;

import java.io.IOException;

import kim.kohlhaas.sone.signal.FloatAudioSignal;

public interface FloatSpectrogram {

    public int getChannels();
    public int getFreqs();
    public int getTimeSteps();
    public double getTimeStep(double millisecond);
    public double getMilliseconds(double timeSteps);
    public int getSamplesPerTimeStep();
    public double getMilliseconds();
    public float getMaxAmp();
    public float getMaxAmp(int channel);
    public float getMaxAmpInFreq(int channel, int freqIndex);
    public float getMaxAmpInTime(int channel, int timeIndex);
    public float getAmp(int timeIndex, int freqIndex);
    public float getAmp(int channel, int timeIndex, int freqIndex);
    public void copySpectrum(int channel, int index, float[] dest);
    public void copySpectrum(int channel, int from, int to, float[][] dest);
    public double getFreq(int freqIndex);
    public FloatAudioSignal getFloatAudioSignal();
    public void setFloatAudioSignal(FloatAudioSignal floatAudioSignal) throws IOException;
    
}
