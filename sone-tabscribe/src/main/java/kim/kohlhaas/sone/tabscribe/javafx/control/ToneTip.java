package kim.kohlhaas.sone.tabscribe.javafx.control;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Tooltip;
import kim.kohlhaas.sone.harmony.Temperament;
import kim.kohlhaas.sone.harmony.Tone;
import kim.kohlhaas.sone.harmony.ToneFormatter;
import kim.kohlhaas.sone.javafx.RuntimeSettings;
import kim.kohlhaas.sone.javafx.Settings;
import kim.kohlhaas.sone.util.TimeUtils;

public class ToneTip extends Tooltip {

    private Temperament temperament;
    private Settings settings;
    
    public final ObjectProperty<Tone> toneProperty;
    public final ObjectProperty<Tone> pitchShiftedToneProperty;
    public final DoubleProperty millisecondProperty;
    
    public ToneTip(ToneFormatter.PitchNotation pitchNotation, Temperament temperament, Settings settings) {
    	this.temperament = temperament;
        this.settings = settings;
        
        getStyleClass().add("toneTooltip");
        
        toneProperty = new SimpleObjectProperty<Tone>();
        pitchShiftedToneProperty = new SimpleObjectProperty<Tone>();
        millisecondProperty = new SimpleDoubleProperty();
        
                
        textProperty().bind(new StringBinding() {
            {
                super.bind(toneProperty, pitchShiftedToneProperty, millisecondProperty);
            }
            
            @Override
            protected String computeValue() {
                if (toneProperty.get() == null) {
                    return RuntimeSettings.getInstance().getLabelBundle().getString("time_position") + ": "
                            + TimeUtils.getFormattedString(millisecondProperty.get());
                }
                
                if (pitchShiftedToneProperty.get().equals(toneProperty.get())) {
                    return settings.getLabelBundle().getString("audio_tone") + ": " 
                            + ToneFormatter.simpleFormat(
                                    pitchNotation,
                                    ToneFormatter.SemitoneCoincide.BOTH, 
                                    temperament, toneProperty.get())
                            + "\n" + settings.getLabelBundle().getString("time_position") + ": "
                            + TimeUtils.getFormattedString(millisecondProperty.get());

                } else {
                    return settings.getLabelBundle().getString("audio_tone") 
                            + " (" + RuntimeSettings.getInstance().getLabelBundle().getString("audio_pitch_shifted")  + ")"
                            + ": " 
                            + ToneFormatter.simpleFormat(
                                    pitchNotation,
                                    ToneFormatter.SemitoneCoincide.BOTH, 
                                    temperament, pitchShiftedToneProperty.get())
                            + "\n" + RuntimeSettings.getInstance().getLabelBundle().getString("audio_original_tone")
                            + ": "
                            + ToneFormatter.simpleFormat(
                                    pitchNotation,
                                    ToneFormatter.SemitoneCoincide.BOTH, 
                                    temperament, toneProperty.get())
                            + "\n" + RuntimeSettings.getInstance().getLabelBundle().getString("time_position") + ": "
                            + TimeUtils.getFormattedString(millisecondProperty.get());
                    
                }
            }
            
        });
    }
    
    
    
}
