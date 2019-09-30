package kim.kohlhaas.sone.javafx;

import java.util.ResourceBundle;

import kim.kohlhaas.sone.harmony.Piano88KeyToneSet;
import kim.kohlhaas.sone.harmony.PianoHighKeyToneSet;
import kim.kohlhaas.sone.harmony.PianoShortHighKeyToneSet;
import kim.kohlhaas.sone.harmony.PianoToneSet;
import kim.kohlhaas.sone.harmony.ToneSet;
import kim.kohlhaas.sone.javafx.Settings;

public class RuntimeSettings implements Settings {
    
    private static final Settings INSTANCE = new RuntimeSettings();
    
    private ResourceBundle labelBundle;
    
    private ToneSet toneSet;
    
    private PianoToneSet pianoToneSet;
    		
    RuntimeSettings() {
    	pianoToneSet = new PianoShortHighKeyToneSet();
        toneSet = pianoToneSet;
    }
    
    public static Settings getInstance() {
        return INSTANCE;
    }

    @Override
    public ResourceBundle getLabelBundle() {
        return labelBundle;
    }

    @Override
    public void setLabelBundle(ResourceBundle labelBundle) {
        this.labelBundle = labelBundle;        
    }

	@Override
	public ToneSet getToneSet() {
		return toneSet;
	}

	@Override
	public PianoToneSet getPianoToneSet() {
		return pianoToneSet;
	}

}
