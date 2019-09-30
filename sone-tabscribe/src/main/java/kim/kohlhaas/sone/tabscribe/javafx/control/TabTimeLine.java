package kim.kohlhaas.sone.tabscribe.javafx.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import kim.kohlhaas.sone.harmony.Temperament;
import kim.kohlhaas.sone.harmony.ToneFormatter;
import kim.kohlhaas.sone.javafx.event.HoverEvent;
import kim.kohlhaas.sone.tabscribe.model.BeatParser;
import kim.kohlhaas.sone.tabscribe.model.Guitar;
import kim.kohlhaas.sone.tabscribe.model.GuitarString;
import kim.kohlhaas.sone.tabscribe.model.Lyrics;
import kim.kohlhaas.sone.tabscribe.model.LyricsStatement;
import kim.kohlhaas.sone.tabscribe.model.Session;
import kim.kohlhaas.sone.tabscribe.model.TabStatement;
import kim.kohlhaas.sone.tabscribe.model.Track;
import kim.kohlhaas.util.StringUtil;

public class TabTimeLine extends StackPane {
	// TODO consider moving logic for time range and time triggered tabs to its own component and make this one pure view 
	
	private static final double SPACING = 20.5;
	private static final double LINE_GAP = 11.0;
	
	private Session session = null;
	private ChordBar chordBar;
	private Temperament temperament;
	private ToneFormatter.PitchNotation pitchNotation;
	private VBox vBox;
	private double width = 0.0;
	private List<Line> lines = new ArrayList<Line>();
	private Map<Track, Pane> trackPanes = new HashMap<>();
	private Map<TabStatement, TabTimeLineLabel> tabLabels = new HashMap<>();
	private TreeMap<Double, LinkedList<TabStatement>> timeRangeTabs = new TreeMap<>();
	private Map<LyricsStatement, TabTimeLineLabel> lyricsLabels = new HashMap<>();
	private TreeMap<Double, LinkedList<LyricsStatement>> timeRangeLyrics = new TreeMap<>();
	private LinkedList<TabTimeLineLabel> freeLabelPool = new LinkedList<>();
	private LinkedList<TabStatement> currentBeatTabs = new LinkedList<>();
	
	private final DoubleProperty millisecondProperty;
	private double lastMillisecond = 0.0;
	private final DoubleProperty millisecondWidthProperty;
	private final DoubleProperty hoverMillisecondProperty;
	
	private double viewportHeight;
	private double maxLabelHeight = 0.0;
	private double tracksHeight = 0.0;
	private int guitarCount = 0;
	private int lyricsCount = 0;

	private Pane underlay = new Pane();
	private Line position;
	private Line mousePosition;
	private Rectangle beatPosition = new Rectangle(0.0, 0.0, 10.0, 10.0);
	
	private final DoubleProperty mouseXProperty;
	
	private Pane currentPane;
	private Line currentLine;
	private TabTimeLineLabel currentLabel;
	private Track currentTrack;
	private Guitar currentGuitar;
	private Lyrics currentLyrics;
	private String currentText;
	private LyricsStatement currentLyricsStatement;
	private GuitarString currentGuitarString;
	private TabStatement currentStatement;
	private Double currentIntervalStart;
	private boolean currentStartInclusive;
	private double currentMillisecond;
	private Double currentIntervalEnd;
	private Map.Entry<Double, TabStatement> currentStringEntry;
	private Map.Entry<Double, LyricsStatement> currentLyricsEntry;
	private Map.Entry<Double, LinkedList<TabStatement>> currentTabTimeEntry;
	private Map.Entry<Double, LinkedList<LyricsStatement>> currentLyricsTimeEntry;
	private LinkedList<TabStatement> currentStatementList;
	private LinkedList<LyricsStatement> currentLyricsStatementList;
	private boolean isMouseDragging = false;
	private boolean isMouseOutside = true;	
	private int dragPitchDiff = 0;
	private TabTimeLineLabel hiddenEditLabel = null;
	
	private ChangeListener<Number> hoverElsewhereHandler;
	
	
	private final TextField editTextField = new TextField();
	
