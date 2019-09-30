package kim.kohlhaas.sone.synthesis;

import javax.sound.sampled.AudioFormat;

import kim.kohlhaas.sone.filter.Filter;
import kim.kohlhaas.sone.filter.FilterFlow;
import kim.kohlhaas.sone.filter.ManagedFilter;

public class GuitarSimpleBody implements Filter {
    
    private double f10 = 0;
    private double f11 = 0;
    private double c0;
    private double c1;
    private double r10 = 0;
    private double r0;
    private double r1;
    private double r11 = 0;
    private double f00 = 0;
    private double f0 = 0;
    private double f1 = 0;
    private double f01 = 0;
    private double r00 = 0;
    private double r01 = 0;
    private AudioFormat format;
    
    private double resonatedSampleLeft = 0.0;
    private double resonatedSampleRight = 0.0;

    

    public GuitarSimpleBody(AudioFormat format) {
        this.format = format;
        
        this.c0 = 2.0 * Math.sin(Math.PI * 3.4375 / format.getSampleRate());
        this.c1 = 2.0 * Math.sin(Math.PI * 6.12492868721483 / format.getSampleRate());
        this.r0 = 0.98;
        this.r1 = 0.98;
    }


    public void writeFrame(float[][] frameValues, FilterFlow filterFlow) {
        // TODO assert 2 channels
        
        /*
         * The guitar body is just simulated by two mixed in low-pass filter with resonance. - Andre Michelle
         */

        for (int i = 0; i < frameValues[0].length; i++) {
            this.r00 = this.r00 * this.r0;
            this.r00 = this.r00 + (this.f0 - this.f00) * this.c0;
            this.f00 = this.f00 + this.r00;
            this.f00 = this.f00 - this.f00 * this.f00 * this.f00 * 0.166666666666666;
            this.r01 = this.r01 * this.r0;
            this.r01 = this.r01 + (this.f1 - this.f01) * this.c0;
            this.f01 = this.f01 + this.r01;
            this.f01 = this.f01 - this.f01 * this.f01 * this.f01 * 0.166666666666666;
            
            this.r10 = this.r10 * this.r1;
            this.r10 = this.r10 + (this.f0 - this.f10) * this.c1;
            this.f10 = this.f10 + this.r10;
            this.f10 = this.f10 - this.f10 * this.f10 * this.f10 * 0.166666666666666;
            this.r11 = this.r11 * this.r1;
            this.r11 = this.r11 + (this.f1 - this.f11) * this.c1;
            this.f11 = this.f11 + this.r11;
            this.f11 = this.f11 - this.f11 * this.f11 * this.f11 * 0.166666666666666;
            this.f0 = frameValues[0][i];
            this.f1 = frameValues[1][i];
            
            resonatedSampleLeft = this.f0 + (this.f00 + this.f10) * 2;
            resonatedSampleRight = this.f1 + (this.f01 + this.f11) * 2;

            frameValues[0][i] = (float) resonatedSampleLeft;
            frameValues[1][i] = (float) resonatedSampleRight;
            
            
        }
        
        filterFlow.next(frameValues);
        
    }

    
}
