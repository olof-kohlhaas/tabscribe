package kim.kohlhaas.sone.javafx;

import java.util.ResourceBundle;

import kim.kohlhaas.sone.harmony.PianoToneSet;
import kim.kohlhaas.sone.harmony.ToneSet;

public interface Settings {

    ResourceBundle getLabelBundle();
    
    void setLabelBundle(ResourceBundle labelBundle);
    
    ToneSet getToneSet();
    
    PianoToneSet getPianoToneSet();
   
    
}