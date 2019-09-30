package kim.kohlhaas.sone.analyze;

import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kim.kohlhaas.sone.harmony.PianoToneSet;
import kim.kohlhaas.sone.harmony.Tone;
import kim.kohlhaas.sone.harmony.ToneSet;
import kim.kohlhaas.sone.signal.BufferedFloatAudioSignal;
import kim.kohlhaas.sone.signal.FloatAudioSignal;
import kim.kohlhaas.sone.util.ArrayUtils;
import kim.kohlhaas.sone.util.PCMUtils;
import kim.kohlhaas.sone.Environment;

public class PianoSpectrogram extends LinearTimeFloatSpectrogram {
    
    private volatile FloatAudioSignal floatAudioSignal;
    private int samplesPerTimeStep;
    private int timeSteps;
    final static Logger log = LoggerFactory.getLogger(PianoSpectrogram.class);
    private int toneResolution = 1;
    private volatile float[][][] grid;
    private volatile byte[][][] gridCount;
    private int lastChannels = 0;
    private volatile float max = 0.0f;
    private double milliseconds;
    private PianoToneSet pianoToneSet;
    private static final int OCTAVE_RANGE = 8;
    
    private static final int OCTAVE_RANGE_PRE = 1;
    private static final int CHANNEL_PRE = 1;
    private static final double MEAN_DECREASE_INHIBITOR = 15.0; //TODO changeable via settings
    
    private Thread scanDaemon = null; 
    private int progressStep = 0;
    private double rootMeanSquare = 0; 
    private Thread[] fftThread;
    private int progressStepCount;
    private int fftThreadCount = Environment.getFFTThreadCount();
    
    public PianoSpectrogram(int toneResolution, PianoToneSet pianoToneSet) {
        this.toneResolution = toneResolution;
        this.pianoToneSet = pianoToneSet;
        samplesPerTimeStep = FFTAnalyzer.Resolution.FRQ_256.getValue(); //TODO changeable via settings
    }
    
