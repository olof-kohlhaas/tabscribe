package kim.kohlhaas.sone.javafx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.stage.Screen;

public class Environment {
    
    final static Logger log = LoggerFactory.getLogger(Environment.class);
    
    public static int getMaxMulitScreenWidth() {
        int result = 0;
        int index = 0;
        
        for (Screen screen : Screen.getScreens()) {
            log.debug("Screen " + (index++) + ": " + screen.getBounds().getWidth());
            result += screen.getBounds().getWidth();
        }
        
        return result;
    }
    
    public static int getMaxSingleScreenWidth() {
    	int result = 0;
        
        for (Screen screen : Screen.getScreens()) {
        	if (screen.getBounds().getWidth() > result) {
        		result = (int) Math.ceil(screen.getBounds().getWidth());
        	}
        }
        
        return result;
    }
    
}
