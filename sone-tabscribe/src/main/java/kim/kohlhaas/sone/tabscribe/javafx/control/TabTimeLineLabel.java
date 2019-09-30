package kim.kohlhaas.sone.tabscribe.javafx.control;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Label;

public class TabTimeLineLabel extends Label {

	private ChangeListener<Number> widthListener = null;
	private ChangeListener<Number> heightListener = null;
	
	public ChangeListener<Number> getWidthListener() {
		return widthListener;
	}
	
	public void setWidthListener(ChangeListener<Number> widthListener) {
		if (this.widthListener != null) {
			this.widthProperty().removeListener(this.widthListener);
		}
		this.widthListener = widthListener;
		this.widthProperty().addListener(this.widthListener);
	}
	
	public ChangeListener<Number> getHeightListener() {
		return heightListener;
	}
	
	public void setHeightListener(ChangeListener<Number> heightListener) {
		if (this.heightListener != null) {
			this.heightProperty().removeListener(this.heightListener);
		}
		this.heightListener = heightListener;
		this.heightProperty().addListener(this.heightListener);
	}	
	
}
