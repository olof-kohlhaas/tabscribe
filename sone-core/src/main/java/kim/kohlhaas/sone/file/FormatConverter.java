package kim.kohlhaas.sone.file;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

public interface FormatConverter {

	File getFile();

	AudioInputStream createInputStream() throws UnsupportedAudioFileException, IOException;

	AudioFormat getSourceFormat();

	AudioFormat getTargetFormat();

	long getFrameLength();

	float getFrameRate();

	double getMillisecondDuration();
	
	void close();

}