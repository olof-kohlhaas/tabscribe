package kim.kohlhaas.sone.javafx.binding;

import javafx.beans.binding.IntegerExpression;
import javafx.beans.binding.ObjectBinding;
import kim.kohlhaas.sone.analyze.FloatSpectrogram;
import kim.kohlhaas.sone.analyze.PianoSpectrogram;
import kim.kohlhaas.sone.harmony.Piano88KeyToneSet;
import kim.kohlhaas.sone.harmony.PianoHighKeyToneSet;
import kim.kohlhaas.sone.harmony.Tone;
import kim.kohlhaas.sone.harmony.ToneSet;
import kim.kohlhaas.sone.javafx.RuntimeSettings;

public class FreqIndexToneBinding extends ObjectBinding<Tone> {

    private FloatSpectrogram spectrogram;
    private IntegerExpression freqIndexProperty;
    private ToneSet toneSet = null;
    
    public FreqIndexToneBinding(IntegerExpression freqIndexProperty) {
        this(freqIndexProperty, null);
    }
    
    public FreqIndexToneBinding(IntegerExpression freqIndexProperty, FloatSpectrogram spectrogram) {
        super.bind(freqIndexProperty);
        this.freqIndexProperty = freqIndexProperty;
        
        setSpectrogram(spectrogram);
    }
    
    @Override
    protected Tone computeValue() {
        if (spectrogram instanceof PianoSpectrogram) {
            return ((PianoSpectrogram) spectrogram).getTone(freqIndexProperty.get());
        } else if (spectrogram == null) {
            return null;
        } else {
            return toneSet.getNearestTone(spectrogram.getFreq(freqIndexProperty.get()));
        }
    }

    public FloatSpectrogram getSpectrogram() {
        return spectrogram;
    }

    public void setSpectrogram(FloatSpectrogram spectrogram) {
        this.spectrogram = spectrogram;
        
        if (spectrogram == null || !(spectrogram instanceof PianoSpectrogram)) {
            toneSet = RuntimeSettings.getInstance().getPianoToneSet();
        }
    }
    
    
}
