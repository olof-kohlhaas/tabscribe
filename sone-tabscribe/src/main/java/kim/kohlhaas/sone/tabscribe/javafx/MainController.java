package kim.kohlhaas.sone.tabscribe.javafx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.BlurType;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import kim.kohlhaas.javafx.color.ColorRange;
import kim.kohlhaas.javafx.color.GrayColorRange;
import kim.kohlhaas.javafx.color.HeatColorRange;
import kim.kohlhaas.javafx.css.StyleParser;
import kim.kohlhaas.sone.Player;
import kim.kohlhaas.sone.analyze.LinearTimeFloatSpectrogram;
import kim.kohlhaas.sone.analyze.PianoSpectrogram;
import kim.kohlhaas.sone.harmony.Temperament;
import kim.kohlhaas.sone.harmony.Tone;
import kim.kohlhaas.sone.harmony.GenericTone;
import kim.kohlhaas.sone.harmony.ToneFormatter;
import kim.kohlhaas.sone.harmony.TwelveToneEqualTemperament;
import kim.kohlhaas.sone.javafx.RuntimeSettings;
import kim.kohlhaas.sone.javafx.binding.FreqIndexToneBinding;
import kim.kohlhaas.sone.javafx.binding.PitchShiftedToneBinding;
import kim.kohlhaas.sone.javafx.control.SpectrogramViewer;
import kim.kohlhaas.sone.javafx.control.Spectrum;
import kim.kohlhaas.sone.javafx.control.WaveScroll;
import kim.kohlhaas.sone.javafx.event.SeekEvent;
import kim.kohlhaas.sone.javafx.event.HoverEvent;
import kim.kohlhaas.sone.signal.FloatAudioSignal;
import kim.kohlhaas.sone.synthesis.GuitarSynthesizer;
import kim.kohlhaas.sone.tabscribe.javafx.control.BeatBar;
import kim.kohlhaas.sone.tabscribe.javafx.control.BeatBarEvent;
import kim.kohlhaas.sone.tabscribe.javafx.control.TabTimeLine;
import kim.kohlhaas.sone.tabscribe.javafx.control.TabTimeLineEvent;
import kim.kohlhaas.sone.tabscribe.javafx.control.ToneTip;
import kim.kohlhaas.sone.tabscribe.javafx.control.spectrumcontext.SpectrumContextEvent;
import kim.kohlhaas.sone.tabscribe.javafx.control.spectrumcontext.SpectrumContextMenu;
import kim.kohlhaas.sone.tabscribe.model.BarLine;
import kim.kohlhaas.sone.tabscribe.model.BeatParser;
import kim.kohlhaas.sone.tabscribe.model.ChordCollection;
import kim.kohlhaas.sone.tabscribe.model.GuitarString;
import kim.kohlhaas.sone.tabscribe.model.Lyrics;
import kim.kohlhaas.sone.tabscribe.model.LyricsStatement;
import kim.kohlhaas.sone.tabscribe.model.Session;
import kim.kohlhaas.sone.tabscribe.model.SessionLoader;
import kim.kohlhaas.sone.tabscribe.model.SessionWriter;
import kim.kohlhaas.sone.tabscribe.model.TabStatement;
import kim.kohlhaas.sone.tabscribe.model.TextDefinitionLoader;
import kim.kohlhaas.sone.tabscribe.model.TextDefinitionWriter;
import kim.kohlhaas.sone.util.HarmonicsUtil;
import kim.kohlhaas.sone.util.PCMUtils;
import kim.kohlhaas.sone.util.TimeUtils;
import kim.kohlhaas.sone.Environment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.animation.FillTransition;


