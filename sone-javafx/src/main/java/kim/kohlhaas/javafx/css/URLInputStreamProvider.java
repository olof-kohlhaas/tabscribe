package kim.kohlhaas.javafx.css;

import java.io.InputStream;
import java.net.URL;

import com.helger.commons.io.IHasInputStream;

public class URLInputStreamProvider implements IHasInputStream {
    
    private URL url;
    
    public URLInputStreamProvider(URL url) {
        this.url = url;
    }

    @Override
    public InputStream getInputStream() {
        try {
            return url.openStream();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } 
    }

	@Override
	public boolean isReadMultiple() {
		// TODO Auto-generated method stub
		return false;
	}

}
