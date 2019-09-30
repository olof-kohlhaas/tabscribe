package kim.kohlhaas.sone.synthesis;

import java.util.Random;

public class NoiseGenRnd implements NoiseGen {

    private Random random;
    
    public NoiseGenRnd() {
        this(0);
    }
    
    public NoiseGenRnd(long seed) {
        this.random = new Random(seed);
    }
    
    @Override
    public double norm() {
        return random.nextDouble();
    }

    @Override
    public double bipolar() {
        return random.nextDouble() * 2.0 - 1.0;
    }

}
