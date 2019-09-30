package kim.kohlhaas.sone;

public class Environment {
	
    public static int getFFTThreadCount() {  	
        return Runtime.getRuntime().availableProcessors() / 4 + 1;         
    }
    
}