public class MainController {
    final static Logger log = LoggerFactory.getLogger(MainController.class);
    private FloatAudioSignal audioSignalPlayer = null;
    private LinearTimeFloatSpectrogram spectrogram;
    private Player player;
    private StatusPlotterProcess statusPlotterProcess;
    private GuitarSynthesizer tmpGuitar;
    private GuitarSynthesizer tmpBass;
    private GuitarSynthesizer spectroGuitar;
    private Stage modalProgressStage;
    private Stage primaryStage;
    private int pianoToneResolution = 3;
    private Temperament temperament;
    private ToneFormatter.PitchNotation pitchNotation;
    private ColorRange heatColorRange;
    private ColorRange grayColorRange;
    private final ObjectProperty<Tone> contextToneProperty;
    private final ObjectProperty<LinkedHashSet<TabStatement>> contextBeatStatementsProperty;
    private PitchShiftedToneBinding pitchShiftedToneBinding;
    private FreqIndexToneBinding freqIndexToneBinding;
    private ToneTip toneTip;
    private Text playIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.PLAY);
    private Text plusIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.PLUS);
    private Text trashIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.TRASH);
    private Text chevronLeftIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.CHEVRON_LEFT);
    private Text chevronRightIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.CHEVRON_RIGHT);
    private Text backwardIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.BACKWARD);
    private Text forwardIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.FORWARD);
    private Text fastBackwardIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.FAST_BACKWARD);
    private Text fastForwardIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.FAST_FORWARD);
    private Text longArrowUpIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.LONG_ARROW_UP);
    private Text arrowLeftIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.LONG_ARROW_LEFT, "20px");
    private Text arrowRightIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.LONG_ARROW_RIGHT, "20px");
    private Text volumeIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.VOLUME_UP, "20px");
    private Text volumeIconTab = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.VOLUME_UP, "20px");
    private Text volumeIconMaster = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.VOLUME_UP, "20px");
    private Text volumeIconBeat = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.VOLUME_UP, "20px"); 
    private Text pauseIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.PAUSE);
    private Text loopStartingIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.ROTATE_RIGHT);
    private Text loopTerminalIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.ROTATE_LEFT);
    private Text zoomOutIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.SEARCH_MINUS, "20px");
    private Text zoomInIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.SEARCH_PLUS, "20px");
    private OpenFileService openFileService;
    private SessionLoader sessionLoader;
    private SessionWriter sessionWriter;
    private ModalProgressController modalProgressController;
    private Session session;
    private SpectrumContextMenu contextMenuTones;
    private boolean isJustSeeked = false;
    private double lastSeekPosition;
    private float overallVolume = 1.0f;
    private float masterVolume = 1.0f;
    private float audioFileVolume = 1.0f;
    private float beatVolume = 0.0f;
    private float tabVolume = 1.0f;
    private ChangeListener<Number> seekEndListener;
    private FadeTransition fadeInTransition;
    private FadeTransition fadeOutTransition;
    private ResourceBundle bundle = RuntimeSettings.getInstance().getLabelBundle();
    private boolean isPreloading = true;
    private List<Circle> threadCircle = new ArrayList<Circle>();
    private List<FillTransition> passiveTrans = new ArrayList<FillTransition>();
    private Metronome metronome;
    private SnippetPlayer snippetPlayer;
    private BeatParser beatParser;
    private BarLineChangeHandler barLineChangeHandler;
    
    @FXML
    private SpectrogramViewer spectrogramViewer;
    
    @FXML
    private ScrollBar spectrogramScrollBar;
    
    @FXML
    private TabTimeLine tabTimeLine;
    
    @FXML
    private BeatBar beatBar;
    
    @FXML
    private WaveScroll waveScroll;
    
    @FXML
    private SplitPane splitSpec;
    
    @FXML
    private Spectrum spectrumLeft;
    
    @FXML
    private Spectrum spectrumRight;
    
    @FXML
    private BorderPane root;

    @FXML
    private VBox paneTop;

    @FXML
    private ToggleButton playButton;
    
    @FXML
    private ToggleButton maxButton;
    
    @FXML
    private ToggleButton maxGlobalButton;

    @FXML
    private ToggleButton grayRangeButton;
    
    @FXML
    private ToggleButton heatRangeButton;

    @FXML
    private ToggleGroup colorRangeGroup;
    
    @FXML
    private ToggleButton wsola1o8FilterButton;

    @FXML
    private ToggleGroup wsolaToggleGroup;

    @FXML
    private ToggleButton wsola1o4FilterButton;

    @FXML
    private ToggleButton wsola1o3FilterButton;

    @FXML
    private ToggleButton wsola1o2FilterButton;

    @FXML
    private ToggleButton wsola2o3FilterButton;

    @FXML
    private ToggleButton wsola3o4FilterButton;

    @FXML
    private ToggleButton wsola7o8FilterButton;

    @FXML
    private ToggleButton wsola1o1FilterButton;

    @FXML
    private ToggleButton wsola3o2FilterButton;
    
    @FXML
    private ToggleButton wsola5o4FilterButton;
    
    @FXML
    private Slider pitchSlider;
    
    @FXML
    private Slider sliderZoom;
    
    @FXML
    private Slider sliderBeatVolume;
    
    @FXML
    private Slider sliderMasterVolume;
    
    @FXML
    private Slider sliderAudioVolume;
    
    @FXML
    private Slider sliderTabVolume;

    @FXML
    private VBox paneCenter;

    @FXML
    private AnchorPane toneMatrixContainer;
    
    @FXML
    private ToggleButton loopStartingButton;
    
    @FXML
    private ToggleButton loopTerminalButton;
    
    @FXML
    private Tooltip loopStartingTooltip;
    
    @FXML
    private Tooltip loopTerminalTooltip;

    @FXML
    private AnchorPane tablatureContainer;
    
    @FXML
    private HBox threadBox;
    
    @FXML
    private ScrollPane tabScrollPane;

    @FXML
    private VBox paneRight;

    @FXML
    private VBox paneBottom;
    
    @FXML
    private Label labelProgress;

    @FXML
    private Label labelDuration;

    @FXML
    private Label labelPosition;
    
    @FXML
    private Label labelVolumeAudio;
    
    @FXML
    private Label labelVolumeTab;
    
    @FXML
    private Label labelVolumeMaster;
    
    @FXML
    private Label labelVolumeBeat;
    
    @FXML
    private Label labelZoomIn;
    
    @FXML
    private Label labelZoomOut;
    
    @FXML
    private Label labelStepLeft;
    
    @FXML
    private Label labelStepRight;
    
    @FXML
    private Label labelZoomPercent;
    
    @FXML
    private MenuItem menuItemSaveSession;
    
    @FXML
    private MenuItem menuItemSaveSessionAs;
    
    @FXML
    private MenuBar menuBar;
    
    @FXML
    private ProgressBar progressOnTheFly;
    
    @FXML
    private Spinner<Double> spinnerMeasure;
    
    @FXML
    private Spinner<Double> spinnerOffset;
    
    @FXML
    private Spinner<Integer> spinnerBeats;
    
    @FXML
    private Button addBarLineButton;
    
    @FXML
    private Button removeBarLineButton;
    
    @FXML
    private ComboBox<Float> comboMilliStep;
    
    @FXML
    private Button beatBackwardButton;
    
    @FXML
    private Button beatForwardButton;
    
    @FXML
    private Button measureBackwardButton;
    
    @FXML
    private Button measureForwardButton;

    @FXML
    private Button barBackwardButton;
    
    @FXML
    private Button barForwardButton;
    
    public MainController() {
        heatColorRange = new HeatColorRange();
        grayColorRange = new GrayColorRange();
        
        //spectrogram = new FloatAudioSignalSpectrogram();
        spectrogram = new PianoSpectrogram(this.pianoToneResolution, RuntimeSettings.getInstance().getPianoToneSet());
        
        // I know, but the spectrogram stays exchangeable this way
        if (spectrogram instanceof PianoSpectrogram) {
            temperament = ((PianoSpectrogram) spectrogram).getToneSet().getTemperament();
        } else {
            temperament = new TwelveToneEqualTemperament();
        }
        
        pitchNotation = ToneFormatter.PitchNotation.valueOf(
       		 RuntimeSettings.getInstance().getLabelBundle().getString("format_pitch_notation").trim().toUpperCase());
        
        sessionLoader = new TextDefinitionLoader(temperament);
        sessionWriter = new TextDefinitionWriter();       
        
        player = new Player();
        player.setOnTrackFinished(e -> {
            if (statusPlotterProcess.loopStartingProperty().get() > 0.0) {
                try {
                    player.play(statusPlotterProcess.loopStartingProperty().get(), false);
                } catch (LineUnavailableException | UnsupportedAudioFileException | IOException exception) {
                    log.error("exception on loop return after finished track: {}" , exception.toString());
                    showGenericAlert(exception);
                } 
            } else {
                playButton.setSelected(false);
                statusPlotterProcess.stop();
                Platform.runLater(() -> playButton.setGraphic(playIcon));
            }
            
        });
        player.setOnTrackPlay(e -> {
        	metronome.reset();
            playButton.setSelected(true);
            statusPlotterProcess.start();
            Platform.runLater(() -> playButton.setGraphic(pauseIcon));
        });
        player.setOnTrackSeek(e -> {
            statusPlotterProcess.start();
        });
        player.setOnTrackPause(e -> {
            playButton.setSelected(false);
            playButton.setGraphic(playIcon);
    
        });
        
        try {
        	Tone[] spectrumBaseTone = {new Tone(GenericTone.Name.A, GenericTone.Semitone.NONE, -2, temperament)};
        	spectroGuitar = new GuitarSynthesizer("spectrumGuitar", spectrumBaseTone);
            spectroGuitar.open();
        	Tone[] guitarTones = {
        		new Tone(GenericTone.Name.E, GenericTone.Semitone.NONE, 2, temperament),
        		new Tone(GenericTone.Name.A, GenericTone.Semitone.NONE, 2, temperament),
        		new Tone(GenericTone.Name.D, GenericTone.Semitone.NONE, 3, temperament),
        		new Tone(GenericTone.Name.G, GenericTone.Semitone.NONE, 3, temperament),
        		new Tone(GenericTone.Name.B, GenericTone.Semitone.NONE, 3, temperament),
        		new Tone(GenericTone.Name.E, GenericTone.Semitone.NONE, 4, temperament)
        	};
            tmpGuitar = new GuitarSynthesizer("testGuitar", guitarTones);
            tmpGuitar.open();
            Tone[] bassTones = {
        		new Tone(GenericTone.Name.E, GenericTone.Semitone.NONE, 1, temperament),
        		new Tone(GenericTone.Name.A, GenericTone.Semitone.NONE, 1, temperament),
        		new Tone(GenericTone.Name.D, GenericTone.Semitone.NONE, 2, temperament),
        		new Tone(GenericTone.Name.G, GenericTone.Semitone.NONE, 2, temperament)
        	};
            tmpBass = new GuitarSynthesizer("testBass", bassTones);
            tmpBass.open();
        } catch (LineUnavailableException exception) {
            log.error("error on opening synth lines: {}", exception.toString());
            showGenericAlert(exception);
        }
        
        statusPlotterProcess = new StatusPlotterProcess(player);
        statusPlotterProcess.setOnLoopTriggered(e -> {
           seek(statusPlotterProcess.loopStartingProperty().get()); 
        });
        statusPlotterProcess.setInterval(15);
        
        metronome = new Metronome();
        try {
			metronome.open();
		} catch (LineUnavailableException exception) {
			log.error("error on opening metronome lines: {}", exception.toString());
            showGenericAlert(exception);
		}
        
        snippetPlayer = new SnippetPlayer();
        
        modalProgressStage = new Stage();
        modalProgressStage.setTitle("");
        try {
            // Parent modalProgressRoot = FXMLLoader.load(getClass().getResource("ModalProgress.fxml"));
            FXMLLoader modalProgressLoader = new FXMLLoader(getClass().getResource("ModalProgress.fxml"));
            Parent modalProgressRoot = modalProgressLoader.load();
            modalProgressController = modalProgressLoader.getController();
            Scene modalProgressScene = new Scene(modalProgressRoot);
           
            modalProgressStage.setScene(modalProgressScene);
        } catch (IOException exception) {
        	exception.printStackTrace();
            showGenericAlert(exception);
        }
        modalProgressStage.initOwner(primaryStage);
        modalProgressStage.initModality(Modality.WINDOW_MODAL);
        modalProgressStage.initStyle(StageStyle.UNDECORATED);
        modalProgressStage.setOnShown(e -> {
        	centerProgressStage();
        });
        
        contextToneProperty = new SimpleObjectProperty<Tone>();
        contextBeatStatementsProperty = new SimpleObjectProperty<LinkedHashSet<TabStatement>>();
        
        contextMenuTones = new SpectrumContextMenu(RuntimeSettings.getInstance().getLabelBundle(),
        		pitchNotation,
        		new ChordCollection(this.getClass().getResourceAsStream("guitar-6-standard-tuning-chords.txt")),
        		contextToneProperty,
        		contextBeatStatementsProperty);
        contextMenuTones.addEventHandler(SpectrumContextEvent.FRET_SELECTED, fretSelectedEvent -> {
        	fretSelectedEvent.getGuitarString().addTabStatement(
        			new TabStatement("" + fretSelectedEvent.getFret(), beatParser.getFloorBeatMilli(fretSelectedEvent.getMillisecond()))
        	);
        	// TODO, DRY: combination of following three lines is repetitive, e.g. by booleanproperty binding
        	session.setChanged(true);
        	menuItemSaveSession.setDisable(false);
        	tabTimeLine.updateTabs();
        	tabTimeLine.getChordBar().draw(true);
        });
        contextMenuTones.addEventHandler(SpectrumContextEvent.CHORD_SELECTED, chordSelectedEvent -> {
        	chordSelectedEvent.getChord().deployTabStatements(chordSelectedEvent.getGuitar(), chordSelectedEvent.getMillisecond());
        	session.setChanged(true);
        	menuItemSaveSession.setDisable(false);
        	tabTimeLine.updateTabs();
        	tabTimeLine.getChordBar().draw(true);
        });
        contextMenuTones.addEventHandler(SpectrumContextEvent.ADD_MUTE_TABS, addMuteTabsEvent -> {        	
        	TabStatement currentTab;
        	
        	for(int i = 0; i < addMuteTabsEvent.getGuitar().getStringCount(); i++) {
        		currentTab = new TabStatement("X", addMuteTabsEvent.getMillisecond());
        		addMuteTabsEvent.getGuitar().getString(i).addTabStatement(currentTab);
        	}
        	
        	session.setChanged(true);
        	menuItemSaveSession.setDisable(false);
        	tabTimeLine.updateTabs();
        	tabTimeLine.getChordBar().draw(true);
        });
        contextMenuTones.addEventHandler(SpectrumContextEvent.BEAT_DELETED, e -> {
        	e.getTriggeredTabs().stream()
        		.filter(t -> t.getGuitar().equals(e.getGuitar()))
        		.forEach(t -> t.getGuitarString().removeTabStatement(t));
        	
        	session.setChanged(true);
        	menuItemSaveSession.setDisable(false);
        	tabTimeLine.plot();
        	tabTimeLine.getChordBar().draw(true);
        });
        contextMenuTones.addEventHandler(SpectrumContextEvent.BEAT_ARRANGE_UPSTROKE, e -> { // TODO implement arrange handler outside main controller
        	AtomicInteger inc = new AtomicInteger(0);
        	e.getTriggeredTabs().stream()
        		.filter(t -> t.getGuitar().equals(e.getGuitar()))
        		.sorted((t1, t2) -> {
        			int compString = Integer.valueOf(e.getGuitar().getStringIndex(t2.getGuitarString()))
        			.compareTo(Integer.valueOf(e.getGuitar().getStringIndex(t1.getGuitarString())));
        			
        			if (compString != 0) {
        				return compString;
        			} else {
        				return Double.valueOf(t1.getMillisecond()).compareTo(Double.valueOf(t2.getMillisecond()));
        			}
        		})
        		.forEach(t -> {
        			GuitarString currentString = t.getGuitarString();
        			currentString.removeTabStatement(t);        			
        			t.setMillisecond(e.getBeatMilliLeft() + 10 * inc.get());
        			if(t.getMillisecond() >= e.getBeatMilliRight()) {
        				t.setMillisecond(e.getBeatMilliRight() - 10);
        			}
        			t.setVelocity(0.9 - 0.1 * inc.getAndIncrement());
        			currentString.addTabStatement(t);
        		});
        	
        	session.setChanged(true);
        	menuItemSaveSession.setDisable(false);
        	tabTimeLine.plot();
        	tabTimeLine.getChordBar().draw(true);
        });
        contextMenuTones.addEventHandler(SpectrumContextEvent.BEAT_ARRANGE_DOWNSTROKE, e -> {
        	AtomicInteger inc = new AtomicInteger(0);
        	e.getTriggeredTabs().stream()
        		.filter(t -> t.getGuitar().equals(e.getGuitar()))
        		.sorted((t1, t2) -> {
        			int compString = Integer.valueOf(e.getGuitar().getStringIndex(t1.getGuitarString()))
        			.compareTo(Integer.valueOf(e.getGuitar().getStringIndex(t2.getGuitarString())));
        			
        			if (compString != 0) {
        				return compString;
        			} else {
        				return Double.valueOf(t1.getMillisecond()).compareTo(Double.valueOf(t2.getMillisecond()));
        			}
        		})
        		.forEach(t -> {
        			GuitarString currentString = t.getGuitarString();
        			currentString.removeTabStatement(t);        			
        			t.setMillisecond(e.getBeatMilliLeft() + 10 * inc.get());
        			if(t.getMillisecond() >= e.getBeatMilliRight()) {
        				t.setMillisecond(e.getBeatMilliRight() - 10);
        			}
        			t.setVelocity(0.9 - 0.1 * inc.getAndIncrement());
        			currentString.addTabStatement(t);
        		});        	
        	session.setChanged(true);
        	menuItemSaveSession.setDisable(false);
        	tabTimeLine.plot();
        	tabTimeLine.getChordBar().draw(true);
        });
        contextMenuTones.addEventHandler(SpectrumContextEvent.BEAT_ARRANGE_STRAIGHT, e -> {
        	AtomicInteger inc = new AtomicInteger(0);
        	e.getTriggeredTabs().stream()
        		.filter(t -> t.getGuitar().equals(e.getGuitar()))
        		.sorted((t1, t2) -> {
        			int compString = Integer.valueOf(e.getGuitar().getStringIndex(t1.getGuitarString()))
        			.compareTo(Integer.valueOf(e.getGuitar().getStringIndex(t2.getGuitarString())));
        			
        			if (compString != 0) {
        				return compString;
        			} else {
        				return Double.valueOf(t1.getMillisecond()).compareTo(Double.valueOf(t2.getMillisecond()));
        			}
        		})
        		.forEach(t -> {
        			GuitarString currentString = t.getGuitarString();
        			currentString.removeTabStatement(t);        			
        			t.setMillisecond(e.getBeatMilliLeft());
        			t.setVelocity(1.0);
        			currentString.addTabStatement(t);
        		});        	
        	session.setChanged(true);
        	menuItemSaveSession.setDisable(false);
        	tabTimeLine.plot();
        	tabTimeLine.getChordBar().draw(true);
        });
        contextMenuTones.addEventHandler(SpectrumContextEvent.BEAT_LOWER_SEMI, e -> {
        	e.getTriggeredTabs().stream()
        		.filter(t -> t.getGuitar().equals(e.getGuitar()))
        		.forEach(t -> {
        			if (!t.isStopTab()) {
	        			GuitarString currentString = t.getGuitarString();
	        			TabStatement newTab = new TabStatement("" + (t.getStatementFret(0) - 1), t.getMillisecond());
	        			newTab.setVelocity(t.getVelocity());
	        			currentString.removeTabStatement(t);
	        			currentString.addTabStatement(newTab);
        			}
        		});        	
        	session.setChanged(true);
        	menuItemSaveSession.setDisable(false);
        	tabTimeLine.plot();
        	tabTimeLine.getChordBar().draw(true);
        });
        contextMenuTones.addEventHandler(SpectrumContextEvent.BEAT_RAISE_SEMI, e -> {
        	e.getTriggeredTabs().stream()
        		.filter(t -> t.getGuitar().equals(e.getGuitar()))
        		.forEach(t -> {
        			if (!t.isStopTab()) {
	        			GuitarString currentString = t.getGuitarString();
	        			TabStatement newTab = new TabStatement("" + (t.getStatementFret(0) + 1), t.getMillisecond());
	        			newTab.setVelocity(t.getVelocity());
	        			currentString.removeTabStatement(t);
	        			currentString.addTabStatement(newTab);
        			}
        		});        	
        	session.setChanged(true);
        	menuItemSaveSession.setDisable(false);
        	tabTimeLine.plot();
        	tabTimeLine.getChordBar().draw(true);
        });
        contextMenuTones.addEventHandler(SpectrumContextEvent.BEAT_LOWER_OCTAVE, e -> {
        	e.getTriggeredTabs().stream()
        		.filter(t -> t.getGuitar().equals(e.getGuitar()))
        		.forEach(t -> {
        			if (!t.isStopTab()) {
	        			GuitarString currentString = t.getGuitarString();
	        			TabStatement newTab = new TabStatement("" + (t.getStatementFret(0) - 12), t.getMillisecond());
	        			newTab.setVelocity(t.getVelocity());
	        			currentString.removeTabStatement(t);
	        			currentString.addTabStatement(newTab);
        			}
        		});        	
        	session.setChanged(true);
        	menuItemSaveSession.setDisable(false);
        	tabTimeLine.plot();
        	tabTimeLine.getChordBar().draw(true);
        });
        contextMenuTones.addEventHandler(SpectrumContextEvent.BEAT_RAISE_OCTAVE, e -> {
        	e.getTriggeredTabs().stream()
        		.filter(t -> t.getGuitar().equals(e.getGuitar()))
        		.forEach(t -> {
        			if (!t.isStopTab()) {
	        			GuitarString currentString = t.getGuitarString();
	        			TabStatement newTab = new TabStatement("" + (t.getStatementFret(0) + 12), t.getMillisecond());
	        			newTab.setVelocity(t.getVelocity());
	        			currentString.removeTabStatement(t);
	        			currentString.addTabStatement(newTab);
        			}
        		});        	
        	session.setChanged(true);
        	menuItemSaveSession.setDisable(false);
        	tabTimeLine.plot();
        	tabTimeLine.getChordBar().draw(true);
        });
       
    }
    
    @FXML
    void initialize() {   	
    	root.setOpacity(0.0);
    	fadeInTransition = new FadeTransition(Duration.millis(750), root);
        fadeInTransition.setToValue(1.0);
        fadeInTransition.setInterpolator(Interpolator.EASE_OUT);
        fadeInTransition.setOnFinished(e -> {
        	menuBar.setDisable(false);
        	tabTimeLine.getChordBar().setChordImageCachingOn(true);
        });
        
        fadeOutTransition = new FadeTransition(Duration.millis(750), root);
        fadeOutTransition.setToValue(0.0);
        fadeOutTransition.setInterpolator(Interpolator.EASE_OUT);
    	     
        playButton.setGraphic(playIcon);
        labelVolumeAudio.setGraphic(volumeIcon);
        labelVolumeTab.setGraphic(volumeIconTab);
        labelVolumeMaster.setGraphic(volumeIconMaster);
        labelVolumeBeat.setGraphic(volumeIconBeat);
        
        labelZoomIn.setGraphic(zoomInIcon);
        labelZoomOut.setGraphic(zoomOutIcon);
        
        labelStepLeft.setGraphic(arrowLeftIcon);
        labelStepRight.setGraphic(arrowRightIcon);
        
        loopStartingIcon.setScaleY(-1.0);
        loopStartingButton.setGraphic(loopStartingIcon);
        loopTerminalButton.setGraphic(loopTerminalIcon);

        spectrogramScrollBar.maxProperty().bind(spectrogramViewer.maxScroll());
        spectrogramScrollBar.visibleAmountProperty().bind(spectrogramViewer.visibleAmount());
        spectrumLeft.setToneResolution(pianoToneResolution);
        spectrumLeft.setChannel(0);
        spectrumRight.setToneResolution(pianoToneResolution);
        spectrumRight.setChannel(1);
        spectrogramViewer.setToneResolution(this.pianoToneResolution);
        spectrogramViewer.verticalScrollPosition().bindBidirectional(spectrogramScrollBar.valueProperty());
        
        spectrumLeft.millisecondProperty.bind(statusPlotterProcess.getPositionProperty());
        spectrumRight.millisecondProperty.bind(statusPlotterProcess.getPositionProperty());
        spectrogramViewer.millisecondProperty().bind(statusPlotterProcess.getPositionProperty());
        
        spectrogramViewer.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> onSpectrogramClick(e));
        spectrumLeft.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> onSpectrumClick(e));
        spectrumRight.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> onSpectrumClick(e));
        
        maxButton.setGraphic(longArrowUpIcon);
        maxButton.setSelected(!spectrumLeft.isNormalizeToAbsMax() && !spectrumRight.isNormalizeToAbsMax());
        
        spectrogramViewer.addEventHandler(HoverEvent.HOVER_ENTERED, e -> {
            spectrumLeft.setHoverSyncOn(false);
            spectrumRight.setHoverSyncOn(false);
            hoverEntered(e);
         }); 
        spectrogramViewer.addEventHandler(HoverEvent.HOVER_MOVED, e -> hoverMoved(e));
        spectrogramViewer.addEventHandler(HoverEvent.HOVER_EXITED, e -> {
            spectrumLeft.setHoverSyncOn(true);
            spectrumRight.setHoverSyncOn(true);
            hoverExited(e);
        });
        
        tabTimeLine.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
        	if (e.getButton() == MouseButton.SECONDARY || e.getButton() == MouseButton.PRIMARY && e.isControlDown()) {
        		Node node = (Node) e.getSource();
            	contextToneProperty.set(null);
            	contextBeatStatementsProperty.set(null);
            	
            	if (contextMenuTones.isShowing()) {
            		contextMenuTones.hide();
            	}
            	
            	contextMenuTones.show(node,	e.getScreenX(),	e.getScreenY());
            	contextMenuTones.setMillisecondPosition(spectrogramViewer.hoverMillisecondProperty().get());
        	}
        });
        
        tabTimeLine.addEventHandler(HoverEvent.HOVER_ENTERED, e -> hoverEntered(e)); 
        tabTimeLine.addEventHandler(HoverEvent.HOVER_MOVED, e -> hoverMoved(e));
        tabTimeLine.addEventHandler(HoverEvent.HOVER_EXITED, e -> hoverExited(e));
        tabTimeLine.setTemperament(temperament);
        tabTimeLine.setPitchNotation(pitchNotation);
        tabTimeLine.addEventHandler(TabTimeLineEvent.TAB_TRIGGERED, e -> {
        	if (player.isPlaying() && !isJustSeeked) {
	        	triggerTabs(e.getTriggeredTabs());
        	}
        });
        
        tabTimeLine.addEventHandler(TabTimeLineEvent.BEAT_TRIGGERED, e -> {
        	if (!player.isPlaying()) {
	        	triggerTabs(e.getTriggeredTabs());
        	}
        });
        
        tabTimeLine.addEventHandler(TabTimeLineEvent.BEAT_CONTEXT, e -> {
        	Node node = (Node) e.getMouseEvent().getSource();
        	contextToneProperty.set(null);
        	contextBeatStatementsProperty.set(e.getTriggeredTabs());
        	if (contextMenuTones.isShowing()) {
        		contextMenuTones.hide();
        	}
        	
        	contextMenuTones.show(node,	e.getMouseEvent().getScreenX(),	e.getMouseEvent().getScreenY());
        	contextMenuTones.setMillisecondPosition(tabTimeLine.getChordBar().mouseBeatLeftMilliProperty().doubleValue());
        	contextMenuTones.setBeatLeftMilli(e.getLeftBeatMillisecond());
        	contextMenuTones.setBeatRightMilli(e.getRightBeatMillisecond());
        });
        
        tabTimeLine.addEventHandler(TabTimeLineEvent.TAB_CLICKED, e -> {
        	TabStatement tab = e.getClickedTab();
        	MouseEvent mouseEvent = e.getMouseEvent();
        	if (mouseEvent.getButton() == MouseButton.PRIMARY && !mouseEvent.isControlDown()) {
        		if (!tab.isStopTab()) {
	        		if (tab.getGuitar().getName().contains("guitar")) {
	        			tmpGuitar.pluck(tab.getGuitar().getStringIndex(tab.getGuitarString()), tab.getStatementFret(0), tab.getVelocity());
	        		} else if (tab.getGuitar().getName().contains("bass")) {
	        			tmpBass.pluck(tab.getGuitar().getStringIndex(tab.getGuitarString()), tab.getStatementFret(0), tab.getVelocity());
	        		}
        		} else {
	        		if (tab.getGuitar().getName().contains("guitar")) {
	        			tmpGuitar.damp(tab.getGuitar().getStringIndex(tab.getGuitarString()));
	        		} else if (tab.getGuitar().getName().contains("bass")) {
	        			tmpBass.damp(tab.getGuitar().getStringIndex(tab.getGuitarString()));
	        		}
        		}
        	} else if (mouseEvent.getButton() == MouseButton.SECONDARY || mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.isControlDown()) {
        		tab.getGuitarString().removeTabStatement(tab);
        		session.setChanged(true);
            	menuItemSaveSession.setDisable(false);
        		tabTimeLine.plot();
        		tabTimeLine.getChordBar().draw(true);
        	}
        	
        });
        
        tabTimeLine.addEventHandler(TabTimeLineEvent.TAB_DRAGGED, e -> {
        	TabStatement oldTab = e.getDraggedTab();
        	GuitarString guitarString = oldTab.getGuitarString();
        	TabStatement newTab = new TabStatement(oldTab.getPitchShiftedStatement(e.getPitchDiff()), e.getMillisecond());
        	guitarString.removeTabStatement(oldTab);
        	guitarString.addTabStatement(newTab);
        	session.setChanged(true);
        	menuItemSaveSession.setDisable(false);
        	tabTimeLine.plot();
        	tabTimeLine.getChordBar().draw(true);
        });
        
        tabTimeLine.addEventHandler(TabTimeLineEvent.STRING_CLICKED, e -> {
        	e.getGuitarString().addTabStatement(new TabStatement("0", e.getMillisecond()));
        	session.setChanged(true);
        	menuItemSaveSession.setDisable(false);
        	tabTimeLine.plot();
        	tabTimeLine.getChordBar().draw(true);
        });
        
        tabTimeLine.addEventHandler(TabTimeLineEvent.STRING_RIGHT_CLICKED, e -> {
        	e.getGuitarString().addTabStatement(new TabStatement("X", e.getMillisecond()));
        	session.setChanged(true);
        	menuItemSaveSession.setDisable(false);
        	tabTimeLine.plot();
        	tabTimeLine.getChordBar().draw(true);
        });
        
        tabTimeLine.addEventHandler(TabTimeLineEvent.LYRICS_CLICKED, e -> {
        	LyricsStatement lyricsStatement = e.getLyricsStatement();
        	MouseEvent mouseEvent = e.getMouseEvent();
        	if (mouseEvent.getButton() == MouseButton.SECONDARY || mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.isControlDown()) {
        		((Lyrics) lyricsStatement.getTrack()).removeStatement(lyricsStatement);
        		session.setChanged(true);
            	menuItemSaveSession.setDisable(false);
        		tabTimeLine.plot();
        	}
        });
        
        tabTimeLine.addEventHandler(TabTimeLineEvent.LYRICS_DRAGGED, e -> {
        	LyricsStatement oldStatement = e.getLyricsStatement();
        	Lyrics lyricsTrack = (Lyrics) oldStatement.getTrack();
        	LyricsStatement newStatement = new LyricsStatement(oldStatement.getText(), e.getMillisecond());
        	lyricsTrack.removeStatement(oldStatement);
        	lyricsTrack.addStatement(newStatement);
        	session.setChanged(true);
        	menuItemSaveSession.setDisable(false);
        	tabTimeLine.plot();
        });
        
        tabTimeLine.addEventHandler(TabTimeLineEvent.LYRICS_CREATED, e -> {
        	LyricsStatement lyricsStatement = e.getLyricsStatement();
        	if (lyricsStatement.getText().trim().length() > 0) {
        		e.getLyricsTrack().addStatement(lyricsStatement);
        		session.setChanged(true);
            	menuItemSaveSession.setDisable(false);
        		tabTimeLine.plot();
        	}
        });
        
        tabTimeLine.addEventHandler(TabTimeLineEvent.LYRICS_CHANGED, e -> {
        	LyricsStatement lyricsStatement = e.getLyricsStatement();
        	if (lyricsStatement.getText().trim().length() == 0) {
        		((Lyrics) lyricsStatement.getTrack()).removeStatement(lyricsStatement);
        	}
        	session.setChanged(true);
        	menuItemSaveSession.setDisable(false);
        	tabTimeLine.plot();
        });
        
        spectrumLeft.addEventHandler(HoverEvent.HOVER_ENTERED, e -> hoverEntered(e)); 
        spectrumLeft.addEventHandler(HoverEvent.HOVER_MOVED, e -> hoverMoved(e));
        spectrumLeft.addEventHandler(HoverEvent.HOVER_EXITED, e -> hoverExited(e));
        
        spectrumRight.addEventHandler(HoverEvent.HOVER_ENTERED, e -> hoverEntered(e)); 
        spectrumRight.addEventHandler(HoverEvent.HOVER_MOVED, e -> hoverMoved(e));
        spectrumRight.addEventHandler(HoverEvent.HOVER_EXITED, e -> hoverExited(e));
        
        waveScroll.millisecondProperty.bind(statusPlotterProcess.getPositionProperty());
        
        waveScroll.addEventHandler(SeekEvent.SEEK, e -> {       
            	seek(((SeekEvent) e).getMillisecond());
        });
       
        waveScroll.loopStartingProperty().bind(statusPlotterProcess.loopStartingProperty());
        waveScroll.loopTerminalProperty().bind(statusPlotterProcess.loopTerminalProperty());
        
        openFileService = new OpenFileService(spectrogram);
        openFileService.messageProperty().addListener((observable, oldValue, newValue) -> {
        	// TODO solve the messge commands otherwise
        	double from, to, viewFrom, viewTo;
        	int threadNumber;
        	
        	if (newValue.startsWith("cmd:init")) {
        		progressOnTheFly.setVisible(true);
        		threadBox.setVisible(true);
        		labelProgress.setVisible(true);
        	
        	} else if (newValue.startsWith("cmd:rootMeanSquare") && !isPreloading) {
        		progressOnTheFly.setVisible(false);
        		threadBox.setVisible(false);
        		labelProgress.setVisible(false);
        		log.debug("refreshing spectrogram after calculating root mean square");
        		spectrogramViewer.refresh();
        		spectrumLeft.refresh();
    			spectrumRight.refresh();
        		for (FillTransition trans : passiveTrans) {
        			trans.stop();
        			trans.play();
        		}
        	} else if (newValue.startsWith("cmd:startthread") && !isPreloading) {
        		threadNumber = Integer.parseInt(newValue.split("\\(")[1].split("\\)")[0]);
        		passiveTrans.get(threadNumber).stop();
        		passiveTrans.get(threadNumber).play();
        	
        	} else if (newValue.startsWith("cmd:range") && !isPreloading) {
        		threadNumber = Integer.parseInt(newValue.split("\\(")[1].split("\\)")[0]);
        		passiveTrans.get(threadNumber).stop();
        		passiveTrans.get(threadNumber).play();

        		
        		from = audioSignalPlayer.getMillisecondDuration() * Double.parseDouble(newValue.split("=")[1].split("-")[0]) / audioSignalPlayer.getFrameLength();
        		to = audioSignalPlayer.getMillisecondDuration() * Double.parseDouble(newValue.split("=")[1].split("-")[1]) / audioSignalPlayer.getFrameLength(); 
        		
        		viewFrom = spectrogramViewer.millisecondProperty().getValue() - spectrogramViewer.millisecondWidthProperty().getValue() / 2.0;
        		viewTo = spectrogramViewer.millisecondProperty().getValue() + spectrogramViewer.millisecondWidthProperty().getValue() / 2.0;
        		
        		if ((from >= viewFrom && from <= viewTo || to >= viewFrom && to <= viewTo || from <= viewFrom && to >= viewTo) && !isPreloading) {
        			log.debug("refreshing spectrogram after fft, from {} to {} viewFrom {} viewTo {} modal {}", from, to, viewFrom, viewTo, isPreloading);
        			spectrogramViewer.refresh();
        			spectrumLeft.refresh();
        			spectrumRight.refresh();
        		}        		
        	}
        });
        
        //updateMessage(bundle.getString("progress_analyzing_audio") + " " + ((int)((0.1 + 0.9 * done) * 100) + "%"));
        modalProgressController.getMessage().textProperty().bind(Bindings.concat(
        		bundle.getString("progress_analyzing_audio") + " ", 
        		Bindings.createLongBinding(
        				() -> Math.round(openFileService.progressProperty().getValue() * 100),
        				openFileService.progressProperty()
        		),
        		"%"));
        modalProgressController.getProgressBar().progressProperty().bind(openFileService.progressProperty());
        progressOnTheFly.progressProperty().bind(openFileService.progressProperty());
        
        spectrogramViewer.loopStartingProperty().bind(statusPlotterProcess.loopStartingProperty());
        spectrogramViewer.loopTerminalProperty().bind(statusPlotterProcess.loopTerminalProperty());
        
        pitchSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double factor;
            factor = HarmonicsUtil.getPitchFactor(pitchSlider.getValue());
            if (factor < 0.1) {
                factor = 0.1;
            } else if (factor > 4.0) {
                factor = 4.0;
            }
            player.setPitch(factor);
            try {
				snippetPlayer.setPitch(factor);
			} catch (LineUnavailableException e1) {
				log.error("exception during setting pitch of snippet player: {}", e1.toString());
				showGenericAlert(e1);
			}
            switchFrameRate();
        });      
        
        
        spectrogramViewer.hoverIndexProperty().bindBidirectional(spectrumLeft.hoverIndexProperty);
        spectrumLeft.hoverIndexProperty.bindBidirectional(spectrumRight.hoverIndexProperty);        
   
        
        tabTimeLine.mouseXProperty().bindBidirectional(spectrogramViewer.mouseXProperty());
        beatBar.mouseXProperty().bindBidirectional(spectrogramViewer.mouseXProperty());
        
        freqIndexToneBinding = new FreqIndexToneBinding(spectrogramViewer.hoverIndexProperty(), spectrogram);
        pitchShiftedToneBinding = new PitchShiftedToneBinding(temperament, pitchSlider.valueProperty(), freqIndexToneBinding);
        
        toneTip = new ToneTip(pitchNotation, temperament, RuntimeSettings.getInstance());
        toneTip.getStyleClass().add("toneTooltip");
        
        spinnerMeasure.setValueFactory(new DoubleSpinnerValueFactory(100.0, 10000.0, 1000.0, 0.01));
        spinnerOffset.setValueFactory(new DoubleSpinnerValueFactory(-10000.0, 10000.0, 0.0, 0.01));
        spinnerBeats.setValueFactory(new IntegerSpinnerValueFactory(1, 64, 4, 1));
        
        barLineChangeHandler = new BarLineChangeHandler(spinnerMeasure, spinnerOffset, spinnerBeats);
        
        beatParser = new BeatParser(spectrogram.getSamplesPerTimeStep());
        beatParser.millisecondWidthProperty().bind(spectrogramViewer.millisecondWidthProperty());
        beatParser.millisecondProperty().bind(statusPlotterProcess.getPositionProperty());
        beatParser.viewPortWidthProperty().bind(spectrogramViewer.widthProperty());
        beatParser.subscribe(barLineChangeHandler);
        
        metronome.setBeatParser(beatParser);
        
        tabTimeLine.millisecondProperty().bind(statusPlotterProcess.getPositionProperty());
        tabTimeLine.millisecondWidthProperty().bind(spectrogramViewer.millisecondWidthProperty());
        tabTimeLine.hoverMillisecondProperty().bind(spectrogramViewer.hoverMillisecondProperty());
        
        tabTimeLine.init(beatParser);
       
        beatBar.init(beatParser, spectrogram.getSamplesPerTimeStep(), sliderZoom.getMax() / 100.0);
        
        comboMilliStep.getItems().addAll(100.0f, 10.0f, 1.0f, 0.1f, 0.01f);
        comboMilliStep.valueProperty().setValue(10.0f);
        ((DoubleSpinnerValueFactory) spinnerMeasure.getValueFactory()).amountToStepByProperty().bind(comboMilliStep.valueProperty());
        ((DoubleSpinnerValueFactory) spinnerOffset.getValueFactory()).amountToStepByProperty().bind(comboMilliStep.valueProperty());
        
        beatBackwardButton.setGraphic(chevronLeftIcon);
        beatForwardButton.setGraphic(chevronRightIcon);
        measureBackwardButton.setGraphic(backwardIcon);
        measureForwardButton.setGraphic(forwardIcon);
        barBackwardButton.setGraphic(fastBackwardIcon);
        barForwardButton.setGraphic(fastForwardIcon);
        
        barBackwardButton.disableProperty().bind(beatParser.barLineCountProperty().isEqualTo(1));
    	barForwardButton.disableProperty().bind(beatParser.barLineCountProperty().isEqualTo(1));
        
        beatBackwardButton.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
        	Double pos = beatParser.getPreviousBeatMilli();
        	if (pos == null) {
        		pos = beatParser.getLastBeatMilli();
        	}
        	seek(pos);
        });
        
        beatForwardButton.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
        	Double pos = beatParser.getNextBeatMilli();
        	if (pos == null) {
        		pos = beatParser.getFirstBeatMilli();
        	}
        	seek(pos);
        });
        
        measureBackwardButton.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
        	Double pos = beatParser.getPreviousMeasureMilli();
        	if (pos == null) {
        		pos = beatParser.getLastMeasureMilli();
        	}
        	seek(pos);
        });
        
        measureForwardButton.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
        	Double pos = beatParser.getNextMeasureMilli();
        	if (pos == null) {
        		pos = beatParser.getFirstMeasureMilli();
        	}
        	seek(pos);
        });
        
        barBackwardButton.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
        	Double pos = beatParser.getPreviousBarLineMilli();
        	if (pos == null) {
        		pos = beatParser.getLastBarLineMilli();
        	}
        	seek(pos);
        });
        
        barForwardButton.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
        	Double pos = beatParser.getNextBarLineMilli();
        	if (pos == null) {
        		pos = beatParser.getFirstBarLineMilli();
        	}
        	seek(pos);
        });
        
        beatBar.addEventHandler(BeatBarEvent.BEAT_CLICKED, e -> {
        	log.debug("beat clicked");
        	if (!(player.isPaused() || player.isStopped())) {
        		seek(e.getLeftMillisecond());
        	}
        });
        
        beatBar.addEventHandler(BeatBarEvent.MILLISECOND_SELECTED, e -> {
        	log.debug("millisecond selected");
       		seek(e.getMillisecond());
        });
        
        beatBar.addEventHandler(BeatBarEvent.BEAT_PRESSED, e -> {
        	log.debug("beat pressed");
        	if (player.isPaused() || player.isStopped()) {
        		snippetPlayer.play(e.getLeftMillisecond(), e.getRightMillisecond(), true);
        	}
        });
        
        beatBar.addEventHandler(BeatBarEvent.BEAT_RELEASED, e -> {
        	log.debug("beat released");
        	if (player.isPaused() || player.isStopped()) {
        		snippetPlayer.setLooping(false);
        	}
        });
        
        beatBar.addEventHandler(HoverEvent.HOVER_ENTERED, e -> hoverEntered(e)); 
        beatBar.addEventHandler(HoverEvent.HOVER_MOVED, e -> hoverMoved(e));
        beatBar.addEventHandler(HoverEvent.HOVER_EXITED, e -> hoverExited(e));
        
        spectrogramViewer.mouseBeatLeftXProperty().bind(beatBar.mouseBeatLeftXProperty());
        spectrogramViewer.mouseBeatRightXProperty().bind(beatBar.mouseBeatRightXProperty());
        
        addBarLineButton.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
        	BarLine barLine = new BarLine(statusPlotterProcess.getPositionProperty().doubleValue(),
        			BarLine.Type.SINGLE,
        			0.0,
        			barLineChangeHandler.getCurrentBarLine().getMeasureMilliDuration(),
        			barLineChangeHandler.getCurrentBarLine().getBeatsPerMeasure()
        	);
        	       	
        	session.getBarLines().addBarLine(barLine);
        	
        	session.setChanged(true);
    		menuItemSaveSession.setDisable(false);
    		beatParser.reset();
        	beatBar.getBeatWave().draw(true);
        	tabTimeLine.getChordBar().draw(true);
        	metronome.reset();
        });
        
        removeBarLineButton.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
        	BarLine barLine = barLineChangeHandler.getCurrentBarLine();
        	        	
        	if (barLine != session.getBarLines().getDefaultBarLine()) {
	        	session.getBarLines().removeBarLine(barLine);
	        	
	        	session.setChanged(true);
	    		menuItemSaveSession.setDisable(false);
	    		beatParser.reset();
	        	beatBar.getBeatWave().draw(true);
	        	tabTimeLine.getChordBar().draw(true);
	        	metronome.reset();
        	}
        });
                
        spinnerMeasure.valueProperty().addListener((obs, oldValue, newValue) -> {
        	if (barLineChangeHandler.getCurrentBarLine().getMeasureMilliDuration() != newValue) {
	        	beatParser.getBarLineAt(statusPlotterProcess.getPositionProperty().doubleValue()) //TODO getBarLineAt kann zu Nullpointer bei sessionwechsel führen nach verschiebung des beatParser.setSession nach signalverfügbarkeit (linux)
	        		.setMeasureMilliDuration(newValue);
	        	if (!isPreloading) {
	        		session.setChanged(true);
	        		menuItemSaveSession.setDisable(false);
	        	}
	        	beatParser.reset();
	        	beatBar.getBeatWave().draw(true);
	        	tabTimeLine.getChordBar().draw(true);
	        	metronome.reset();
        	}
        });
        
       
        spinnerOffset.valueProperty().addListener((obs, oldValue, newValue) -> {
        	if (barLineChangeHandler.getCurrentBarLine().getMeasureMilliOffset() != newValue) {
	        	beatParser.getBarLineAt(statusPlotterProcess.getPositionProperty().doubleValue())
	    			.setMeasureMilliOffset(newValue);
	        	if (!isPreloading) {
	        		session.setChanged(true);
	        		menuItemSaveSession.setDisable(false);
	        	}
	        	beatParser.reset();
	        	beatBar.getBeatWave().draw(true);
	        	tabTimeLine.getChordBar().draw(true);
	        	metronome.reset();
        	}
        });
        

        spinnerBeats.valueProperty().addListener((obs, oldValue, newValue) -> {
        	if (barLineChangeHandler.getCurrentBarLine().getBeatsPerMeasure() != newValue) {
	        	beatParser.getBarLineAt(statusPlotterProcess.getPositionProperty().doubleValue())
	    			.setBeatsPerMeasure(newValue);
	        	if (!isPreloading) {
	        		session.setChanged(true);
	        		menuItemSaveSession.setDisable(false);
	        	}
	        	beatParser.reset();
	        	beatBar.getBeatWave().draw(true);
	        	tabTimeLine.getChordBar().draw(true);
	        	metronome.reset();
        	}
        });
        
        
        tabScrollPane.viewportBoundsProperty().addListener((observable, oldValue, newValue) -> {
        	tabTimeLine.setViewportHeight(tabScrollPane.getViewportBounds().getHeight());
        });
        
        openSession(this.getClass().getResourceAsStream("session.asg"));
        
        labelPosition.textProperty().bind(new PositionStringBinding(statusPlotterProcess.getPositionProperty())); 
        
        root.opacityProperty().addListener((obs, oldOpacity, newOpacity) -> {
        	overallVolume = newOpacity.floatValue();
        	setOverallVolume();
        });
        
        sliderMasterVolume.valueProperty().addListener((obs, oldVolume, newVolume) -> {
        	masterVolume = newVolume.floatValue() / 100.0f;
        	setOverallVolume();
        });
        
        sliderBeatVolume.valueProperty().addListener((obs, oldVolume, newVolume) -> {
        	beatVolume =  newVolume.floatValue() / 100.0f;
        	metronome.setVolume(masterVolume * beatVolume);
        	if (metronome.getVolume() == 0.0f) {
        		metronome.millisecondProperty().unbind();
        	} else {
        		metronome.millisecondProperty().bind(statusPlotterProcess.getPositionProperty());
        		metronome.reset();
        	}
        });
        
        sliderAudioVolume.valueProperty().addListener((obs, oldVolume, newVolume) -> {
        	audioFileVolume =  newVolume.floatValue() / 100.0f;
        	player.setVolume(masterVolume * audioFileVolume);
        	snippetPlayer.setVolume(masterVolume * audioFileVolume);
        });
        
        sliderTabVolume.valueProperty().addListener((obs, oldVolume, newVolume) -> {
        	tabVolume = newVolume.floatValue() / 100.0f;
        	tmpGuitar.setVolume(masterVolume * tabVolume);
        	tmpBass.setVolume(masterVolume * tabVolume);
        });
        
        sliderZoom.valueProperty().addListener((obs, oldZoom, newZoom) -> {
        	Slider slider = (Slider)((DoubleProperty) obs).getBean();
        	double value = newZoom.doubleValue();
			double stepVal = slider.getMajorTickUnit() / (slider.getMinorTickCount() + 1);
			int step = (int) (value / stepVal);
			double remain = value % stepVal;
			
			if (remain < stepVal / 2) {
				value = stepVal * step;
			} else {
				value = stepVal * step + stepVal;
			}
        	      	
        	spectrogramViewer.setZoomXScale(value / 100.0);
        	
        });
        
        
        labelZoomPercent.textProperty().bind(Bindings.concat(
        		Bindings.createDoubleBinding(
        				() -> {
        					double value = sliderZoom.valueProperty().getValue();
        					double stepVal = sliderZoom.getMajorTickUnit() / (sliderZoom.getMinorTickCount() + 1);
        					int step = (int) (value / stepVal);
        					double remain = value % stepVal;
        					
        					if (remain < stepVal / 2) {
        						return stepVal * step;
        					} else {
        						return stepVal * step + stepVal;
        					}
        				},
        				sliderZoom.valueProperty()
        		)
        		, "%")
        );
                
        addBarLineButton.setGraphic(plusIcon);
        removeBarLineButton.setGraphic(trashIcon);
    }
        
    private void triggerTabs(LinkedHashSet<TabStatement> triggeredTabs) {
    	TabStatement lastTab = null;
    	for (TabStatement tab : triggeredTabs) {
    		if (lastTab != null && tab.getMillisecond() > lastTab.getMillisecond()) {
    			try {
					Thread.sleep((long)((tab.getMillisecond() - lastTab.getMillisecond()) * (1.0 / player.getTimeStretch())));
				} catch (InterruptedException e1) {
					Thread.currentThread().interrupt();
					e1.printStackTrace();
				}
    		}
    		if (tab.getGuitar().getName().contains("guitar")) {
    			if (tab.isStopTab()) {
    				tmpGuitar.damp(tab.getGuitar().getStringIndex(tab.getGuitarString()));
    			} else {
    				tmpGuitar.pluck(tab.getGuitar().getStringIndex(tab.getGuitarString()), tab.getStatementFret(0), tab.getVelocity());
    			}

    		} else if (tab.getGuitar().getName().contains("bass")) {
    			if (tab.isStopTab()) {
    				tmpBass.damp(tab.getGuitar().getStringIndex(tab.getGuitarString()));
    			} else {
    				tmpBass.pluck(tab.getGuitar().getStringIndex(tab.getGuitarString()), tab.getStatementFret(0), tab.getVelocity());
    			}
    		}
    		lastTab = tab;
    	}
    }
    
    private void switchFrameRate() {
    	if (player.getTimeStretch() == 1.0 && player.getPitch() == 1.0) {
    		statusPlotterProcess.setInterval(15);
    	} else {
    		statusPlotterProcess.setInterval(50);
    	}
    }
    
    private void setOverallVolume() {
    	player.setVolume(masterVolume * audioFileVolume * overallVolume);
    	snippetPlayer.setVolume(masterVolume * audioFileVolume * overallVolume);
    	spectroGuitar.setVolume(masterVolume * overallVolume);
    	metronome.setVolume(masterVolume * beatVolume * overallVolume);
    	tmpGuitar.setVolume(masterVolume * tabVolume * overallVolume);
    	tmpBass.setVolume(masterVolume * tabVolume * overallVolume);
    }
    
    private void onSpectrogramClick(MouseEvent e) {
        double clickMillisecondPosition = spectrogramViewer.hoverMillisecondProperty().get();
        if (e.getButton() == MouseButton.PRIMARY && !e.isControlDown()) {
        	contextMenuTones.hide();
	        if (spectrogramViewer.timeSelectOnProperty().get()) {
	            if (loopStartingButton.isSelected() && statusPlotterProcess.loopStartingProperty().get() == 0.0) {
	                if (clickMillisecondPosition > 0.0) {
	                    statusPlotterProcess.loopStartingProperty().set(clickMillisecondPosition);
	                } else {
	                    statusPlotterProcess.loopStartingProperty().set(0.0);
	                }
	                loopStartingButton.getStyleClass().add("loop-icon-active");
	                loopStartingButton.getTooltip().setText(RuntimeSettings.getInstance().getLabelBundle().getString("command_remove_loop_starting_point") );
	            } else if (loopTerminalButton.isSelected() && statusPlotterProcess.loopTerminalProperty().get() == 0.0) {
	                if (clickMillisecondPosition < PCMUtils.getMilliseconds(audioSignalPlayer)) {
	                    statusPlotterProcess.loopTerminalProperty().set(clickMillisecondPosition);
	                } else {
	                    statusPlotterProcess.loopTerminalProperty().set(PCMUtils.getMilliseconds(audioSignalPlayer));
	                }
	                loopTerminalButton.getStyleClass().add("loop-icon-active");
	                loopTerminalButton.getTooltip().setText(RuntimeSettings.getInstance().getLabelBundle().getString("command_remove_loop_terminal_point") );
	            }
	            
	            spectrogramViewer.timeSelectOnProperty().set(false);
	        } else {
	            playPianoKey();
	        }
        } else if (e.getButton() == MouseButton.SECONDARY || e.getButton() == MouseButton.PRIMARY && e.isControlDown()) {
        	
        	Node node = (Node) e.getSource();
        	contextToneProperty.set(pitchShiftedToneBinding.get());
        	contextBeatStatementsProperty.set(null);
        	
        	if (contextMenuTones.isShowing()) {
        		contextMenuTones.hide();
        	}
        	contextMenuTones.show(node,	e.getScreenX(),	e.getScreenY());
        	
        	
        	contextMenuTones.setMillisecondPosition(spectrogramViewer.hoverMillisecondProperty().get());
        	
        }
    }
    
    private void onSpectrumClick(MouseEvent e) {
    	if (e.getButton() == MouseButton.PRIMARY && !e.isControlDown()) {
    		playPianoKey();
    	} else if (e.getButton() == MouseButton.SECONDARY || e.getButton() == MouseButton.PRIMARY && e.isControlDown()) {
    		Node node = (Node) e.getSource();
    		    		
        	contextToneProperty.set(pitchShiftedToneBinding.get());
        	contextBeatStatementsProperty.set(null);
        	
        	if (contextMenuTones.isShowing()) {
        		contextMenuTones.hide();
        	}
        	contextMenuTones.show(node,	e.getScreenX(),	e.getScreenY());
        	
        	
        	contextMenuTones.setMillisecondPosition(((Spectrum) node).millisecondProperty.get());
    	}
    }
    
    
    private void playPianoKey() {
        spectroGuitar.playTone(0, temperament.getPitchShiftedTone(freqIndexToneBinding.get(), (int) pitchSlider.getValue()), 1.0);
    }
    
    private void hoverEntered(HoverEvent e) {
        Node node = (Node) e.getSource();
        
        Tooltip.install(node, toneTip);
        toneTip.pitchShiftedToneProperty.bind(pitchShiftedToneBinding);
        toneTip.toneProperty.bind(freqIndexToneBinding);
        toneTip.millisecondProperty.unbind();
        
                
        if (node instanceof SpectrogramViewer) {
            toneTip.millisecondProperty.bind(spectrogramViewer.hoverMillisecondProperty());
        } else if (node instanceof TabTimeLine) {
        	toneTip.toneProperty.unbind();
        	toneTip.toneProperty.set(null);
        	toneTip.millisecondProperty.bind(spectrogramViewer.hoverMillisecondProperty());
        } else if (node instanceof BeatBar) {
        	toneTip.toneProperty.unbind();
        	toneTip.toneProperty.set(null);
        	toneTip.millisecondProperty.bind(beatBar.hoverMillisecondProperty());
        } else {
            toneTip.millisecondProperty.bind(statusPlotterProcess.getPositionProperty());
        }
    }
    
    private void hoverMoved(HoverEvent e) {
        Node node = (Node) e.getSource();
        
        toneTip.show(node, 
                node.localToScreen(node.getBoundsInLocal()).getMinX() + e.getLocalX() + 10.0,
                node.localToScreen(node.getBoundsInLocal()).getMinY() + e.getLocalY() - toneTip.getHeight());
    }
    
    private void hoverExited(HoverEvent e) {
        Node node = (Node) e.getSource();
        
        toneTip.hide();
        toneTip.pitchShiftedToneProperty.unbind();
        toneTip.toneProperty.unbind();
        toneTip.millisecondProperty.unbind();
        Tooltip.uninstall(node, toneTip);
    }
    
    
    private boolean isCurrentSessionQuitConfirmed() {
    	boolean confirmed = true;
    	
    	if (session.isChanged()) {
    		
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.initOwner(primaryStage);
			alert.initStyle(StageStyle.UTILITY);
			alert.setTitle(RuntimeSettings.getInstance().getLabelBundle().getString("alert_session_modified_title"));
			alert.setHeaderText(RuntimeSettings.getInstance().getLabelBundle().getString("alert_session_modified_header"));
			alert.setContentText(RuntimeSettings.getInstance().getLabelBundle().getString("alert_session_modified_question"));
			ButtonType okButton = new ButtonType(RuntimeSettings.getInstance().getLabelBundle().getString("alert_yes"), ButtonBar.ButtonData.YES);
			ButtonType noButton = new ButtonType(RuntimeSettings.getInstance().getLabelBundle().getString("alert_no"), ButtonBar.ButtonData.NO);
			ButtonType cancelButton = new ButtonType(RuntimeSettings.getInstance().getLabelBundle().getString("alert_cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
			alert.getButtonTypes().setAll(okButton, noButton, cancelButton);
			Optional<ButtonType> response = alert.showAndWait();
				if (response.get().getButtonData() == ButtonBar.ButtonData.YES) {
					if (session.getSessionFile() != null) {
						saveSession();
						confirmed = true;
					} else {
						saveSession();
						confirmed = false;
					} 
				} else if (response.get().getButtonData() == ButtonBar.ButtonData.NO) {
					confirmed = true;
				} else if (response.get().getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE) {
					confirmed = false;
				} 
        }
    	
    	return confirmed;
    }
    
    @FXML
    void onMenuExit(ActionEvent event) {
        log.debug("Menu exit.");
        if (isCurrentSessionQuitConfirmed()) {
        	Platform.exit();
            System.exit(0);
        }
    }

    @FXML
    void onMenuOpenFile(ActionEvent event) {
        log.debug("Open FileChooser ");
        File file;
        
        if (isCurrentSessionQuitConfirmed()) {

	        FileChooser fileChooser = new FileChooser();
	        fileChooser.setTitle(RuntimeSettings.getInstance().getLabelBundle().getString("dialog_open_audio_file_title")); 
	        fileChooser.getExtensionFilters()
	                .addAll(new ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac", "*.m4a", "*.ogg", "*.aif", "*.aiff", "*.mp4", "*.flac"));
	
	        file = fileChooser.showOpenDialog(getStage());
	        log.debug("File selected: " + (file == null ? "empty" : file.getAbsolutePath()));
	        if (file != null) {
	        	menuBar.setDisable(true);
	        	
	        	if(session.getAudioFile() == null) {
		        	openSession(this.getClass().getResourceAsStream("session.asg"));
		            openFile(file);
	        	} else {
	        		getStage().setTitle(RuntimeSettings.getInstance().getLabelBundle().getString("application_title"));
	        		menuItemSaveSessionAs.setDisable(true);
	        		tabTimeLine.getChordBar().setChordImageCachingOn(false);
	        		fadeOutTransition.setOnFinished(e -> {
	        			openSession(this.getClass().getResourceAsStream("session.asg"));
	    	            openFile(file);
	        		});
	        		fadeOutTransition.play();
	        	}
	        } 
        }
    }
    
    @FXML
    void onMenuOpenSession(ActionEvent event) {
        log.debug("Open FileChooser Session" + event.getSource());
        File file; 
        
        if(isCurrentSessionQuitConfirmed()) {
        
	        FileChooser fileChooser = new FileChooser();
	        fileChooser.setTitle(RuntimeSettings.getInstance().getLabelBundle().getString("dialog_open_session_file_title"));        fileChooser.getExtensionFilters()
	                .addAll(new ExtensionFilter("Session Files", "*.asg"));
	
	        file = fileChooser.showOpenDialog(getStage());
	        log.debug("File selected: " + (file == null ? "empty" : file.getAbsolutePath()));
	        if (file != null) {
	        	menuBar.setDisable(true);
	        	if(session.getAudioFile() == null) {
		            log.debug("open session");
		            openSession(file);
	        	} else {
	        		getStage().setTitle(RuntimeSettings.getInstance().getLabelBundle().getString("application_title"));
	        		menuItemSaveSessionAs.setDisable(true);
	        		tabTimeLine.getChordBar().setChordImageCachingOn(false);
	        		fadeOutTransition.setOnFinished(e -> {
	        			log.debug("open session");
	    	            openSession(file);
	        		});
	        		fadeOutTransition.play();
	        	}
	        }
        }
    }
    
    @FXML
    void onMenuSaveSession(ActionEvent event) {
        log.debug("Save Session" + event.getSource());
        
        saveSession();
    }
    
    @FXML
    void onMenuSaveSessionAs(ActionEvent event) {
        log.debug("Save Session As..." + event.getSource());
        
        session.setSessionFile(null);
        saveSession();
    }
    

    
    @FXML
    void togglePlayButton(MouseEvent event) {
        boolean isButtonSelected = ((ToggleButton)event.getSource()).isSelected();

        if (isButtonSelected) {
            playTrack();
        }
        else if (!isButtonSelected) {
            pauseTrack();
        }
    }
    
    @FXML
    void toggleMaxButton(MouseEvent event) {
        boolean isButtonSelected = ((ToggleButton)event.getSource()).isSelected();

        spectrumLeft.setNormalizeToAbsMax(!isButtonSelected);
        spectrumRight.setNormalizeToAbsMax(!isButtonSelected);
    }
    

    @FXML
    void toggleWsolaFilterButton(MouseEvent event) {
        ToggleButton button = (ToggleButton) event.getSource();
        
        if (!button.isSelected()) {
            button = wsola1o1FilterButton;
            wsola1o1FilterButton.setSelected(true);
        }
        try {
	        if (button == wsola1o1FilterButton) {
	            player.setTimeStretch(1.0);
	            snippetPlayer.setTimeStretch(1.0);
	        } else if (button == wsola1o8FilterButton) {
	            player.setTimeStretch(0.125);
	            snippetPlayer.setTimeStretch(0.125);
	        } else if (button == wsola1o4FilterButton) {
	            player.setTimeStretch(0.25);
	            snippetPlayer.setTimeStretch(0.25);
	        } else if (button == wsola1o3FilterButton) {
	            player.setTimeStretch(1.0/3.0);
	            snippetPlayer.setTimeStretch(1.0/3.0);
	        } else if (button == wsola1o2FilterButton) {
	            player.setTimeStretch(0.5);
	            snippetPlayer.setTimeStretch(0.5);
	        } else if (button == wsola2o3FilterButton) {
	            player.setTimeStretch(2.0/3.0);
	            snippetPlayer.setTimeStretch(2.0/3.0);
	        } else if (button == wsola3o4FilterButton) {
	            player.setTimeStretch(0.75);
	            snippetPlayer.setTimeStretch(0.75);
	        } else if (button == wsola7o8FilterButton) {
	            player.setTimeStretch(0.875);
	            snippetPlayer.setTimeStretch(0.875);
	        } else if (button == wsola5o4FilterButton) {
	            player.setTimeStretch(1.25);
	            snippetPlayer.setTimeStretch(1.25);
	        } else if (button == wsola3o2FilterButton) {
	            player.setTimeStretch(1.5);
	            snippetPlayer.setTimeStretch(1.5);
	        }
	        switchFrameRate();
        } catch (LineUnavailableException lue) {
        	log.error("exception during setting time stretch of snippet player: {}", lue.toString());
			showGenericAlert(lue);
        }
    }
    
    @FXML
    void toggleColorRange(MouseEvent event) {
        ToggleButton button = (ToggleButton) event.getSource();
        
        if (button == heatRangeButton) {
            spectrogramViewer.setColorRange(heatColorRange);
        } else if (button == grayRangeButton) {
            spectrogramViewer.setColorRange(grayColorRange);
        }
    }

    
    @FXML
    void toggleLoopStarting(MouseEvent event) {
        if (loopStartingButton.isSelected()
                && loopTerminalButton.isSelected() 
                && statusPlotterProcess.loopTerminalProperty().get() == 0.0) {
            loopTerminalButton.setSelected(false);
        }
        
        if (loopStartingButton.isSelected()) {
            spectrogramViewer.timeSelectOnProperty().set(true);
        } else {
            statusPlotterProcess.loopStartingProperty().set(0.0);
            spectrogramViewer.timeSelectOnProperty().set(false);
            loopStartingButton.getStyleClass().remove("loop-icon-active");
            loopStartingButton.getTooltip().setText(RuntimeSettings.getInstance().getLabelBundle().getString("command_place_loop_starting_point"));
        }
        
    }
    
    @FXML
    void toggleLoopTerminal(MouseEvent event) {
        if (loopTerminalButton.isSelected()
                && loopStartingButton.isSelected() 
                && statusPlotterProcess.loopStartingProperty().get() == 0.0) {
            loopStartingButton.setSelected(false);
        }
        
        if (loopTerminalButton.isSelected()) {
            spectrogramViewer.timeSelectOnProperty().set(true);
        } else {
            statusPlotterProcess.loopTerminalProperty().set(0.0);
            spectrogramViewer.timeSelectOnProperty().set(false);
            loopTerminalButton.getStyleClass().remove("loop-icon-active");
            loopTerminalButton.getTooltip().setText(RuntimeSettings.getInstance().getLabelBundle().getString("command_place_loop_terminal_point") );
        }
    }
    
    private Stage getStage() {     
        return (Stage) root.getScene().getWindow();
    }
    
    private void seek(double millisecond) {
    	 try {
         	isJustSeeked = true;
         	metronome.pause();        	
         	
         	this.seekEndListener = (o, oldMilli, newMilli) -> {
         		if (newMilli.doubleValue() != lastSeekPosition) {
         			statusPlotterProcess.getPositionProperty().removeListener(this.seekEndListener);
         			isJustSeeked = false;
         			metronome.resume();         			
         		}
             };
         	statusPlotterProcess.getPositionProperty().addListener(this.seekEndListener);
         	lastSeekPosition = statusPlotterProcess.getPositionProperty().get();
         	player.seek(millisecond);
         } catch (LineUnavailableException | UnsupportedAudioFileException | IOException exception) {
         	log.error("exception on seeking: {}" , exception.toString());
             showGenericAlert(exception);
         }
    }
    
    private void playTrack() {
        log.debug("playTrack");
        
        try {
            playButton.setGraphic(pauseIcon);
            player.play();
            
        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException exception) {
        	log.error("exception on playing track: {}" , exception.toString());
            showGenericAlert(exception);
        } 
    }
    
    private void stopTrack() {
        log.debug("stopTrack");
       
        playButton.setGraphic(playIcon);
        playButton.setSelected(false);
        player.stop();
        statusPlotterProcess.stop();
    }  
    
    private void pauseTrack() {
        log.debug("pauseTrack");
        player.pause();
    }
    
    private void saveSession() {
    	// TODO modal progress window task, service
    	log.debug("save session to {}", session.getSessionFile());
    	if (session.getSessionFile() == null) {
    		File file; 
            
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(RuntimeSettings.getInstance().getLabelBundle().getString("dialog_save_session_file_title"));
            fileChooser.getExtensionFilters()
                    .addAll(new ExtensionFilter("Session Files", "*.asg"));

            file = fileChooser.showSaveDialog(getStage());
            log.debug("File selected: " + (file == null ? "empty" : file.getAbsolutePath()));
            session.setSessionFile(file);
    	}
    	if (session.getSessionFile() != null) {
	    	try {
				sessionWriter.write(session, session.getSessionFile());
		    	session.setChanged(false);
		    	menuItemSaveSession.setDisable(true);
			} catch (IOException ex) {
	        	Alert alert = new Alert(AlertType.ERROR);
	        	alert.initOwner(primaryStage);
				alert.initStyle(StageStyle.UTILITY);
				alert.setTitle(RuntimeSettings.getInstance().getLabelBundle().getString("error"));
				alert.setHeaderText(RuntimeSettings.getInstance().getLabelBundle().getString("error_file_ioexception"));
				alert.setContentText(ex.getMessage());
				alert.show();
				log.error("exception on saving session: {}", ex.toString());
			}
    	}

    }
    
    private void openSession(File file) {  	    	
    	// TODO modal progress window task, service
    	Session session;
    	try {
			session = sessionLoader.load(file);
			menuItemSaveSession.setDisable(true);
			deploySession(session);
		} catch (UnsupportedEncodingException exception) {
			log.error("exception during opening session file {}: {}", file, exception.toString());
			Alert alert = new Alert(AlertType.ERROR);
			alert.initOwner(primaryStage);
			alert.initStyle(StageStyle.UTILITY);
			alert.setTitle(RuntimeSettings.getInstance().getLabelBundle().getString("error"));
			alert.setHeaderText(RuntimeSettings.getInstance().getLabelBundle().getString("error_session_encoding"));
			alert.setContentText(exception.getMessage());
			alert.show();
		} catch (FileNotFoundException fnfe) {
			log.error("exception during opening session file {}: {}", file, fnfe.toString());
			Alert alert = new Alert(AlertType.ERROR);
			alert.initOwner(primaryStage);
			alert.initStyle(StageStyle.UTILITY);
			alert.setTitle(RuntimeSettings.getInstance().getLabelBundle().getString("error"));
			alert.setHeaderText(RuntimeSettings.getInstance().getLabelBundle().getString("error_file_not_found"));
			alert.setContentText(file.getAbsolutePath());
			alert.show();
		}
    }
    
    private void openSession(InputStream inputStream) {  	    	
    	// TODO modal progress window task, service
    	Session session;
    	try {
    		menuItemSaveSession.setDisable(true);
			session = sessionLoader.load(inputStream);
			deploySession(session);
		} catch (UnsupportedEncodingException exception) {
			exception.printStackTrace();
			showGenericAlert(exception);
		}
    }
    
    private void deploySession(Session session) {
    	this.session = session;

    	tabTimeLine.setSession(session);

    	
    	barLineChangeHandler.setCurrentBarLine(session.getBarLines().getDefaultBarLine());
    	spinnerMeasure.getValueFactory().setValue(session.getBarLines().getDefaultBarLine().getMeasureMilliDuration());
    	spinnerOffset.getValueFactory().setValue(session.getBarLines().getDefaultBarLine().getMeasureMilliOffset());
        spinnerBeats.getValueFactory().setValue(session.getBarLines().getDefaultBarLine().getBeatsPerMeasure());

    	if (this.session.getAudioFile() != null) {
    		if (this.session.getAudioFile().exists()) {
    			openFile(this.session.getAudioFile()); 
    		} else {
    			Alert alert = new Alert(AlertType.INFORMATION);
    			alert.initOwner(primaryStage);
    			alert.initStyle(StageStyle.UTILITY);
    			alert.setTitle(RuntimeSettings.getInstance().getLabelBundle().getString("alert_audio_file_not_found_choose_title"));
    			alert.setHeaderText(this.session.getAudioFileAsString());
    			alert.setContentText(RuntimeSettings.getInstance().getLabelBundle().getString("alert_audio_file_not_found_choose_hint"));
    			alert.setOnHidden(e -> {
    				// TODO DRY make a common method for alert and menu
    				File file;
    				FileChooser fileChooser = new FileChooser();
    		        fileChooser.setTitle(RuntimeSettings.getInstance().getLabelBundle().getString("dialog_open_audio_file_title")); 
    		        fileChooser.getExtensionFilters()
    		                .addAll(new ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac", "*.m4a", "*.ogg", "*.aif", "*.aiff", "*.mp4", "*.flac"));
    		
    		        file = fileChooser.showOpenDialog(getStage());
    		        if (file != null) {
	    				openFile(file);	    				
	    	        	if (session.getSessionFile() != null) {
		    	        	menuItemSaveSession.setDisable(false);
		    	        	session.setChanged(true);
	    	        	}
    		        } else {
    		        	menuBar.setDisable(false);
    		        }
    			});
    			alert.show();
  
    		}
    		
    	}
    	this.contextMenuTones.setSession(session);
    }
    
    
    private void openFile(File file) {
        log.info("Opening file: {}", file.getAbsolutePath());

        boolean hasErrorOccured = false;

    	      
        
        if (audioSignalPlayer != null) {
        	audioSignalPlayer.close();
        }


        try {
            stopTrack();
            centerProgressStage();
            modalProgressStage.show();
            tmpGuitar.damp();
            tmpBass.damp();
            spectroGuitar.damp();
            isPreloading = true;

                      
            openFileService.setFile(file);
            
            openFileService.setOnSucceeded(workerEvent -> {
            	try {
    				audioSignalPlayer = openFileService.getAudioSignalPlayer();
    				player.setSignal(audioSignalPlayer);
                    waveScroll.setFloatAudioSignal(audioSignalPlayer);
                    beatBar.getBeatWave().setSignal(audioSignalPlayer);
                    beatParser.setSignal(audioSignalPlayer);
            		snippetPlayer.open(audioSignalPlayer);
                    spectrogramViewer.setSpectrogram(spectrogram);
                    spectrumLeft.setSpectrogram(spectrogram);
                    spectrumRight.setSpectrogram(spectrogram);

                    beatParser.setSession(session);
                    beatBar.getBeatWave().draw(true);
                    tabTimeLine.getChordBar().draw(true);
                    metronome.reset();

                    spectrogramViewer.timeSelectOnProperty().set(false);
                    loopStartingButton.setSelected(false);
                    loopTerminalButton.setSelected(false);
                    statusPlotterProcess.loopStartingProperty().set(0.0);
                    loopStartingButton.getStyleClass().remove("loop-icon-active");
                    loopStartingButton.getTooltip().setText(RuntimeSettings.getInstance().getLabelBundle().getString("command_place_loop_starting_point"));
                    statusPlotterProcess.loopTerminalProperty().set(0.0);
                    loopTerminalButton.getStyleClass().remove("loop-icon-active");
                    loopTerminalButton.getTooltip().setText(RuntimeSettings.getInstance().getLabelBundle().getString("command_place_loop_terminal_point"));
                    

                    
                    labelDuration.setText("/ " + TimeUtils.getFormattedString(PCMUtils.getMilliseconds(audioSignalPlayer)));
                    root.setDisable(false);
                    this.session.setAudioFile(file);        
                    menuItemSaveSessionAs.setDisable(false);
                    getStage().setTitle(RuntimeSettings.getInstance().getLabelBundle().getString("application_title") 
                            + " - " + file.getAbsolutePath()); //TODO Binding session file if not null
                    fadeInTransition.play();
                    modalProgressStage.close();
                    isPreloading = false;
                    splitSpec.setDividerPositions(0.75f);
    			} catch (InterruptedException ie) {
    				Thread.currentThread().interrupt();
    			} catch (ExecutionException ee) {
    				log.error("exception during opening file in JavaFX task: {}", ee.toString());
    				showGenericAlert(ee);
    			} catch (LineUnavailableException e) {
    				log.error("exception during opening file in JavaFX task, probably while opening snippet player: {}", e.toString());
    				showGenericAlert(e);
				} 

            });
            openFileService.restart();         
            
            
        } finally {
        	if (hasErrorOccured) {
        		root.setDisable(false);
                modalProgressStage.close();
                isPreloading = false;
                if (session.getAudioFile() != null) {
                	fadeInTransition.play();
                } else {
                	menuBar.setDisable(false);
                }
        	}
        }
        
        System.gc();
    }

    public void close() {
        stopTrack();
        player.terminateFilterChain();
        spectroGuitar.close(true);
        metronome.close(true);
        snippetPlayer.close(true);
        tmpGuitar.close(true);
        tmpBass.close(true);
        spectrogram.close();
    }
    
    public File getFile() {
        return session.getAudioFile();
    }

    public void setFile(File file) {
        if (file != null) {
            openFile(file);
        }
    }
    
    public void setPrimaryStage(Stage primaryStage) {
    	this.primaryStage = primaryStage;
    	primaryStage.setOnCloseRequest(e -> {
    		if (!isCurrentSessionQuitConfirmed()) {
    			e.consume();
    		}
    	});
    	modalProgressStage.initOwner(primaryStage);
    	centerProgressStage();
    	
    	Circle circle;
    	FillTransition ft;
    	Color bgColor = Color.BLACK;
    	Color stColor = Color.web("#909890");

        try {
 			String bgColorString = StyleParser.getStyleRuleValueAsString(threadBox.getScene(), ".root", "-fx-base");
 			String stColorString = StyleParser.getStyleRuleValueAsString(threadBox.getScene(), ".root", "-fx-background");
 			bgColor = Color.web(bgColorString);
 			stColor = Color.web(stColorString); 
 		} catch (MalformedURLException e1) {
 			e1.printStackTrace();
 		}      

        //DropShadow dropShadow = new DropShadow(BlurType.GAUSSIAN, Color.BLACK, 5, 0, 1, 1);
        
        for (int i = 0; i < Environment.getFFTThreadCount(); i++) {
         	circle = new Circle();
         	circle.setRadius(4);
         	circle.setFill(bgColor);
         	//circle.setEffect(dropShadow); 
         	circle.setStroke(stColor);
         	circle.setStrokeWidth(1.0);
         	threadCircle.add(circle);
         	threadBox.getChildren().add(circle);
         	
         	ft = new FillTransition(Duration.millis(500), circle);
         	ft.setFromValue(Color.web("#0094c5"));
         	ft.setToValue(bgColor);
         	passiveTrans.add(ft);
        }
    }
    

    
    private void centerProgressStage() {
    	modalProgressStage.setX(primaryStage.getX() + primaryStage.getWidth() / 2d - modalProgressStage.getWidth() / 2d);
        modalProgressStage.setY(primaryStage.getY() + primaryStage.getHeight() / 2d - modalProgressStage.getHeight() / 2d);
    }
    
    private void showGenericAlert(Exception exception) {
    	Alert alert = new Alert(AlertType.ERROR);
    	alert.initOwner(primaryStage);
		alert.initStyle(StageStyle.UTILITY);
		alert.setTitle(RuntimeSettings.getInstance().getLabelBundle().getString("error"));
		alert.setHeaderText(exception.getClass().getName());
		alert.setContentText(exception.getLocalizedMessage());
		alert.show();
    }
    
}
