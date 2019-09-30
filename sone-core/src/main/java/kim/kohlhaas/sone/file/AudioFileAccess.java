package kim.kohlhaas.sone.file;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioFileAccess {
	
	public static FormatConverter getInstance(File file) throws IOException, UnsupportedAudioFileException {
		return TarsosFFmpegConverter.getInstance(file);
	}
	
	public static void clear() {
		TarsosFFmpegConverter.clear();
	}
}
