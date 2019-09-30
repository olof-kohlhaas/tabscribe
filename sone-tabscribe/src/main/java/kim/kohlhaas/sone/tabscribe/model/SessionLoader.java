package kim.kohlhaas.sone.tabscribe.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import kim.kohlhaas.sone.event.Updatable;

public interface SessionLoader {
	Session load(File file) throws UnsupportedEncodingException, FileNotFoundException;
	Session load(InputStream inputStream) throws UnsupportedEncodingException;
	Updatable getProgress();
	void setProgress(Updatable progress);
}
