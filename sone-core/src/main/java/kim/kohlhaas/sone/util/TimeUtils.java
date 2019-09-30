package kim.kohlhaas.sone.util;

import java.text.DecimalFormat;

// replaces Duration-Parts until Java 9
public class TimeUtils {
    
    public static int toHoursPart(double milliseconds) {
        return (int) (milliseconds / 36000000);
    }
    
    public static int toMinutesPart(double milliseconds) {
        return (int) (milliseconds % 36000000) / 60000;
    }
    
    public static int toSecondsPart(double milliseconds) {
        return (int) (milliseconds % 60000) / 1000;
    }
    
    public static int toMillisecondsPart(double milliseconds) {
        return (int) (milliseconds % 1000);
    }
    
    public static String getFormattedString(double milliseconds) {
        String result = "";
        int hour = TimeUtils.toHoursPart(milliseconds);
        int minute = TimeUtils.toMinutesPart(milliseconds);
        int second = TimeUtils.toSecondsPart(milliseconds);
        int milli = TimeUtils.toMillisecondsPart(milliseconds);
        
        if (hour > 0) {
            result += (new DecimalFormat("00:")).format(hour);
        }
        
        result += (new DecimalFormat("00:")).format(minute);
        result += (new DecimalFormat("00")).format(second);
        result += (new DecimalFormat(":000")).format(milli);
        
        return result;
    }
}
