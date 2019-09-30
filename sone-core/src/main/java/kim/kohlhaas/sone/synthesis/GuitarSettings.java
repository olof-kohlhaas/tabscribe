package kim.kohlhaas.sone.synthesis;

public class GuitarSettings {
    
    private static final GuitarSettings INSTANCE = new GuitarSettings();
    
    public enum Body {
        NONE, SIMPLE, BAND_PASS
    }
    
    private double characterVariation = 0.5;
    private double characterVariationMin = 0.0;
    private double characterVariationMax = 1.0;
    
    private double stereoSpread = 0.2;
    private double stereoSpreadMin = 0.0;
    private double stereoSpreadMax = 1.0;
    
    private double pluckDamping = 0.5;
    private double pluckDampingMin = 0.1;
    private double pluckDampingMax = 0.9;
    
    private double pluckDampingVariation = 0.25;
    private double pluckDampingVariationMin = 0.0;
    private double pluckDampingVariationMax = 0.5;
    
    private double stringDamping = 0.5;
    private double stringDampingMin = 0.1;
    private double stringDampingMax = 0.7;
    
    private double stringDampingVariation = 0.25;
    private double stringDampingVariationMin = 0.0;
    private double stringDampingVariationMax = 0.5;
    
    private double stringTension = 0.0;
    private double stringTensionMin = 0.0;
    private double stringTensionMax = 1.0;
    
    private boolean isStringDampingMagic = true;
    
    private Body body = Body.BAND_PASS;
    

    public static GuitarSettings getInstance() {
        return INSTANCE;
    }
    
    public double getStereoSpread() {
        return stereoSpread;
    }

    public void setStereoSpread(double stereoSpread) {
        this.stereoSpread = stereoSpread;
    }

    public double getStereoSpreadMin() {
        return stereoSpreadMin;
    }

    public void setStereoSpreadMin(double stereoSpreadMin) {
        this.stereoSpreadMin = stereoSpreadMin;
    }

    public double getStereoSpreadMax() {
        return stereoSpreadMax;
    }

    public void setStereoSpreadMax(double stereoSpreadMax) {
        this.stereoSpreadMax = stereoSpreadMax;
    }

    public double getPluckDamping() {
        return pluckDamping;
    }

    public void setPluckDamping(double pluckDamping) {
        this.pluckDamping = pluckDamping;
    }

    public double getPluckDampingMin() {
        return pluckDampingMin;
    }

    public void setPluckDampingMin(double pluckDampingMin) {
        this.pluckDampingMin = pluckDampingMin;
    }

    public double getPluckDampingMax() {
        return pluckDampingMax;
    }

    public void setPluckDampingMax(double pluckDampingMax) {
        this.pluckDampingMax = pluckDampingMax;
    }

    public double getPluckDampingVariation() {
        return pluckDampingVariation;
    }

    public void setPluckDampingVariation(double pluckDampingVariation) {
        this.pluckDampingVariation = pluckDampingVariation;
    }

    public double getPluckDampingVariationMin() {
        return pluckDampingVariationMin;
    }

    public void setPluckDampingVariationMin(double pluckDampingVariationMin) {
        this.pluckDampingVariationMin = pluckDampingVariationMin;
    }

    public double getPluckDampingVariationMax() {
        return pluckDampingVariationMax;
    }

    public void setPluckDampingVariationMax(double pluckDampingVariationMax) {
        this.pluckDampingVariationMax = pluckDampingVariationMax;
    }

    public double getStringDamping() {
        return stringDamping;
    }

    public double getStringDampingMin() {
        return stringDampingMin;
    }

    public double getStringDampingMax() {
        return stringDampingMax;
    }

    public double getStringDampingVariation() {
        return stringDampingVariation;
    }

    public double getStringDampingVariationMin() {
        return stringDampingVariationMin;
    }

    public double getStringDampingVariationMax() {
        return stringDampingVariationMax;
    }

    public boolean isStringDampingMagic() {
        return isStringDampingMagic;
    }

    public double getStringTension() {
        return stringTension;
    }

    public double getStringTensionMin() {
        return stringTensionMin;
    }

    public double getStringTensionMax() {
        return stringTensionMax;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public void setStringDamping(double stringDamping) {
        this.stringDamping = stringDamping;
    }

    public void setStringDampingMin(double stringDampingMin) {
        this.stringDampingMin = stringDampingMin;
    }

    public void setStringDampingMax(double stringDampingMax) {
        this.stringDampingMax = stringDampingMax;
    }

    public void setStringDampingVariation(double stringDampingVariation) {
        this.stringDampingVariation = stringDampingVariation;
    }

    public void setStringDampingVariationMin(double stringDampingVariationMin) {
        this.stringDampingVariationMin = stringDampingVariationMin;
    }

    public void setStringDampingVariationMax(double stringDampingVariationMax) {
        this.stringDampingVariationMax = stringDampingVariationMax;
    }

    public void setStringTension(double stringTension) {
        this.stringTension = stringTension;
    }

    public void setStringTensionMin(double stringTensionMin) {
        this.stringTensionMin = stringTensionMin;
    }

    public void setStringTensionMax(double stringTensionMax) {
        this.stringTensionMax = stringTensionMax;
    }

    public void setStringDampingMagic(boolean stringDampingMagic) {
        this.isStringDampingMagic = stringDampingMagic;
    }

    public double getCharacterVariation() {
        return characterVariation;
    }

    public void setCharacterVariation(double characterVariation) {
        this.characterVariation = characterVariation;
    }

    public double getCharacterVariationMin() {
        return characterVariationMin;
    }

    public double getCharacterVariationMax() {
        return characterVariationMax;
    }
    
    
    
}
