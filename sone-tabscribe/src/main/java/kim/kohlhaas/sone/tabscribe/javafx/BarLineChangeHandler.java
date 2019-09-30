package kim.kohlhaas.sone.tabscribe.javafx;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import javafx.scene.control.Spinner;
import kim.kohlhaas.sone.tabscribe.model.BarLine;

public class BarLineChangeHandler implements Subscriber<BarLine> {

	private Spinner<Double> spinnerMeasure;
    private Spinner<Double> spinnerOffset;
    private Spinner<Integer> spinnerBeats;
    private BarLine currentBarLine;
	
	public BarLineChangeHandler(Spinner<Double> spinnerMeasure,
			Spinner<Double> spinnerOffset,
			Spinner<Integer> spinnerBeats) {
		this.spinnerMeasure = spinnerMeasure;
		this.spinnerOffset = spinnerOffset;
		this.spinnerBeats = spinnerBeats;
	}

	@Override
	public void onSubscribe(Subscription subscription) {
		subscription.request(Long.MAX_VALUE);
		
	}

	@Override
	public void onNext(BarLine item) {
		currentBarLine = item;
		spinnerMeasure.getValueFactory().setValue(item.getMeasureMilliDuration());
		spinnerOffset.getValueFactory().setValue(item.getMeasureMilliOffset());
		spinnerBeats.getValueFactory().setValue(item.getBeatsPerMeasure());
	}

	@Override
	public void onError(Throwable throwable) {
		
	}

	@Override
	public void onComplete() {
		
	}

	public BarLine getCurrentBarLine() {
		return currentBarLine;
	}

	public void setCurrentBarLine(BarLine currentBarLine) {
		this.currentBarLine = currentBarLine;
	}
	
	

}
