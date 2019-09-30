package kim.kohlhaas.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kim.kohlhaas.sone.javafx.RuntimeSettings;

public class StringUtil {
	
	private static final Pattern localeTermPattern = Pattern.compile("([\\%]+)([\\S]*)([\\%]+)");
	
	public static String replaceByLocale(String text) {
		String prefix;
		String termKey;
		String suffix;
		Matcher matcher = localeTermPattern.matcher(text);
		while (matcher.find())
		{
			prefix = matcher.group(1);
			termKey = matcher.group(2);
			suffix = matcher.group(3);
			
			text = text.replaceFirst(prefix + termKey + suffix, RuntimeSettings.getInstance().getLabelBundle().getString(termKey));

		}
		
		return text;
	}
	
}
