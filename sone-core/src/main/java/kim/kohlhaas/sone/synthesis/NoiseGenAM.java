package kim.kohlhaas.sone.synthesis;

public class NoiseGenAM implements NoiseGen {
    
    private long seed;
    
    public NoiseGenAM() {
        this(0);
    }
    
    public NoiseGenAM(long seed) {
        this.seed = seed;
    }
    
    /* (non-Javadoc)
     * @see kim.kohlhaas.sone.synthesis.NoiseGen#norm()
     */
    @Override
    public double norm() {
       long loc1 = 16807 * (this.seed & 65535);
       long loc2 = 16807 * (this.seed >> 16);
       loc1 = loc1 + ((loc2 & 32767) << 16);
       loc1 = loc1 + (loc2 >> 15);
       if (loc1 > 4151801719L) {
          loc1 = loc1 - 4151801719L;
       }
       return (this.seed = loc1) / 4151801719.0;
    }
    
    /* (non-Javadoc)
     * @see kim.kohlhaas.sone.synthesis.NoiseGen#bipolar()
     */
    @Override
    public double bipolar() {
       long loc1 = 16807 * (this.seed & 65535);
       long loc2 = 16807 * (this.seed >> 16);
       loc1 = loc1 + ((loc2 & 32767) << 16);
       loc1 = loc1 + (loc2 >> 15);
       if (loc1 > 4151801719L) {
          loc1 = loc1 - 4151801719L;
       }
       return (this.seed = loc1) / 4151801719.0 * 2 - 1;
    }
    
}
