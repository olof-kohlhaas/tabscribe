package kim.kohlhaas.sone.analyze;

import java.io.IOException;

import kim.kohlhaas.sone.event.Updatable;
import kim.kohlhaas.sone.signal.FloatAudioSignal;

public abstract class LinearTimeFloatSpectrogram implements FloatSpectrogram {
	
	protected Updatable progress = new Updatable() {

		@Override
		public void update(double done, String message) {
			// default void
		}
		
	};

    @Override
    public double getTimeStep(double millisecond) {
        return (millisecond / getMilliseconds()) * getTimeSteps();
    }
    
    @Override
    public double getMilliseconds(double timeSteps) {
        return timeSteps * getMilliseconds() / getTimeSteps();
    }
    
    public abstract void close();

	public Updatable getProgress() {
		return progress;
	}

	public void setProgress(Updatable progress) {
		this.progress = progress;
	}    
    
    

}
