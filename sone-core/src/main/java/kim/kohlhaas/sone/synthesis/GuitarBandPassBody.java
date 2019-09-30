package kim.kohlhaas.sone.synthesis;

import javax.sound.sampled.AudioFormat;

import kim.kohlhaas.sone.filter.Filter;
import kim.kohlhaas.sone.filter.FilterFlow;

public class GuitarBandPassBody implements Filter {

    private final Bandpass[] bands = new Bandpass[4];
    
    
    public GuitarBandPassBody(AudioFormat format) {
        double _loc1_ = 4.0;
        this.bands[0] = new Bandpass(format, 4.64 * 4, -100.0, _loc1_);
        this.bands[1] = new Bandpass(format, 96.52, -40.0, _loc1_);
        this.bands[2] = new Bandpass(format, 189.33, -10.0, _loc1_);
        this.bands[3] = new Bandpass(format, 219.95, -5.0, _loc1_);
    }
    
    
    @Override
    public void writeFrame(float[][] frameValues, FilterFlow filterFlow) {
        double _loc5_;
        double _loc6_;

    
        for (int i = 0; i < frameValues[0].length; i ++) {
           _loc5_ = frameValues[0][i] + frameValues[1][i];
           _loc6_ = 0;
           _loc6_ = _loc6_ + this.bands[0].process(_loc5_);
           _loc6_ = _loc6_ + this.bands[1].process(_loc5_);
           _loc6_ = _loc6_ + this.bands[2].process(_loc5_);
           _loc6_ = _loc6_ + this.bands[3].process(_loc5_);
           _loc6_ = _loc6_ * 6;
           frameValues[0][i] = frameValues[0][i] + (float) _loc6_;
           frameValues[1][i] = frameValues[1][i] + (float) _loc6_;
        }
        
        filterFlow.next(frameValues);

    }
    
    private class Bandpass {
        
       private double _hpOutL1;
       private double _lpInL1;
       private final double HIGH_BORDER_FREQ;
       private double _lpOutL1;
       private double _lpOutL2;
       private double _lpB1;
       private double _lpB2;
       private double _hpOutL2;
       private double _lpInL2;
       private double _hpB1;
       private double _hpB2;
       private double _hpInL1;
       private double _hpInL2;
       private final double LOW_BORDER_FREQ;
       private final double PI_SR = 7.12379286528297E-5;
       private double _lpA1;
       private double _lpA2;
       private double _lpA3;
       private double _hpA1;
       private double _hpA2;
       private double _hpA3;
       
       public Bandpass(AudioFormat format, double param1, double param2, double param3) {
          
          HIGH_BORDER_FREQ = format.getSampleRate() / 4.41;
          LOW_BORDER_FREQ = format.getSampleRate() / 14700.0; 
           
          double _loc4_ = param1 - param2;
          double _loc5_ = param1 + param2;
          if(_loc4_ < this.LOW_BORDER_FREQ) {
             _loc4_ = this.LOW_BORDER_FREQ;
          }
          else if(_loc4_ > this.HIGH_BORDER_FREQ) {
             _loc4_ = this.HIGH_BORDER_FREQ;
          }
          if(_loc5_ < this.LOW_BORDER_FREQ) {
             _loc5_ = this.LOW_BORDER_FREQ;
          }
          else if(_loc5_ > this.HIGH_BORDER_FREQ) {
             _loc5_ = this.HIGH_BORDER_FREQ;
          }
          double _loc6_ = 1.0 / Math.tanh(_loc4_ * this.PI_SR);  
          double _loc7_ = _loc6_ * _loc6_;
          double _loc8_ = Math.tanh(_loc5_ * this.PI_SR);
          double _loc9_ = _loc8_ * _loc8_;
          double _loc10_= param3 * _loc6_;
          this._lpA1 = 1.0 / (1.0 + _loc10_ + _loc7_);
          this._lpA2 = 2.0 * this._lpA1;
          this._lpA3 = this._lpA1;
          this._lpB1 = 2.0 * (1.0 - _loc7_) * this._lpA1;
          this._lpB2 = (1.0 - _loc10_ + _loc7_) * this._lpA1;
          double _loc11_ = param3 * _loc8_;
          this._hpA1 = 1.0 / (1.0 + _loc11_ + _loc9_);
          this._hpA2 = -2.0 * this._hpA1;
          this._hpA3 = this._hpA1;
          this._hpB1 = 2.0 * (_loc9_ - 1) * this._hpA1;
          this._hpB2 = (1.0 - _loc11_ + _loc9_) * this._hpA1;
          this._lpInL1 = this._lpInL2 = this._lpOutL1 = this._lpOutL2 = this._hpInL1 = this._hpInL2 = this._hpOutL1 = this._hpOutL2 = 0;
       }
       
       public double process(double param1) {
          double _loc2_;
          double _loc3_;
          _loc2_ = this._lpA1 * param1 + this._lpA2 * this._lpInL1 + this._lpA3 * this._lpInL2 - this._lpB1 * this._lpOutL1 - this._lpB2 * this._lpOutL2;
          _loc3_ = this._hpA1 * _loc2_ + this._hpA2 * this._hpInL1 + this._hpA3 * this._hpInL2 - this._hpB1 * this._hpOutL1 - this._hpB2 * this._hpOutL2;
          this._lpInL2 = this._lpInL1;
          this._lpInL1 = param1;
          this._lpOutL2 = this._lpOutL1;
          this._lpOutL1 = _loc2_;
          this._hpInL2 = this._hpInL1;
          this._hpInL1 = _loc2_;
          this._hpOutL2 = this._hpOutL1;
          this._hpOutL1 = _loc3_;
          return _loc3_;
       }
    }

}
