package kim.kohlhaas.javafx.css;

public class FormattedSelector {
   
    private String formattedSelectorString;
    
    public FormattedSelector(String selector) {
        // removing whitespace surrounding +, ~, > or one single blank to conform to ph-css-API-style
        this.formattedSelectorString = selector.replaceAll("(\\s*)([\\+,~, ,>])(\\s*)", "$2").trim();
    }
    
    public String getFormattedSelectorString() {
        return formattedSelectorString;
    }
}
