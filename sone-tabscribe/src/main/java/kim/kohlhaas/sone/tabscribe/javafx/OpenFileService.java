package kim.kohlhaas.sone.tabscribe.javafx;

import java.io.File;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import kim.kohlhaas.sone.analyze.FloatSpectrogram;
import kim.kohlhaas.sone.analyze.LinearTimeFloatSpectrogram;
import kim.kohlhaas.sone.signal.FloatAudioSignal;

public class OpenFileService extends Service<Float> {

	final static Logger log = LoggerFactory.getLogger(OpenFileService.class);
	private LinearTimeFloatSpectrogram spectrogram;
	private File file;
	private OpenFileTask task;
	
	
	public OpenFileService(LinearTimeFloatSpectrogram spectrogram) {
		this.spectrogram = spectrogram;
	}
	
	@Override
	protected Task<Float> createTask() {
		task = new OpenFileTask(file, spectrogram);
		return task;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public FloatAudioSignal getAudioSignal() throws InterruptedException, ExecutionException {
		task.get(); // blocking until finished
		return task.getAudioSignal();
	}
	
	public FloatAudioSignal getAudioSignalPlayer() throws InterruptedException, ExecutionException {
		task.get(); // blocking until finished
		return task.getAudioSignalPlayer();
	}
	

}
