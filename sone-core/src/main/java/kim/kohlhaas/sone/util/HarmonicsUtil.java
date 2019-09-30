package kim.kohlhaas.sone.util;

public class HarmonicsUtil {
    
    private static final double TWELFTH_ROOT_OF_2 = Math.pow(2.0, 1.0 / 12.0);
    
    public static double pitchSemitoneUp(double frequency) {
        return frequency * TWELFTH_ROOT_OF_2;
    }
    
    public static double pitchSemitoneDown(double frequency) {
        return frequency / TWELFTH_ROOT_OF_2;
    }
    
    /**
     * Returns the factor with which a frequency has to be multiplied to change it by the given semitones .
     * @param semitones number of semitones, positive number to pitch up, negative number to pitch down
     * @return the value of the pitch factor
     */
    public static double getPitchFactor(double semitones) {
        return Math.pow(TWELFTH_ROOT_OF_2, semitones);
    }
    
    /**
     * Returns the number of semitones a frequency has been changed by when multiplying it with the given factor.
     * @param pitchFactor the factor with which a frequency has been changed, greater than 1 means increased, lower than 1 means decreased frequency
     * @return the number of semitones.
     */
    public static double getSemitones(double pitchFactor) {
        return Math.log(pitchFactor) / Math.log(TWELFTH_ROOT_OF_2);
    }
    
    public static double getTonePrecision(int index, double gradientIndex) {
      double precision = 2 * (0.5 - Math.abs(gradientIndex - index));
      if (precision < 0) {
          precision = 0.0;
      }
      
      return precision;
    }
    
    /**
     * 
     * @param keyNumber the number of a standard piano. Keys deceeding or exceeding the range of a piano are counted continuously
     * @return
     */
    public static double getPianoKeyFrequency(int keyNumber) {
        return Math.pow(2, (keyNumber - 49) / 12.0) * 440.0;
    }
    
    
}
