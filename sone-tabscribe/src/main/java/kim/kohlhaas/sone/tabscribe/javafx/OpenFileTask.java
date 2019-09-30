package kim.kohlhaas.sone.tabscribe.javafx;

import java.io.File;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.concurrent.Task;
import kim.kohlhaas.sone.analyze.FFTAnalyzer;
import kim.kohlhaas.sone.analyze.FloatSpectrogram;
import kim.kohlhaas.sone.analyze.LinearTimeFloatSpectrogram;
import kim.kohlhaas.sone.javafx.RuntimeSettings;
import kim.kohlhaas.sone.signal.BufferedFloatAudioSignal;
import kim.kohlhaas.sone.signal.FloatAudioSignal;
import kim.kohlhaas.sone.signal.InMemoryFloatAudioSignal;

public class OpenFileTask extends Task<Float> {
	
	final static Logger log = LoggerFactory.getLogger(OpenFileTask.class);
	private FloatAudioSignal audioSignal;
	private FloatAudioSignal audioSignalPlayer;
	private LinearTimeFloatSpectrogram spectrogram;
	private File file;
	private ResourceBundle bundle = RuntimeSettings.getInstance().getLabelBundle();
	
	public OpenFileTask(File file, LinearTimeFloatSpectrogram spectrogram) {
		this.file = file;
		this.spectrogram = spectrogram;
	}

	@Override
	protected Float call() throws Exception {
		updateMessage(bundle.getString("progress_loading_audio"));
		updateProgress(0.05, 1.0);
        audioSignal = new BufferedFloatAudioSignal(file, FFTAnalyzer.Resolution.FRQ_2.getValue());
        log.info("audiosignal finished");
        updateProgress(0.05, 1.0);
        audioSignalPlayer = new BufferedFloatAudioSignal(file);
        log.info("audiosignal player finished");
        updateProgress(0.1, 1.0);
        updateMessage(bundle.getString("progress_analyzing_audio"));
        spectrogram.setProgress((done, message) -> {
        	updateProgress(0.1 + 0.9 * done, 1.0);
        	updateMessage(message);
        });
        spectrogram.setFloatAudioSignal(audioSignal);
        log.info("spectrogram finished");
        updateProgress(1.0, 1.0);
        updateMessage(bundle.getString("progress_finished_audio"));
        return 1.0f;
	}

	public FloatAudioSignal getAudioSignal() {
		return audioSignal;
	}
	
	public FloatAudioSignal getAudioSignalPlayer() {
		return audioSignalPlayer;
	}

}
