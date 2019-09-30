package kim.kohlhaas.sone.synthesis;

import javax.sound.sampled.AudioFormat;

import kim.kohlhaas.sone.harmony.PianoToneSet;
import kim.kohlhaas.sone.harmony.ToneSet;

/*************************************************************************
 * Dependencies : RingBuffer
 * Description  : 
 *  this will create a simulated guitar string at the given frequency.
 *  Included are functions to pluck() the string and tic() the simulation
 *  (advance the simulation by one step).
 *               
 *****************************************************************************/

public class GuitarString 
{

        public enum Channel {
            MONO, LEFT, RIGHT
        }
    
        
        private static final double DECAY_FACTOR = 20.0;
        
        private GuitarSettings settings = GuitarSettings.getInstance();
    
        private double gainL;
        private double gainR;
        private double prePan;
        
        private double pluckDamping;
        private double charVariation;
        
        private double velocity;

        private double dr = 0.0;
        private double dc = 0.0;
        private double af = 0.0;
        private double df = 0.0;
        
        private int periodN;
        private double currentFreq;
        private double readOffset;
        
        private AudioFormat format;
        private ToneSet toneSet;
        private int toneSetIndex;
        private int order;
        
        private final NoiseGen stringNoiseGen = new NoiseGenRnd(4095);
        private final NoiseGen dampNoiseGen = new NoiseGenRnd(65535);
        
        private final Object pluckSync = new Object();
        
        private GuitarCharacter character;
        
        private boolean isDamping = false;

	private RingBuffer ringBuffer;

	private int maxPeriodN;
	
	private int time_;
	// Sampling rate to use for this simulation

	
	
	// create a guitar string of the given frequencey, using a sampling rate of 44,100
	public GuitarString(AudioFormat format, ToneSet toneSet, GuitarCharacter character, int toneSetIndex, int order) 
	{   
	    
	    this.format = format;
	    this.character = character;
	    this.prePan = (order - 2.5) * 0.4;
	    this.toneSet = toneSet;
	    this.toneSetIndex = toneSetIndex;
	    this.order = order;

	    
		time_ = 0;
		maxPeriodN = (int) Math.ceil(format.getSampleRate() / toneSet.getTone(toneSetIndex).getFrequency());
		
		ringBuffer = new RingBuffer(maxPeriodN);

		while(!ringBuffer.isFull())
			ringBuffer.enqueue(0);
	}

	// pluck the guitar string by replacing the buffer with white noise
	public void pluck(int tab, double velocity)
	{
		synchronized (pluckSync) {
			isDamping = false;

		    this.currentFreq = toneSet.getTone(toneSetIndex + tab).getFrequency();
		    this.periodN = (int) Math.ceil(format.getSampleRate() / currentFreq);
		    ringBuffer.resize(periodN);
		    this.readOffset = settings.getStringTension() * (this.periodN - 1);
	
		    double loc11 = settings.getStereoSpread() * this.prePan;
		    this.gainL = (1.0 - loc11) * 0.5;
		    this.gainR = (loc11 + 1.0) * 0.5;
		    
		    double loc3 = (this.toneSetIndex + tab - (this.toneSetIndex - this.order * 5.0)) / (toneSet.getToneCount() / 2.0);
		    //loc3 = (this.toneSetIndex + tab - 19.0) / 44.0;
		    double loc4 = settings.getStringDamping();
		    if (settings.isStringDampingMagic()) {
		        this.dc = loc4 + Math.pow(loc3, 0.5) * (1 - loc4) * 0.5 + (1 - loc4) * Math.random() * settings.getStringDampingVariation();
		    } else {
		        this.dc = loc4;
		    }
		    double loc5 = settings.getPluckDampingVariation();
		    double loc6 = settings.getPluckDamping();
		    double loc7 = Math.rint(settings.getPluckDampingMin() + 0 * (settings.getPluckDampingMax() - settings.getPluckDampingMin()));
		    double loc8 = Math.rint(settings.getPluckDampingMin() + 1 * (settings.getPluckDampingMax() - settings.getPluckDampingMin()));
		    double loc9 = loc6 - (loc6 - loc7) * loc5;
		    double loc10 = loc6 + (loc8 - loc6) * loc5;
		    this.pluckDamping = loc9 + dampNoiseGen.norm() * (loc10 - loc9);
		    
		    this.velocity = velocity * 0.25;
		    
		    this.charVariation = settings.getCharacterVariation();
		    
		    double rnd;
		    double rndChar;
		    double rndPluck;
	
			for(int i = 0; i < maxPeriodN; i++)
			{
	
				rndChar = this.character.getCharacterSample(i);
				
				rndPluck = this.stringNoiseGen.bipolar();
	
				
				rnd = (rndChar * (1 - this.charVariation) 
				        + rndPluck * this.charVariation) * this.velocity;
				
				af = af + (rnd - af) * this.pluckDamping;
				
				ringBuffer.enqueue(af);
			}
		}
		
	}
	// advance the simulation one time step
	public void tic()
	{
		synchronized (pluckSync) {
	    
		        // TODO add method for actively damping down the string
		    
		        double loc4 = readOffset;
		        int loc5 = (int) readOffset;
		        int loc6 = loc5 + 1;
		        double loc7 = loc4 - loc5;
	
			double first = ringBuffer.peek(loc5);
			double second = ringBuffer.peek(loc5 + 1);
			
			this.af = first * (1.0 - loc7) + second * loc7;
			
			this.dr = this.dr * 0.0;
			this.dr = this.dr + (this.af - this.df) * this.dc;
			this.df = (this.df + this.dr);
			
			this.df += (0.0 - this.df) / (format.getFrameRate() / DECAY_FACTOR) ;
			
			if (isDamping) {
				df -= df / 10;
			}
			
			ringBuffer.dequeue();
			ringBuffer.enqueue(df);
	
			time_++;
		}
	}
	// return the current sample by peeking the buffer
	public double sample()
	{
		synchronized (pluckSync) {
			return sample(Channel.MONO);
		}
	}
	
	public void damp() {
		synchronized (pluckSync) {
			isDamping = true;
		}
	}
	
	public double sample(Channel channel) {
	    if (channel == Channel.LEFT) {
	        return ringBuffer.peek() * this.gainL;
	    } else if (channel == Channel.RIGHT) {
	        return ringBuffer.peek() * this.gainR;
	    } else {
	        return ringBuffer.peek();
	    }
	}
	
	// return number of times tic was called so far
	public int time()
	{
		return time_;
	}	
	
    
}
