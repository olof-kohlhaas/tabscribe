package kim.kohlhaas.sone.tabscribe.javafx.control.spectrumcontext;

import java.util.LinkedHashSet;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectExpression;
import kim.kohlhaas.sone.tabscribe.model.TabStatement;

public class TabsOctaveDecreasableBinding extends BooleanBinding {

	private ObjectExpression<LinkedHashSet<TabStatement>> beatStatements;
	
	public TabsOctaveDecreasableBinding(ObjectExpression<LinkedHashSet<TabStatement>> beatStatements) {
		super.bind(beatStatements);
		this.beatStatements = beatStatements;
	}

	@Override
	protected boolean computeValue() {
		if (beatStatements.get() == null) {
			return false;
		} else if (beatStatements.get().isEmpty()) {
			return false;
		} else {
			return beatStatements.get().stream().allMatch(t -> t.isOctaveDecreasable() || t.isStopTab());
		}
	}

}
