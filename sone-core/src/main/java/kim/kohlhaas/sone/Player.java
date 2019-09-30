package kim.kohlhaas.sone;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kim.kohlhaas.sone.PlayingProcess;
import kim.kohlhaas.sone.event.EventHandler;
import kim.kohlhaas.sone.event.NullEventHandler;
import kim.kohlhaas.sone.event.PlayerEvent;
import kim.kohlhaas.sone.filter.FilterChain;
import kim.kohlhaas.sone.filter.ResampleFilter;
import kim.kohlhaas.sone.filter.WSOLAFilter;
import kim.kohlhaas.sone.signal.FloatAudioSignal;

public class Player {
    
    final static Logger log = LoggerFactory.getLogger(Player.class);
    

    private FloatAudioSignal signal;
    private PlayingProcess currentPlayingProcess = null;
    private EventHandler<PlayerEvent> onTrackFinished = new NullEventHandler<PlayerEvent>();
    private EventHandler<PlayerEvent> onTrackPause = new NullEventHandler<PlayerEvent>();
    private EventHandler<PlayerEvent> onTrackResume = new NullEventHandler<PlayerEvent>();
    private EventHandler<PlayerEvent> onTrackPlay = new NullEventHandler<PlayerEvent>();
    private EventHandler<PlayerEvent> onTrackSeek = new NullEventHandler<PlayerEvent>();
    private boolean isSeeking = false;
    private ResampleFilter resampleFilter;
    private WSOLAFilter wsolaFilter;
    private FilterChain filterChain;
    private double pitchFactor = 1.0;
    private double stretchFactor = 1.0;
    private Double haltedSeekPosition = null; // force exact borders for assigning selected beats, measures or barlines 
    private static final int CHUNK_SIZE_IN_FRAMES = 1750;
    
    public Player() {
        resampleFilter = new ResampleFilter("Player - Resample Filter");
        filterChain = new FilterChain("Player", CHUNK_SIZE_IN_FRAMES);
    }
        
    public FloatAudioSignal getSignal() {
        return signal;
    }

    public void setSignal(FloatAudioSignal signal) {
        double wsolaTempo = 1.0;
        this.signal = signal;
        stop();
        
        if (wsolaFilter != null) {
           wsolaTempo = wsolaFilter.getTempo();           
        }
        
        if (wsolaFilter == null) {
            wsolaFilter = new WSOLAFilter("Player - WSOLA Filter", signal.getFormat(), 20, 100,35, wsolaTempo);
            if (wsolaTempo != 1.0) {
                filterChain.addFirst(wsolaFilter);
            }
        } else {
           wsolaFilter.setFormat(signal.getFormat()); 
        }
    }

    public EventHandler<PlayerEvent> getOnTrackFinished() {
        return onTrackFinished;
    }

    public void setOnTrackFinished(EventHandler<PlayerEvent> onTrackFinished) {
        this.onTrackFinished = onTrackFinished;
    }
    
    public EventHandler<PlayerEvent> getOnTrackPause() {
        return onTrackPause;
    }

    public void setOnTrackPause(EventHandler<PlayerEvent> onTrackPause) {
        this.onTrackPause = onTrackPause;
    }

    public void setOnTrackSeek(EventHandler<PlayerEvent> onTrackSeek) {
        this.onTrackSeek = onTrackSeek;
    }

    public EventHandler<PlayerEvent> getOnTrackPlay() {
        return onTrackPlay;
    }

    public void setOnTrackPlay(EventHandler<PlayerEvent> onTrackPlay) {
        this.onTrackPlay = onTrackPlay;
    }

    public EventHandler<PlayerEvent> getOnTrackResume() {
        return onTrackResume;
    }

    public void setOnTrackResume(EventHandler<PlayerEvent> onTrackResume) {
        this.onTrackResume = onTrackResume;
    }

    public void play() throws LineUnavailableException, UnsupportedAudioFileException, IOException {
        log.debug("PLAY");
        if (currentPlayingProcess != null && currentPlayingProcess.isPaused()) {
            resume();
        } else {
        	haltedSeekPosition = null;
            play(0, false);
        }
    }
    
    private void setResampleFilterEnabled(boolean isEnabled) {
        
        //TODO Mit Comparable Positionen von WSOLA- und Resample vergleichen, damit später weitere Filter hinzugefügt werden können
        if (isEnabled) { 
            //TODO Diese Abfrage muss momentan nicht in der setWsola... gemacht werde, da in der arrangePitchStretch der Wsola vor resample gesetzt wird 
            if (this.stretchFactor <= 1.0) {
                if (filterChain.isFirst(resampleFilter)) {
                    filterChain.remove(resampleFilter);
                }
                filterChain.addLast(resampleFilter);
            } else {
                if (filterChain.isLast(resampleFilter)) {
                    filterChain.remove(resampleFilter);
                }
                filterChain.addFirst(resampleFilter);
            }
        } else {
            filterChain.remove(resampleFilter);
        }
    }
    
    public void setPitch(double factor) {
        log.debug("pitch factor set to {}", factor);
        this.pitchFactor = factor;
        arrangePitchStretch();
    }
    