    // TODO auf long index werte umstellen
    private void scanHarmonics(FloatAudioSignal signal, double firstHarmonicFrequency, int channel, int startIndex, int endIndexExclusive) throws IOException {
        double frequency;
        int timeStep;
        int timeStepIterator;
        double toneGradient;
        double tonePrecision;
        int toneIndex;
        int gridIndex;
        int timeStepRange;
        double meanPresent;
        double meanAdd;
        int addProportionIncrease = OCTAVE_RANGE; // TODO changeable by settings

        FFTAnalyzer analyzer = FFTAnalyzer.getInstance();
        
        float[][] calcChannelSamples = new float[lastChannels][FFTAnalyzer.Resolution.FRQ_2048.getValue()];
        
        FFTAnalyzer.Resolution fftResolution = FFTAnalyzer.getMinimumResolution(firstHarmonicFrequency, signal);
                
        timeStepRange = FFTAnalyzer.getSampleCount(fftResolution) / samplesPerTimeStep;
        
        
        timeStepIterator = FFTAnalyzer.getSampleCount(fftResolution) / 2;

        
        for (int f = startIndex;
        		f < endIndexExclusive 
        		&& !Thread.currentThread().isInterrupted() 
        		&& floatAudioSignal.getFile().getName().equals(signal.getFile().getName());
        		f += timeStepIterator) {
        	
            timeStep = (f - (f % samplesPerTimeStep)) / samplesPerTimeStep;
            if (!Thread.currentThread().isInterrupted()) {
	            calcChannelSamples[channel] = analyzer.getSpectrum(fftResolution, signal, channel, f , true, firstHarmonicFrequency);
	            for (int s = 0; s < calcChannelSamples[channel].length && !Thread.currentThread().isInterrupted() && floatAudioSignal.getFile().getName().equals(signal.getFile().getName()); s++) {
	                if (s == 0) {
	                    frequency = FFTAnalyzer.getFrequencyOfIndex(fftResolution, signal, 1, firstHarmonicFrequency);
	                } else {
	                    frequency = FFTAnalyzer.getFrequencyOfIndex(fftResolution, signal, s, firstHarmonicFrequency);
	                }
	                toneGradient = pianoToneSet.getGradientIndex(frequency);
	                toneIndex = pianoToneSet.getNearestIndex(frequency);
	                if (toneResolution == 1) {
	                	tonePrecision = pianoToneSet.getTonePrecision(frequency);
	                } else {
	                	tonePrecision = 1.0;
	                }
	                gridIndex = toneIndex * toneResolution  + (int) Math.floor(((toneGradient - toneIndex) + 0.5) * toneResolution);
	                
	                if (gridIndex >= 0) {
	                    if (gridIndex < pianoToneSet.getToneCount() * toneResolution) {
	                        for (int g = 0; g < timeStepRange && !Thread.currentThread().isInterrupted() && floatAudioSignal.getFile().getName().equals(signal.getFile().getName()); g++) {
	                        	
	                        	if (OCTAVE_RANGE > 1 && gridCount[channel][timeStep + g][gridIndex] / addProportionIncrease > 0) {
	                        		meanPresent =  grid[channel][timeStep + g][gridIndex] * gridCount[channel][timeStep + g][gridIndex] / (gridCount[channel][timeStep + g][gridIndex] + 1);
	                        		meanAdd = (calcChannelSamples[channel][s] * tonePrecision) / ((gridCount[channel][timeStep + g][gridIndex] + 1) / addProportionIncrease) ;
	                        	} else {
	                        		meanPresent =  grid[channel][timeStep + g][gridIndex];
	                        		meanAdd = calcChannelSamples[channel][s] * tonePrecision;
	                        	}
	                            grid[channel][timeStep + g][gridIndex] = (float) (meanPresent + meanAdd);
	                            gridCount[channel][timeStep + g][gridIndex]++;
	                            
	                            if (grid[channel][timeStep + g][gridIndex] > max) {
	                            	max = grid[channel][timeStep + g][gridIndex];
	                            }                                                    	
	                        }
	                    } else {
	                        break;
	                    }
	                }
	            }
            }
                        
        }
    }

    
    private void scan() throws IOException {        
        int timeStepRange =  FFTAnalyzer.getSampleCount(FFTAnalyzer.getMinimumResolution(pianoToneSet.getTone(0).getFrequency(), floatAudioSignal)) / samplesPerTimeStep;
        int progressStepCountPre;
        

        if (scanDaemon != null) {
        	log.info("interrupting running analysis thread");
        	scanDaemon.interrupt();
        	try {
        		log.info("joining interrupted analysis thread");
				scanDaemon.join();
				FFTAnalyzer.clear();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
        }   
        
        progressStep = 0;
                     
        
        progressStepCountPre = fftThreadCount * pianoToneSet.getTemperament().getOctaveToneCount() * OCTAVE_RANGE_PRE + 1 + fftThreadCount + 1;
        progressStepCount =  fftThreadCount * (floatAudioSignal.getChannels() * pianoToneSet.getTemperament().getOctaveToneCount() * (OCTAVE_RANGE)) + 2 + 3 * fftThreadCount + 1;
        fftThread = new Thread[fftThreadCount];
        
        
        max = 0.0f;
        
        progress.update((double) progressStep / progressStepCountPre, "cmd:init");
        
        timeSteps = (int) Math.ceil((double) floatAudioSignal.getFrameLength() / samplesPerTimeStep);
        
     
        
        grid = new float[floatAudioSignal.getChannels()][timeSteps + timeStepRange - 1][pianoToneSet.getToneCount() * toneResolution];
        gridCount = new byte[floatAudioSignal.getChannels()][timeSteps + timeStepRange - 1][pianoToneSet.getToneCount() * toneResolution]; //experimental
        
        for (int c = 0; c < floatAudioSignal.getChannels(); c++) {
          for (int t = 0; t < timeSteps; t++) {
              for (int k = 0; k < pianoToneSet.getToneCount() * toneResolution; k++) {
                  grid[c][t][k] = 0.0f;
                  gridCount[c][t][k] = 0;
              }
          }
          
          progress.update((double) ++progressStep / progressStepCountPre, "cmd:delete"); 
        }
        
        
        
        startFFTCluster(0, CHANNEL_PRE, 0, OCTAVE_RANGE_PRE, progressStepCountPre);

        progress.update((double) ++progressStep / progressStepCountPre, "cmd:initDaemon");
        
        try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
        
        scanDaemon = new Thread(() -> {
            rootMeanSquare = 0;
            String initFileName = floatAudioSignal.getFile().getName();
        
    		log.info("fft pre continue");

    		startFFTCluster(CHANNEL_PRE, floatAudioSignal.getChannels(), 0, OCTAVE_RANGE_PRE, progressStepCount);

    		log.info("fft parallel");
    		
    		startFFTCluster(0, floatAudioSignal.getChannels(), OCTAVE_RANGE_PRE, OCTAVE_RANGE, progressStepCount);
           

    		    		
            for (int c = 0; c < grid.length  && !Thread.currentThread().isInterrupted() && floatAudioSignal.getFile().getName().equals(initFileName); c++) {
            	
                for (int t = 0; t < gridCount[c].length && !Thread.currentThread().isInterrupted() && floatAudioSignal.getFile().getName().equals(initFileName); t++) {
                    for (int k = 0; k < grid[c][t].length && !Thread.currentThread().isInterrupted() && floatAudioSignal.getFile().getName().equals(initFileName); k++) {                            
                        if (k % toneResolution == 0) { // first tone division
                            rootMeanSquare = 0;
                        }  
                        rootMeanSquare += Math.pow(grid[c][t][k], 2);                        
                        if (k % toneResolution == toneResolution - 1) { // last tone division
                            rootMeanSquare = Math.sqrt(rootMeanSquare / toneResolution);
                            rootMeanSquare = rootMeanSquare * Math.pow(2.0, -k / (12.0 * MEAN_DECREASE_INHIBITOR));
                            for (int r = 0; r < toneResolution; r++) {
                                if (grid[c][t][k - r] < rootMeanSquare) {     
                                	grid[c][t][k - r] = (float) rootMeanSquare;
                                }
                            }
                        }
                    }
                }                
            }

            progress.update((double) ++progressStep / progressStepCount, "cmd:rootMeanSquare");
    	log.info("thread finished: {}", Thread.currentThread().getName());
        }, "scanDaemon");
        scanDaemon.setDaemon(true);
        scanDaemon.setPriority(Thread.MIN_PRIORITY);
        scanDaemon.start();
        floatAudioSignal.close();
    }
    
    private void startFFTCluster(int channelFrom, int channelTo, int octaveFrom, int octaveTo, int progressSteps) {
        for (int t = 0; t < fftThreadCount && !Thread.currentThread().isInterrupted(); t++) {
        	progress.update((double) ++progressStep / progressSteps, "cmd:startthread(" + t + ")"+progressStep);
        	startFFTDaemon(t, channelFrom, channelTo, (int) Math.ceil((double) t * floatAudioSignal.getFrameLength() / (double) fftThreadCount), (int) Math.floor((t + 1.0) * floatAudioSignal.getFrameLength() / (double) fftThreadCount), octaveFrom, octaveTo, progressSteps);
        }
		try {
			for (int t = 0; t < fftThreadCount && !Thread.currentThread().isInterrupted(); t++) {
				fftThread[t].join();				
			}
		} catch (InterruptedException e) {
			log.info("fft parallel interrupted");
			for (int t = 0; t < fftThreadCount; t++) {
				if (fftThread[t] != null) {
					fftThread[t].interrupt();
				}
			}
			Thread.currentThread().interrupt();
		}
    }
    
    private void startFFTDaemon(int threadNumber, int channelFrom, int channelTo, int startIndex, int endIndex, int octaveFrom, int octaveTo, int progressSteps) {
    	BufferedFloatAudioSignal signal;
    	try {
			signal = new BufferedFloatAudioSignal(floatAudioSignal.getFile(), FFTAnalyzer.Resolution.FRQ_8192.getValue());
			fftThread[threadNumber] = new Thread(() -> {
				
		    	for (int c = channelFrom; c < channelTo && !Thread.currentThread().isInterrupted() && floatAudioSignal.getFile().getName().equals(signal.getFile().getName()); c++) {
		    		for (int o = pianoToneSet.getTemperament().getOctaveToneCount() * octaveFrom; o < pianoToneSet.getTemperament().getOctaveToneCount() * octaveTo && !Thread.currentThread().isInterrupted() && floatAudioSignal.getFile().getName().equals(signal.getFile().getName()); o++) {
		    			try {
							scanHarmonics(signal, pianoToneSet.getTone(o).getFrequency(), c, startIndex, endIndex);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		                log.debug("fft harmonics of {} in channel {}, startIndex {}, end Index{}, progress {}",  pianoToneSet.getTone(o).getFrequency(), c, startIndex, endIndex, progressStep);
		                progress.update((double) ++progressStep / progressSteps, "cmd:range("+threadNumber+")"+progressStep+"=" + startIndex + "-" + endIndex);
		            }
				}
		    	signal.close();
				log.info("fft finished {}", threadNumber);
	    	}, "fftThread-"+threadNumber);
	    	fftThread[threadNumber].setDaemon(true);
	    	fftThread[threadNumber].setPriority(Thread.MIN_PRIORITY);
	    	fftThread[threadNumber].start();
	    	log.info("fft started-" + threadNumber);
		} catch (IOException | UnsupportedAudioFileException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    }
    
    @Override
    public int getChannels() {
        if (floatAudioSignal != null) {
            return floatAudioSignal.getChannels();
        } else {
            return 0;
        }
    }

    @Override
    public int getFreqs() {
        return pianoToneSet.getToneCount() * toneResolution;
    }

    @Override
    public int getTimeSteps() {
        return timeSteps;
    }

    @Override
    public int getSamplesPerTimeStep() {
        return samplesPerTimeStep;
    }
    
    @Override
    public double getMilliseconds() {
        return milliseconds;
    }

    @Override
    public float getMaxAmp() {
        return max;
    }
    
    @Override
    public float getMaxAmp(int channel) {
        return ArrayUtils.getMax(grid[channel]);
    }

    @Override
    public float getMaxAmpInFreq(int channel, int freqIndex) {
        return ArrayUtils.getMax(grid[channel], freqIndex);
    }

    @Override
    public float getMaxAmpInTime(int channel, int timeIndex) {
        return ArrayUtils.getMax(grid[channel][timeIndex]);
    }
    
    @Override
    public float getAmp(int timeIndex, int freqIndex) {
        float result = 0.0f;
        for (int c = 0; c < floatAudioSignal.getChannels(); c++) {
            result += grid[c][timeIndex][freqIndex];
        }
        return result / floatAudioSignal.getChannels();
    }
    
    @Override
    public float getAmp(int channel, int timeIndex, int freqIndex) {
        return grid[channel][timeIndex][freqIndex];
    }

    @Override
    public void copySpectrum(int channel, int timeIndex, float[] dest) {
        System.arraycopy(grid[channel][timeIndex], 0, dest, 0, getFreqs());
    }

    @Override
    public void copySpectrum(int channel, int from, int to, float[][] dest) {
        for (int t = from; t < grid[channel].length && t <= to; t++) {
            System.arraycopy(grid[channel][t], 0, dest, 0, getFreqs());
        }
    }

    @Override
    public double getFreq(int freqIndex) {
        int toneIndex = getToneIndex(freqIndex);
        return pianoToneSet.getTone(toneIndex).getFrequency();
    }
    
    public Tone getTone(int freqIndex) {
        int toneIndex = getToneIndex(freqIndex);
        return pianoToneSet.getTone(toneIndex);
    }

    public int getToneIndex(int freqIndex) {
        return Math.floorDiv(freqIndex, toneResolution);
    }
    
    public ToneSet getToneSet() {
        return this.pianoToneSet;
    }
    
    @Override
    public FloatAudioSignal getFloatAudioSignal() {
        return floatAudioSignal;
    }

    @Override
    public void setFloatAudioSignal(FloatAudioSignal floatAudioSignal) throws IOException {
        this.floatAudioSignal = floatAudioSignal;
        
        milliseconds = PCMUtils.getMilliseconds(floatAudioSignal);
        
        if (lastChannels < floatAudioSignal.getChannels()) {
            lastChannels = floatAudioSignal.getChannels(); 
        }
        
        scan();
    }

	@Override
	public void close() {
		if (scanDaemon != null) {
			scanDaemon.interrupt();
		}
		for (int t = 0; t < fftThreadCount; t++) {
			if (fftThread != null && fftThread[t] != null) {
				fftThread[t].interrupt();
			}
		}
		
	}


}
