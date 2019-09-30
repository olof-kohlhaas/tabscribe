package kim.kohlhaas.sone.util;

public class MathUtils {
    
    private static final double LOG_OF_2 = Math.log(2.0);

    /**
     * Returns the binary logarithm (base 2) of a double value.
     * @param a a value
     * @return the value lb a, the binary logarithm of a.
     */
    public static double lb(double a){
        return Math.log(a) / LOG_OF_2;
    }
}