	public TabTimeLine() {		
		millisecondProperty = new SimpleDoubleProperty(0.0);
		millisecondWidthProperty = new SimpleDoubleProperty();
		mouseXProperty = new SimpleDoubleProperty();
		hoverMillisecondProperty = new SimpleDoubleProperty(0.0);

		beatPosition.setVisible(false);
		beatPosition.setFill(Color.web("#98bcc0ff"));

		
		vBox = new VBox();
		vBox.setSpacing(SPACING);
		vBox.setPadding(new Insets(0.0, 0.0, SPACING, 0.0));
		position = new Line(1, 1, 1, 1);
		position.getStyleClass().add("position-line");
		
		mousePosition = new Line(1, 1, 1, 1);
		mousePosition.getStyleClass().add("position-line");
		mousePosition.setVisible(false);
		
		editTextField.setVisible(false);
		

		editTextField.focusedProperty().addListener((o, oldValue, newValue) -> {
			 if (!newValue) {
				 editTextField.setVisible(false);
				 if (hiddenEditLabel != null) {
					 hiddenEditLabel.setVisible(true);
					 hiddenEditLabel = null;
				 }
			 }
		 });
		
		 editTextField.textProperty().addListener((textObs, textOld, textNew) -> {
			 Text textShape = new Text(textNew);
			 new Scene(new Group(textShape));
			 textShape.applyCss();
			 double textWidthSpace = textShape.getLayoutBounds().getWidth() - 13.0;
			 if (textWidthSpace > 0.0) {
				 editTextField.setPrefWidth(30.0 + textWidthSpace);
			 } else {
				 editTextField.setPrefWidth(30.0);
			 }
			 
		 });
		
		underlay.getChildren().addAll(position, mousePosition, beatPosition);
		
		this.getChildren().addAll(underlay, vBox);
		
		underlay.setPickOnBounds(false);
		mousePosition.setPickOnBounds(false);
		position.setPickOnBounds(false);
		
		this.widthProperty().addListener((observable, oldValue, newValue) -> {
			width = newValue.doubleValue();

			refreshWidth();
			
			if (oldValue.doubleValue() == 0.0 && newValue.doubleValue() > 0.0) {
				plot();
			}
        });
		
		this.heightProperty().addListener((observable, oldValue, newValue) -> {
			refreshPositionLineHeight();
        });
		
		millisecondProperty.addListener((observable, oldValue, newValue) -> {
			lastMillisecond = oldValue.doubleValue();
			updateTabs();
        });
		
		millisecondWidthProperty.addListener((observable, oldValue, newValue) -> {
			updateTabs();
        });
		
		hoverElsewhereHandler = (observable, oldValue, newValue) -> hoverTimeElsewhere();
		
		
		this.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
			mouseXProperty.removeListener(hoverElsewhereHandler);
			double y = e.getSceneY() - this.localToScene(this.getBoundsInLocal()).getMinY();
			isMouseOutside = false;
			this.fireEvent(new HoverEvent(HoverEvent.HOVER_ENTERED, mouseXProperty.get(), y));
		});
		
		this.addEventHandler(MouseEvent.MOUSE_MOVED,e -> setMousePointer(e.getSceneX(), e.getSceneY()));
		
		this.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
			isMouseOutside = true;
			mouseXProperty.addListener(hoverElsewhereHandler);
			if (!isMouseDragging) {
				hideHover();
			}
        });
		
		
				
	}
	
	public void init(BeatParser beatParser) {
		chordBar = new ChordBar(beatParser);
		chordBar.mouseXProperty().bind(mouseXProperty);
		
		chordBar.mouseBeatRightXProperty().addListener((observable, oldValue, newValue) -> {
			Label currentLabel;
			
			if (newValue.doubleValue() > 0.0) {
				if (chordBar.mouseBeatLeftXProperty().doubleValue() >= 0.0) {
					beatPosition.setX(chordBar.mouseBeatLeftXProperty().doubleValue());
				} else {
					beatPosition.setX(0.0);
				}
				beatPosition.setWidth(chordBar.mouseBeatRightXProperty().doubleValue() - chordBar.mouseBeatLeftXProperty().doubleValue());
				beatPosition.setVisible(true);
				
				
				
				if (currentBeatTabs != null) {
					for (TabStatement tab : currentBeatTabs) {
						currentLabel = tabLabels.get(tab);
						if (currentLabel != null) {
							currentLabel.getStyleClass().remove("tab-track-statement-beat");
							currentLabel.getStyleClass().add("tab-track-statement");
						}
					}
				}
				currentBeatTabs = getBeatTabs(chordBar.mouseBeatLeftMilliProperty().doubleValue(), chordBar.mouseBeatRightMilliProperty().doubleValue());
				for (TabStatement tab : currentBeatTabs) {
					currentLabel = tabLabels.get(tab);
					if (currentLabel != null) {
						currentLabel.getStyleClass().remove("tab-track-statement");
						currentLabel.getStyleClass().add("tab-track-statement-beat");
					}
				}
			} else {
				for (TabStatement tab : currentBeatTabs) {
					currentLabel = tabLabels.get(tab);
					if (currentLabel != null) {
						currentLabel.getStyleClass().remove("tab-track-statement-beat");
						currentLabel.getStyleClass().add("tab-track-statement");
					}
				}
				beatPosition.setVisible(false);
			}
		});
		
		chordBar.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
			e.consume(); // prevent event bubbling to parent
		});
		
		chordBar.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
			
			LinkedList<TabStatement> triggeredBeatTabs = getBeatTabs(chordBar.mouseBeatLeftMilliProperty().doubleValue(), chordBar.mouseBeatRightMilliProperty().doubleValue());
			LinkedHashSet<TabStatement> triggeredTabs = new LinkedHashSet<TabStatement>();
			if (!triggeredBeatTabs.isEmpty()) {
				
				for (TabStatement tab : triggeredBeatTabs) {
					triggeredTabs.add(tab);
				}
				
			}

			if (e.getButton() == MouseButton.PRIMARY && !e.isControlDown() && !triggeredTabs.isEmpty()) {
				this.fireEvent(
						new TabTimeLineEvent(TabTimeLineEvent.BEAT_TRIGGERED,  
							triggeredTabs, 
							chordBar.mouseBeatLeftMilliProperty().doubleValue(),
							chordBar.mouseBeatRightMilliProperty().doubleValue()
						)
				);					
			} else if (e.getButton() == MouseButton.SECONDARY || e.getButton() == MouseButton.PRIMARY && e.isControlDown()) {
				this.fireEvent(
						new TabTimeLineEvent(TabTimeLineEvent.BEAT_CONTEXT,  
							triggeredTabs, 
							chordBar.mouseBeatLeftMilliProperty().doubleValue(),
							chordBar.mouseBeatRightMilliProperty().doubleValue(),
							e
						)
				);	
			}
			
			
		});
	}
	
	private LinkedList<TabStatement> getBeatTabs(double fromInclusiveMilli, double toExclusiveMilli) { //TODO consider joining BeatParser combined with Beat-class
		LinkedList<TabStatement> result = new LinkedList<>();
		Map.Entry<Double, LinkedList<TabStatement>> currentTabTimeEntry;
		
		
		currentTabTimeEntry = timeRangeTabs.ceilingEntry(fromInclusiveMilli);
		if (currentTabTimeEntry != null && currentTabTimeEntry.getKey() < toExclusiveMilli) {
			for (TabStatement tab : currentTabTimeEntry.getValue()) {
				result.add(tab);
			}
			while ((currentTabTimeEntry = timeRangeTabs.higherEntry(currentTabTimeEntry.getKey())) != null) {
				if (currentTabTimeEntry.getKey() < toExclusiveMilli) {
					for (TabStatement tab : currentTabTimeEntry.getValue()) {
						result.add(tab);
					}
				} else {
					break;
				}
			}			
		}
		
		
		return result;
	}
	
	private void hoverTimeElsewhere() {
		mousePosition.setVisible(true);  
        mousePosition.setStartX(mouseXProperty.get());
        mousePosition.setEndX(mouseXProperty.get());
	}
	
	private void hideHover() {		
		mouseXProperty.set(-1); //TODO hide hover on spectrogramviewer otherwise
        mousePosition.setVisible(false);
        this.fireEvent(new HoverEvent(HoverEvent.HOVER_EXITED));
    }
	
	private void setMousePointer(double sceneX, double sceneY) {
       
            double y = sceneY - this.localToScene(this.getBoundsInLocal()).getMinY();
           
            mousePosition.setVisible(true);  
            mouseXProperty.set(sceneX - this.localToScene(this.getBoundsInLocal()).getMinX());
            mousePosition.setStartX(mouseXProperty.get());
            mousePosition.setEndX(mouseXProperty.get());

            this.fireEvent(new HoverEvent(HoverEvent.HOVER_MOVED, mouseXProperty.get(), y));

        
    }
	
	private TabTimeLineLabel getFreeLabel(boolean centerHorizontal, boolean centerVertical) {
		TabTimeLineLabel result = null;
		
		result = freeLabelPool.poll();
		
		
		if (result == null) {
			result = new TabTimeLineLabel();
		} else {
			removeStyleClasses(result);
		}
		
		
		//TODO center tabs by its width-mid??
/*		result.setWidthListener((observable, oldValue, newValue) -> {
			 if (centerHorizontal && oldValue.doubleValue() != newValue.doubleValue()) {
				 Label label = (Label)((ReadOnlyDoubleProperty) observable).getBean();
				 label.setLayoutX(label.getLayoutX() - newValue.doubleValue() / 2.0);
			 }				 
		});*/
		
		result.setHeightListener((observable, oldValue, newValue) -> {
			 Label currentLabel = (Label)((ReadOnlyDoubleProperty) observable).getBean();
			 if (centerVertical && oldValue.doubleValue() != newValue.doubleValue()) {
				 Label label = (Label)((ReadOnlyDoubleProperty) observable).getBean();
				 label.setLayoutY(label.getLayoutY() - newValue.doubleValue() / 2.0);
			 }
			 if (maxLabelHeight < newValue.doubleValue()) {
				 maxLabelHeight = newValue.doubleValue();
				 
				 refreshPositionLineHeight();
			 }
		});
		
		return result;
	}
	
	private void removeTabsOffPeriod(double leftBorderMillisecond, double rightBorderMillisecond) {
		currentTabTimeEntry = timeRangeTabs.floorEntry(leftBorderMillisecond);
		if (currentTabTimeEntry != null) {
			recycleTabLabels(currentTabTimeEntry);
			while ((currentTabTimeEntry = timeRangeTabs.lowerEntry(currentTabTimeEntry.getKey())) != null) {
				recycleTabLabels(currentTabTimeEntry);
			}			
		}
		
		currentTabTimeEntry = timeRangeTabs.ceilingEntry(rightBorderMillisecond);
		if (currentTabTimeEntry != null) {
			recycleTabLabels(currentTabTimeEntry);
			while ((currentTabTimeEntry = timeRangeTabs.higherEntry(currentTabTimeEntry.getKey())) != null) {
				recycleTabLabels(currentTabTimeEntry);
			}			
		}
	}
	
	private void recycleTabLabels(Map.Entry<Double, LinkedList<TabStatement>> timeEntry) {
		for (TabStatement statement : timeEntry.getValue()) {
			currentLabel = tabLabels.remove(statement);
			trackPanes.get(statement.getGuitar()).getChildren().remove(currentLabel);
			currentLabel.setText("");
			currentLabel.setVisible(true);
			removeStyleClasses(currentLabel);
			
			currentLabel.setOnMouseReleased(null);
			freeLabelPool.add(currentLabel);
		}
		timeRangeTabs.remove(timeEntry.getKey());
	}
	
	private void removeLyricsOffPeriod(double leftBorderMillisecond, double rightBorderMillisecond) {
		currentLyricsTimeEntry = timeRangeLyrics.floorEntry(leftBorderMillisecond);
		if (currentLyricsTimeEntry != null) {
			recycleLyricsLabels(currentLyricsTimeEntry);
			while ((currentLyricsTimeEntry = timeRangeLyrics.lowerEntry(currentLyricsTimeEntry.getKey())) != null) {
				recycleLyricsLabels(currentLyricsTimeEntry);
			}			
		}
		
		currentLyricsTimeEntry = timeRangeLyrics.ceilingEntry(rightBorderMillisecond);
		if (currentLyricsTimeEntry != null) {
			recycleLyricsLabels(currentLyricsTimeEntry);
			while ((currentLyricsTimeEntry = timeRangeLyrics.higherEntry(currentLyricsTimeEntry.getKey())) != null) {
				recycleLyricsLabels(currentLyricsTimeEntry);
			}			
		}
	}
	
	private void recycleLyricsLabels(Map.Entry<Double, LinkedList<LyricsStatement>> timeEntry) {
		for (LyricsStatement statement : timeEntry.getValue()) {
			currentLabel = lyricsLabels.remove(statement);
			trackPanes.get(statement.getTrack()).getChildren().remove(currentLabel);
			currentLabel.setText("");
			currentLabel.setVisible(true);
			currentLabel.setOnMouseReleased(null);
			currentLabel.setOnMouseDragged(null);
			removeStyleClasses(currentLabel);
			freeLabelPool.add(currentLabel);
		}
		timeRangeLyrics.remove(timeEntry.getKey());
	}	
	
	private void removeStyleClasses(Label label) {
		label.getStyleClass().remove("editable-label");
		label.getStyleClass().remove("tab-track-statement");
		label.getStyleClass().remove("tab-track-statement-beat");
		label.getStyleClass().remove("string-line-label");
		label.getStyleClass().remove("lyrics-track-statement");
		label.getStyleClass().remove("editable-label");
		label.applyCss();
		
	}
		
	public void updateTabs() {
		if (session == null) {
			return;
		}
		
		LinkedHashSet<TabStatement> triggeredTabs = null;
		
		int trackCount = session.getTrackCount();
		double leftBorderMillisecond = millisecondProperty.doubleValue() - millisecondWidthProperty.doubleValue() / 2.0;
		double rightBorderMillisecond = millisecondProperty.doubleValue() + millisecondWidthProperty.doubleValue() / 2.0;	
		
		removeTabsOffPeriod(leftBorderMillisecond, rightBorderMillisecond);
		removeLyricsOffPeriod(leftBorderMillisecond, rightBorderMillisecond);
				
		currentIntervalEnd = millisecondProperty.doubleValue() + millisecondWidthProperty.doubleValue() / 2.0;
		
		for (int i = 0; i < trackCount; i++) {
			currentTrack = session.getTrack(i);
			if (currentTrack.getType() == Track.Type.GUITAR) {
				 currentGuitar = (Guitar) currentTrack; 
				 
				 
				 for (int s = 0; s < currentGuitar.getStringCount(); s++) {
					 currentGuitarString = currentGuitar.getString(s);
					 currentIntervalStart = leftBorderMillisecond;
					 currentStartInclusive = true;
					 
					 while ((currentStringEntry = currentGuitarString.firstStatement(currentIntervalStart, currentStartInclusive, currentIntervalEnd)) != null) {
						 currentStatement = currentStringEntry.getValue();
						 currentIntervalStart = currentStringEntry.getKey();
						 currentStartInclusive = false;
						 currentMillisecond = currentStatement.getMillisecond();
						 
						 currentLabel = tabLabels.get(currentStatement);
						 if (currentLabel == null) {
							 currentLabel = getFreeLabel(true, true);
							 currentLabel.getStyleClass().add("editable-label");
							 currentLabel.getStyleClass().add("tab-track-statement");
							 
							 
							 final TabStatement eventTabStatement = currentStatement;
							 final Pane eventTrackPane = trackPanes.get(currentStatement.getGuitar());
							 final Label eventLabel = currentLabel;
							 
							 currentLabel.setOnMouseReleased(e -> {
								 	if (isMouseDragging) {
								 		double milliOnRelease = hoverMillisecondProperty().get(); //TODO needs to be requested before hiding hover and setting mouseXproperty to -1
								 		if (isMouseOutside) {
								 			hideHover();
								 		}
								 		isMouseDragging = false;

								 		this.fireEvent(new TabTimeLineEvent(TabTimeLineEvent.TAB_DRAGGED, milliOnRelease, eventTabStatement, dragPitchDiff));
								 	} else {
								 		this.fireEvent(new TabTimeLineEvent(TabTimeLineEvent.TAB_CLICKED, eventTabStatement, e));
								 	}
								 	e.consume();
							 });
							 
							 currentLabel.setOnMouseDragged(e -> {

								 isMouseDragging = true;
								 eventLabel.setLayoutX(e.getScreenX() - eventTrackPane.localToScreen(eventTrackPane.getBoundsInLocal()).getMinX());
								 setMousePointer(e.getSceneX(), e.getSceneY());
		
								 
								 dragPitchDiff = (int) Math.round((eventLabel.localToScreen(eventLabel.getBoundsInLocal()).getMinY() + maxLabelHeight/2.0 - e.getScreenY()) / 40.0);
								 eventLabel.setText(eventTabStatement.getPitchShiftedStatement(dragPitchDiff));
		
							 });

							 tabLabels.put(currentStatement, currentLabel);
							 if ((currentStatementList = timeRangeTabs.get(currentStatement.getMillisecond())) == null) {
								 currentStatementList = new LinkedList<>();
								 currentStatementList.add(currentStatement);
								 timeRangeTabs.put(currentStatement.getMillisecond(), currentStatementList);
						 	 } else {
						 		 currentStatementList.add(currentStatement);
						 	 }
							 

							 eventTrackPane.getChildren().add(currentLabel);
						 }

						 currentLabel.setLayoutY(SPACING + (currentGuitar.getStringCount() - (currentStatement.getGuitar().getStringIndex(currentStatement.getGuitarString()) + 1)) * LINE_GAP - currentLabel.getHeight() / 2.0);
						 currentLabel.setLayoutX(this.getWidth() * (currentMillisecond - leftBorderMillisecond) / millisecondWidthProperty.doubleValue());
						 currentLabel.setText(currentStatement.getStatement());
						 if (currentMillisecond < millisecondProperty().doubleValue() && currentMillisecond >= lastMillisecond) {
							 if (triggeredTabs == null) {
								 triggeredTabs = new LinkedHashSet<TabStatement>();
							 }
							 triggeredTabs.add(currentStatement);
						 }
						 currentLabel.applyCss();
						 
						 
					 }
				 }
			} else if (currentTrack.getType() == Track.Type.LYRICS) {
				currentLyrics = (Lyrics) currentTrack;
				currentIntervalStart = leftBorderMillisecond;
				currentStartInclusive = true;
				
				while ((currentLyricsEntry = currentLyrics.firstStatement(currentIntervalStart, currentStartInclusive, currentIntervalEnd)) != null) {
					currentLyricsStatement = currentLyricsEntry.getValue();				
					currentIntervalStart = currentLyricsEntry.getKey();
					currentStartInclusive = false;
					
					currentLabel = lyricsLabels.get(currentLyricsStatement);
					if (currentLabel == null) {
						currentLabel = getFreeLabel(false, false);
						currentLabel.getStyleClass().add("editable-label");
						currentLabel.getStyleClass().add("lyrics-track-statement");
						
						
						final LyricsStatement eventLyricsStatement = currentLyricsStatement;

						final Pane eventTrackPane = trackPanes.get(currentLyricsStatement.getTrack());
						final Label eventLabel = currentLabel;
						final Lyrics eventLyricsTrack = (Lyrics) currentTrack; 
						
						//TODO DRY: same approach as for Guitar-Tracks and Tab-Statements -> generalize 
						
						currentLabel.setOnMouseReleased(e -> {
							double milliOnRelease = hoverMillisecondProperty().get(); //TODO needs to be requested before hiding hover and setting mouseXproperty to -1
							if (isMouseDragging) {
						 		
						 		if (isMouseOutside) {
						 			hideHover();
						 		}
						 		isMouseDragging = false;

						 		this.fireEvent(new TabTimeLineEvent(TabTimeLineEvent.LYRICS_DRAGGED, milliOnRelease, eventLyricsStatement));
						 	} else {					 

						 		if (e.getButton() == MouseButton.SECONDARY || e.getButton() == MouseButton.PRIMARY && e.isControlDown()) {
						 			this.fireEvent(new TabTimeLineEvent(TabTimeLineEvent.LYRICS_CLICKED, eventLyricsStatement, e));
						 		} else {
						 			setEditFieldToPane(eventTrackPane, eventLabel.getLayoutX(), milliOnRelease, eventLyricsTrack, eventLyricsStatement);
						 		}
								 
						 	}
							e.consume();
						});
						
						currentLabel.setOnMouseDragged(e -> {

							 isMouseDragging = true;
							 eventLabel.setLayoutX(e.getScreenX() - eventTrackPane.localToScreen(eventTrackPane.getBoundsInLocal()).getMinX());
							 setMousePointer(e.getSceneX(), e.getSceneY());
	
	
						 });
						
						
						lyricsLabels.put(currentLyricsStatement, currentLabel);
						if ((currentLyricsStatementList = timeRangeLyrics.get(currentLyricsStatement.getMillisecond())) == null) {
							currentLyricsStatementList = new LinkedList<>();
							currentLyricsStatementList.add(currentLyricsStatement);
							timeRangeLyrics.put(currentLyricsStatement.getMillisecond(), currentLyricsStatementList);
						} else {
							currentLyricsStatementList.add(currentLyricsStatement);
						}
						
						trackPanes.get(currentLyricsStatement.getTrack()).getChildren().add(currentLabel);
					}
					
					currentLabel.setLayoutY(SPACING);
					
					currentLabel.setLayoutX(this.getWidth() * (currentLyricsStatement.getMillisecond() - leftBorderMillisecond) / millisecondWidthProperty.doubleValue());
					currentLabel.setText(currentLyricsStatement.getText());
					currentLabel.applyCss();
				}
			}
		}
		if (triggeredTabs != null) {
			this.fireEvent(new TabTimeLineEvent(TabTimeLineEvent.TAB_TRIGGERED, lastMillisecond, millisecondProperty.get(), triggeredTabs));
		}
	}
	
	private void refreshWidth() {
		lines.forEach(line -> {
			line.setEndX(width);
		});
		
		position.setStartX(width / 2 + (width % 2 == 0 ? 0.5 : 0.0));
        position.setEndX(width / 2 + (width % 2 == 0 ? 0.5 : 0.0));
        
        updateTabs();
	}
	
	private void refreshPositionLineHeight() {	
		double filledTracksHeight;
        position.setStartY(0.0);
        mousePosition.setStartY(0.0);
        
        filledTracksHeight = tracksHeight + this.maxLabelHeight * guitarCount + lyricsCount * this.maxLabelHeight + 2.0 + chordBar.getHeight();

        if (viewportHeight > filledTracksHeight) {
        	position.setEndY(viewportHeight);
        	mousePosition.setEndY(viewportHeight);
        	beatPosition.setHeight(viewportHeight);
        } else {
        	// height-properties are not always immediately updated
        	position.setEndY(filledTracksHeight);
        	mousePosition.setEndY(filledTracksHeight);
        	beatPosition.setHeight(filledTracksHeight);
        }
       
	}
	
	private void clearAllChildren(Node node) {
		Pane pane;
		if (node instanceof Pane) {
			pane  = (Pane) node;
			pane.setOnMouseEntered(null);
			pane.setOnMouseMoved(null);
			pane.setOnMouseReleased(null);
			pane.setOnMouseExited(null);
			for (Node child : pane.getChildren()) {
				clearAllChildren(child);
			}
			
			pane.getChildren().clear();
		}
	}
	
	public void plot() {
		if (width == 0.0 || session == null) {
			return;
		}
		
		this.maxLabelHeight = 0.0;
		guitarCount = 0;
		lyricsCount = 0;
			
		int trackCount = session.getTrackCount();
		Pane stringLabelPane;
		Pane tabPane;
		
		
		this.tracksHeight = SPACING * 3;
		
		removeTabsOffPeriod(-0.1, -0.1);
		removeLyricsOffPeriod(-0.1, -0.1);
		
		for (Node node : vBox.getChildren()) {
			clearAllChildren(node);
		}
		
		vBox.getChildren().clear();
		trackPanes.clear();
		
		vBox.getChildren().add(chordBar);
		
		
		for (int i = 0; i < trackCount; i++) {
			 currentPane = new Pane();			 
			 currentTrack = session.getTrack(i);
			 currentPane.setSnapToPixel(true);
			 tracksHeight += SPACING;			 
			 
			 if (currentTrack.getType() == Track.Type.GUITAR) {
				 stringLabelPane = new Pane();
				 stringLabelPane.getStyleClass().add("tab-string-label");
				 tabPane = new Pane();
				 final Pane linePane = new Pane();
				 final Guitar eventGuitar = (Guitar) currentTrack;
				 linePane.setOnMouseMoved(e -> {
					 int lineIndex = (int) Math.round((e.getY() - SPACING) / LINE_GAP);
					 if (lineIndex >= 0) {
						 int stringIndex = linePane.getChildren().size() - (lineIndex + 1);
						 if (stringIndex >= 0 && stringIndex < linePane.getChildren().size()) {
							 linePane.getChildren().forEach( node -> {
								 if (node != linePane.getChildren().get(lineIndex)) {
									 node.getStyleClass().removeAll("tab-string-line-activate");
									 node.getStyleClass().add("tab-string-line");
								 } else {
									 node.getStyleClass().removeAll("tab-string-line");
									 node.getStyleClass().add("tab-string-line-activate"); 
								 }
							 });
		
							 
						 }
					 }
					 
				 });
				 linePane.setOnMouseReleased(e -> {
					 //TODO  DRY: same lineIndex and stringIndex determination as in setOnMouseMoved 
					 int lineIndex = (int) Math.round((e.getY() - SPACING) / LINE_GAP);
					 if (lineIndex >= 0) {
						 int stringIndex = linePane.getChildren().size() - (lineIndex + 1);
						 if (stringIndex >= 0 && stringIndex < linePane.getChildren().size()) {
							 if (e.getButton() == MouseButton.PRIMARY && !e.isControlDown()) {
								 this.fireEvent(new TabTimeLineEvent(TabTimeLineEvent.STRING_CLICKED, hoverMillisecondProperty().get(), eventGuitar.getString(stringIndex)));
							 } else if (e.getButton() == MouseButton.SECONDARY || e.getButton() == MouseButton.PRIMARY && e.isControlDown()) {
								 e.consume();
								 this.fireEvent(new TabTimeLineEvent(TabTimeLineEvent.STRING_RIGHT_CLICKED, hoverMillisecondProperty().get(), eventGuitar.getString(stringIndex)));
							 }
						 }
				     }
				 });
				 linePane.setOnMouseExited(e -> {
					 linePane.getChildren().forEach( node -> {
						 node.getStyleClass().remove("tab-string-line-activate");
						 node.getStyleClass().add("tab-string-line");
					 });
				 });
				 
				 tabPane.setPickOnBounds(false);
				 currentGuitar = (Guitar) currentTrack;
				 guitarCount++;
				 tracksHeight += (currentGuitar.getStringCount() - 1) * LINE_GAP; 
				 for (int l = 0; l < currentGuitar.getStringCount(); l++) {
					 currentGuitarString = currentGuitar.getString(l);
					 currentLine = new Line(0, SPACING + l * LINE_GAP, width, SPACING + l * LINE_GAP);
					 currentLine.setStrokeWidth(1.0d);
					 currentLine.getStyleClass().add("tab-string-line");
					 currentLine.setStrokeType(StrokeType.CENTERED);
					 lines.add(currentLine);
					 linePane.getChildren().add(currentLine);
					 currentLabel = getFreeLabel(false, true);
					 currentLabel.getStyleClass().add("string-line-label");
					 
					 
					 
					 currentLabel.setLayoutX(0.0);
					 currentLabel.setLayoutY(SPACING + (currentGuitar.getStringCount() - (l + 1)) * LINE_GAP);
					 stringLabelPane.getChildren().add(currentLabel);
					 currentLabel.setText(ToneFormatter.simpleFormat(
                             pitchNotation,
                             ToneFormatter.SemitoneCoincide.BOTH, 
                             temperament, currentGuitarString.getEmptyString()) + "\t");
					 
				 }
				 
				 currentPane.getChildren().addAll(linePane, tabPane, stringLabelPane);
				 vBox.getChildren().add(currentPane);
				 trackPanes.put(currentTrack, tabPane);
				 
				 
				 currentLabel = getFreeLabel(false, false);
				 currentLabel.getStyleClass().add("string-line-label");
				 currentLabel.setLayoutX(0.0);
				 currentLabel.setLayoutY(0.0);
				 
				 currentLabel.setText(StringUtil.replaceByLocale(currentTrack.getName()));
				 currentPane.getChildren().add(currentLabel);
			 } else if (currentTrack.getType() == Track.Type.LYRICS) {
				 lyricsCount++;
				 tracksHeight += SPACING;
				 currentPane.setMinHeight(45.0);
				 currentLabel = getFreeLabel(false, false);
				 currentLabel.getStyleClass().add("lyrics-track-head");
				 currentPane.getStyleClass().add("lyrics-track");
				 currentPane.setOnMouseEntered(e -> {
					 currentPane.getScene().setCursor(Cursor.TEXT);
				 });
				 currentPane.setOnMouseExited(e -> {
					 currentPane.getScene().setCursor(Cursor.DEFAULT);
				 });
				
				 final Pane eventPane = currentPane;
				 final Lyrics eventLyricsTrack = (Lyrics) currentTrack; 

				 currentPane.setOnMouseReleased(e -> {
					 double milliOnRelease = hoverMillisecondProperty().get();
					 setEditFieldToPane(eventPane, e.getScreenX() - eventPane.localToScreen(eventPane.getBoundsInLocal()).getMinX(), milliOnRelease, eventLyricsTrack, null);
				 });
				 
				 vBox.getChildren().add(currentPane);
				 trackPanes.put(currentTrack, currentPane);
				 currentLabel.setLayoutX(0.0);
				 currentLabel.setLayoutY(0.0);
				 
				 currentLabel.setText(StringUtil.replaceByLocale(currentTrack.getName()));
				 currentPane.getChildren().add(currentLabel);
			 }
			
			 
			 
			
		}

		
		updateTabs();
		refreshPositionLineHeight();
	}
	

	private void setEditFieldToPane(Pane eventPane, double xPosition, double milliOnRelease, Lyrics eventLyricsTrack, LyricsStatement lyricsStatement) {
		//TODO this is a piece of horrible code like the complete class
		
		final TabTimeLineLabel lyricsLabel;
		Pane editParentPane = (Pane) editTextField.getParent();
		 if (editParentPane != null) {
			 editParentPane.getChildren().remove(editTextField);
		 }
		 eventPane.getChildren().add(editTextField);
		 editTextField.setVisible(true);
		 editTextField.toFront();
		 editTextField.setText("");
		 editTextField.setPrefWidth(30.0);
		 editTextField.setLayoutX(xPosition - 8);
		 editTextField.setLayoutY(SPACING / 2.0 + 4);
		 
		 if (lyricsStatement != null) {
			 lyricsLabel = lyricsLabels.get(lyricsStatement);
			 editTextField.setText(lyricsStatement.getText());
			 lyricsLabel.setVisible(false);
			 hiddenEditLabel = lyricsLabel;
		 } else {
			 lyricsLabel = null;
		 }

		 editTextField.setOnKeyPressed(keyEvent -> {
			 KeyCode keyCode = keyEvent.getCode();
			 if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
				 editTextField.setVisible(false);
				 if (lyricsLabel != null) {
					 lyricsLabel.setVisible(true);
					 hiddenEditLabel = null;
				 }
			 } else if (keyEvent.getCode().equals(KeyCode.ENTER)) {
				 editTextField.setVisible(false);
				 if (lyricsLabel != null) {
					 lyricsLabel.setVisible(true);
				 }
				 hiddenEditLabel = null;
				 if (lyricsStatement == null) {
					 this.fireEvent(new TabTimeLineEvent(TabTimeLineEvent.LYRICS_CREATED, milliOnRelease, eventLyricsTrack, new LyricsStatement(editTextField.getText(), milliOnRelease)));
				 } else {
					 lyricsStatement.setText(editTextField.getText());
					 lyricsLabel.setText(lyricsStatement.getText());

					 this.fireEvent(new TabTimeLineEvent(TabTimeLineEvent.LYRICS_CHANGED, milliOnRelease, eventLyricsTrack, lyricsStatement));
				 }
			 }
		 });
		 editTextField.requestFocus();
	}
	
	@Override
    public boolean isResizable() {
        return true;
    }
	    
    @Override
    public double computeMinWidth(double height) {
        return 100.0;
    }

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
		plot();
	}
    
	public DoubleProperty millisecondProperty() {
        return millisecondProperty;
    }
	
	public DoubleProperty millisecondWidthProperty() {
		return millisecondWidthProperty;
	}
	
	public DoubleProperty mouseXProperty() {
		return mouseXProperty;
	}
	
	public DoubleProperty hoverMillisecondProperty() {
		return hoverMillisecondProperty;
	}
	
	public void setViewportHeight(double viewportHeight) {
		this.viewportHeight = viewportHeight;
		refreshPositionLineHeight();
	}

	public Temperament getTemperament() {
		return temperament;
	}

	public void setTemperament(Temperament temperament) {
		this.temperament = temperament;
	}

	public ToneFormatter.PitchNotation getPitchNotation() {
		return pitchNotation;
	}

	public void setPitchNotation(ToneFormatter.PitchNotation pitchNotation) {
		this.pitchNotation = pitchNotation;
	}
	
	public ChordBar getChordBar() {
		return chordBar;
	}
	
}
