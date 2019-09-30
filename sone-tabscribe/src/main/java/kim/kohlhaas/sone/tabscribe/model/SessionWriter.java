package kim.kohlhaas.sone.tabscribe.model;

import java.io.File;
import java.io.IOException;

import kim.kohlhaas.sone.event.Updatable;

public interface SessionWriter {
	void write(Session session, File file) throws IOException;
	Updatable getProgress();
	void setProgress(Updatable progress);
}
