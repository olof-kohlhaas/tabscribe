package kim.kohlhaas.sone;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kim.kohlhaas.sone.event.EventHandler;
import kim.kohlhaas.sone.event.PlayerEvent;
import kim.kohlhaas.sone.filter.FilterChain;
import kim.kohlhaas.sone.signal.FloatAudioSignal;


public class PlayingProcess implements Runnable {  
    
    final static Logger log = LoggerFactory.getLogger(PlayingProcess.class);
    
    private Thread thread;
    private EventHandler<PlayerEvent> eventHandler;
    private static float FRAME_CHUNK_MILLISECS_PLAYING = 1.0f;
    private FloatAudioSignal signal;
    private long frameOffset;
    private int chunkFrames;
    
    private FilterChain filterChain;
    private boolean isPaused;
    private final Object pauseToken = new Object();
    private static final int MILLIS_TO_REINIT_AUDIO_SOURCE = 250;

    
    public PlayingProcess(FloatAudioSignal signal, FilterChain filterChain, double millisecond, boolean isPaused, EventHandler<PlayerEvent> eventHandler) throws LineUnavailableException, UnsupportedAudioFileException, IOException {
        this.eventHandler = eventHandler;

        this.signal = signal;
     
        this.filterChain = filterChain;
        frameOffset = (long)((millisecond / 1000) * signal.getFrameRate());
        chunkFrames = Math.round(signal.getFrameRate() * FRAME_CHUNK_MILLISECS_PLAYING / 1000);
        
        this.isPaused = isPaused;
        
        this.thread = new Thread(this);
        this.thread.setName("Playing Thread");
        this.thread.start();
    }
    
    
    
    @Override
    public void run() {      
        float[][] framesChunk= new float[signal.getChannels()][chunkFrames];

        for (long i = frameOffset; i < signal.getFrameLength(); i += chunkFrames) {
            if (isPaused && !Thread.currentThread().isInterrupted()) {
                
                synchronized (pauseToken) {
                    try {
                        pauseToken.wait();
                        log.debug("resume notification received");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    
                    if (Thread.currentThread().isInterrupted()) {
                        log.debug("INTERRUPT PAUSED STREAM CHUNK ");
                        break;
                    }
                }
            } 

            if (Thread.currentThread().isInterrupted()) {
                log.debug("INTERRUPT STREAM CHUNK ");
                break;
            }
            
            for (int c = 0; c < signal.getChannels(); c++) {
                signal.copyFrames(c, i, chunkFrames, framesChunk[c], 0);
            }
        
            filterChain.writeFrames(framesChunk);
            
        }
        
        if (!Thread.currentThread().isInterrupted()) {
            filterChain.drain();
        }
        
        log.debug("process finished");
        eventHandler.handle(new PlayerEvent(this, PlayerEvent.PlayerState.FINISHED));
        log.debug("process terminated");
    }
    
    public void close() {
        log.debug("close");
        int pastMillisSinceSourceInit = signal.getPastMillisSinceSourceInit();
        if (pastMillisSinceSourceInit < MILLIS_TO_REINIT_AUDIO_SOURCE ) {
        	log.info("Only {}ms have past since last decoding init. waiting {}ms ...", pastMillisSinceSourceInit, MILLIS_TO_REINIT_AUDIO_SOURCE - pastMillisSinceSourceInit);
	        try {
				Thread.sleep(MILLIS_TO_REINIT_AUDIO_SOURCE - pastMillisSinceSourceInit);
			} catch (InterruptedException e1) {
				Thread.currentThread().interrupt();
			}
        }
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.debug("closed");
    }
    
    public void pause() {
        synchronized (pauseToken) {
            isPaused = true;            
        }
    }
    
    public void resume() {
        synchronized (pauseToken) {
            isPaused = false;
            pauseToken.notifyAll();
        }
    }
    
    public boolean isPaused() {
        return isPaused;
    }
    
    public double getMillisecondPosition() {
        return (frameOffset * 1000.0) / signal.getFormat().getFrameRate() + filterChain.getMicrosecondPosition() / 1000;
    }




    

    

    




}
