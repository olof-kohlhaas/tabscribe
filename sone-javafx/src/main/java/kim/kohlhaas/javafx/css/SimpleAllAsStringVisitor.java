package kim.kohlhaas.javafx.css;



import com.helger.css.ECSSVersion;
import com.helger.css.ICSSWriterSettings;
import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.CSSSelector;
import com.helger.css.decl.CSSStyleRule;
import com.helger.css.decl.visit.DefaultCSSVisitor;
import com.helger.css.writer.CSSWriterSettings;

public class SimpleAllAsStringVisitor extends DefaultCSSVisitor {
        
    private ICSSWriterSettings cssWriterSettings = new CSSWriterSettings(ECSSVersion.CSS30);
    private boolean isMatching = false;
    private String selector;
    private String property;
    private String propertyValue = null;
    
    public SimpleAllAsStringVisitor(FormattedSelector selector, String property) {
        this.selector = selector.getFormattedSelectorString();
        this.property = property.trim();
    }
    
    @Override
    public void onStyleRuleSelector (final CSSSelector aSelector) {
        if (aSelector.getAsCSSString(cssWriterSettings).equals(selector)) {
            isMatching = true;
        }
    }
    
    @Override
    public void onDeclaration (final CSSDeclaration aDeclaration) {
        if (isMatching && aDeclaration.getProperty().equals(property)) {
            propertyValue = aDeclaration.getExpression().getAsCSSString(cssWriterSettings);
        }
    }
    
    @Override
    public void onEndStyleRule (final CSSStyleRule aStyleRule) {
        isMatching = false;
    }
    
    public String popPropertyValue() {
        String result = propertyValue;
        propertyValue = null;
        return result;
    }
    
}