    public double getPitch() {
    	return this.pitchFactor;
    }
    
    public void setTimeStretch(double factor) {
        log.debug("stretch factor set to {}", factor);
        this.stretchFactor = factor;
        arrangePitchStretch();
    }
    
    public double getTimeStretch() {
    	return this.stretchFactor;
    }
    
    private void arrangePitchStretch() {        
        setWsolaFilterTempo(this.stretchFactor * (1.0 / this.pitchFactor)); 
        resampleFilter.setFactor((1.0 / this.pitchFactor));
        if (Math.floor((1.0 / this.pitchFactor) * 100) == 100) {
            setResampleFilterEnabled(false);
        } else {
            setResampleFilterEnabled(true);
        }
        //filterChain.flush();
        log.debug("WSOLA-Position: {}, Resample-Position: {}", filterChain.getPosition(wsolaFilter), filterChain.getPosition(resampleFilter));
    }
    
    private void setWsolaFilterTempo(double tempo) {
        log.debug("SET_WSOLA_FILTER tempo: {}", tempo);
        if (tempo == 1.0) {
            filterChain.remove(wsolaFilter);
        } else {
            wsolaFilter.setTempo(tempo);
            if (!filterChain.contains(wsolaFilter)) {
                filterChain.addFirst(wsolaFilter);
            }
        }
    }   
    
    public void play(double millisecond, boolean isPaused) throws LineUnavailableException, UnsupportedAudioFileException, IOException {
        log.debug("PLAY - {}", millisecond);
        synchronized (this) {
        	if (currentPlayingProcess == null) {
		        filterChain.open(signal.getFormat());
		        if (!isPaused) {
		            filterChain.start();
		        }
		        currentPlayingProcess = new PlayingProcess(signal, filterChain, millisecond, isPaused, e -> {
		            log.debug("Event: {}, isSeeking: {}", e.toString(), isSeeking);
		            filterChain.close();
		            if (!isSeeking) {
		                
		                currentPlayingProcess = null;
		                onTrackFinished.handle(e);               
		                
		            } else {
		                isSeeking = false;
		            }
		                
		        });
		
		        if (!isSeeking && !isPaused()) {
		            onTrackPlay.handle(new PlayerEvent(this, PlayerEvent.PlayerState.PLAYING).setMillisecond(millisecond));
		        }
        	}
        } 
    }
    
    public void seek(double millisecond) throws LineUnavailableException, UnsupportedAudioFileException, IOException {
        log.debug("SEEK - {}", millisecond);
        isSeeking = true;
        
        onTrackSeek.handle(new PlayerEvent(this, PlayerEvent.PlayerState.SEEKING).setMillisecond(millisecond));
       
        if (isPlaying()) {
            stop();
            play(millisecond, false);
        } else if (isStopped() || isPaused()) {
            stop();
            haltedSeekPosition = millisecond;
            play(millisecond, true);
        } 
    }
    
    public void pause() {
        log.debug("PAUSE");
        synchronized (this) {
	        filterChain.stop();
	        currentPlayingProcess.pause();
	        if (!isSeeking) {
	            onTrackPause.handle(new PlayerEvent(this, PlayerEvent.PlayerState.PAUSED).setMillisecond(currentPlayingProcess.getMillisecondPosition()));
	        }
	        isSeeking = false;
        }
    }
    
    public void resume() {
        log.debug("RESUME");
        haltedSeekPosition = null;
        synchronized (this) {
        	if (currentPlayingProcess != null) {
		        filterChain.start();
		        currentPlayingProcess.resume();
		        onTrackResume.handle(new PlayerEvent(this, PlayerEvent.PlayerState.PLAYING).setMillisecond(currentPlayingProcess.getMillisecondPosition()));
        	}
        }
    }
    
    public void stop() {
        log.debug("STOP");
        synchronized (this) {
	        if (currentPlayingProcess != null) {
	            filterChain.stop();
	            filterChain.flush();
	            currentPlayingProcess.close();
	            currentPlayingProcess = null;
	        }
        }
    }
    
    public void terminateFilterChain() {
        log.debug("TERMINATE");
        filterChain.terminate();
    }
    
    public double getMillisecondPosition() {
        if (currentPlayingProcess != null) {
        	if (haltedSeekPosition == null) {
        		return currentPlayingProcess.getMillisecondPosition();
        	} else {
        		return haltedSeekPosition;
        	}
        } else {
            return 0.0;
        }
    }    
    
    public boolean isStopped() {
    	synchronized (this) {
    		return currentPlayingProcess == null;
    	}
    }
    
    public boolean isPaused() {
    	synchronized (this) {
    		return currentPlayingProcess != null && currentPlayingProcess.isPaused(); 
    	}
    }
    
    public boolean isPlaying() {
    	synchronized (this) {
    		return currentPlayingProcess != null && !currentPlayingProcess.isPaused();
    	}
    }
    
    public void setVolume(float volume) {
    	filterChain.setVolume(volume);
    	
    }
    
    public float getVolume() {
    	return filterChain.getVolume();
    }
        
}
