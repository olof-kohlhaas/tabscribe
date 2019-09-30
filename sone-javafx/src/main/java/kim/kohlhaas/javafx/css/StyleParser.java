package kim.kohlhaas.javafx.css;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import com.helger.commons.io.IHasInputStream;
import com.helger.css.ECSSVersion;
import com.helger.css.decl.CascadingStyleSheet;
import com.helger.css.decl.visit.CSSVisitor;
import com.helger.css.reader.CSSReader;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class StyleParser {
    
    public static String getStyleRuleValueAsString(Node node, String selector, String property) throws MalformedURLException {
        String result = null;

        if (Parent.class.isInstance(node)) {
           result = parseStyleSheets(((Parent) node).getStylesheets(), selector, property);  
        } 
        
        // potential matching styles are ignored because of cascading inheritance rules. !important is ignored yet.
        if (result == null) { 
            if (node.getParent() != null) {
                result = getStyleRuleValueAsString(node.getParent(), selector, property);
            } else {
                if (node.getScene() != null) {
                    result = getStyleRuleValueAsString(node.getScene(), selector, property);
                }
            }
        }
        
        return result;
    }
    
    public static String getStyleRuleValueAsString(Scene scene, String selector, String property) throws MalformedURLException {
        String result = null;
        
        // TODO maybe also make traversing down all child nodes possible
        
        if (scene.getStylesheets().isEmpty()) {
            result = parseStyleSheets(scene.getRoot().getStylesheets(), selector, property);
        } else {
            result = parseStyleSheets(scene.getStylesheets(), selector, property);
        }
        
        return result;
    }
    
    public static String getStyleRuleValueAsString(String filePath, String selector, String property) throws MalformedURLException {
        String result = null;
        LinkedList<String> styleSheets = new LinkedList<String>();
        styleSheets.add(filePath);
        
        result = parseStyleSheets(styleSheets, selector, property);
        
        return result;
    }
        
    private static String parseStyleSheets(List<String> styleSheets, String selector, String property) throws MalformedURLException {
        CascadingStyleSheet style;
        SimpleAllAsStringVisitor visitor = new SimpleAllAsStringVisitor(new FormattedSelector(selector), property);
        
        for (String styleSheetPath : styleSheets) {
            URLInputStreamProvider urlInputStreamProvider;

            urlInputStreamProvider = new URLInputStreamProvider(new URL(styleSheetPath));
            style = CSSReader.readFromStream(urlInputStreamProvider, StandardCharsets.UTF_8, ECSSVersion.CSS30);
            
            // multiple matching styles are overwritten because of cascading order rules. !important is ignored yet
            CSSVisitor.visitCSS(style, visitor);  
                      
        }        
        
        return visitor.popPropertyValue();
    }
    
}
