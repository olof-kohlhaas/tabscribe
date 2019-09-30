package kim.kohlhaas.sone.synthesis;
/*************************************************************************
 * Dependencies : Java
 * Description  : 
 *  
 *  Creates a ring buffer of a set size.  Constructor inputs the capacity
 *  of the ring buffer.  Enqueue will increase the size and queue up the item
 *  until the buffer is full, in which case it will overflow subsequent items
 *  onto the end of the queue.
 *               
 *****************************************************************************/
    

public class RingBuffer
{
	// a double array to hold the contents of the buffer
    private double buffer_[];
    // the index of the first item in the 'first' position
    private int first_;
    // the index of the item in the last position
    private int last_;
    // the current size of the buffer
    private int size_;
    // the capacity of the buffer.  Read only
    private final int maxCapacity;
    
    private int capacity_;
    
    // create an empty ring buffer, with given max capacity
    public RingBuffer(int capacity) 
    {
        size_ = 0;
        maxCapacity = capacity;
        capacity_ = capacity;
        buffer_ = new double[maxCapacity];
        first_ = 0;
        last_ = first_;
    }
    
    public void resize(int capacity) {
        if (capacity > maxCapacity) {
            throw new RuntimeException("max capacity exceeded.");
        } else {
            capacity_ = capacity;
            if (size_ > capacity_) {
                size_ = capacity_;
                first_ = 0;
                last_ = 0;
            } else {
                first_ = 0;
                last_ = size_;
            }
            
        }
    }
    
    // return capacity
    public int capacity() 
    {
    	return capacity_;
    }
    // return number of items currently in the buffer
    public int size() 
    {
        return size_;
    }
    // is the buffer empty (size equals zero)?
    public boolean isEmpty() 
    {
        if(size_ == 0)
            return true;
        
        return false;
    }
    // is the buffer full?
    public boolean isFull()
    {
        if(size_ == capacity_)
            return true;
        
        return false;
    }
    
    // add an item to the buffer
    public void enqueue(double x)
    {
    	// wrap the last item around to the 0 position and queue up
        if(last_ == capacity_)
        {
        	last_ = 0;
        	enqueue(x);
        // if the buffer is full, special care needs to be taken to increment first and last
        } else if(isFull()) {
        	// place the item where first is (last should be the same as first)
        	if(last_ != first_)
        		throw new RuntimeException("last does not equal first, should not have got here!");
        	// set the buffer item in last
        	buffer_[last_] = x;
        	// never point first at an invalid position in the array.
        	if (first_ + 1 == capacity_)
        		first_ = 0;
        	else
        		first_++;
        	last_++;
        // simply add an item to the buffer
        } else {
        	// enqueue the item!
        	buffer_[last_] = x;
        	last_++;
        	size_++;
        }
    }
    
    // remove an item from the buffer and update its pointer positions
    public double dequeue()
    {
    	// don't remove from an empty buffer.. bad things happen.
    	if(isEmpty())
    	{
    		throw new RuntimeException("The ring buffer is empty!");
    	}
    	
        // store the value in the buffer at current position
        double item = buffer_[first_];
        // don't point first where there is no item
        if(first_ == capacity_ - 1)
          	first_ = 0;
        else
        	// update our position in the buffer
           	first_++;
        // decrease the size of the buffer and return the value
        size_--;
        return item;
        
    }
    
    public double peek() {
        return peek(0);
    }
    
    // peek the first position in the buffer without dequeueing
    public double peek(int offset)
    {
        int index;
    	// can't peek an empty buffer, right
    	if(isEmpty())
    		throw new RuntimeException("The ring buffer is empty!");
    	
    	index = first_ + offset;
    	index = index % capacity_;
    	
    	// simply return the first position of the buffer
    	return buffer_[index];
    	
    }
    
    // convert the buffer to a string for debugging
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        // simply loop through the buffer
        for(int i = 0; i < capacity_; i++)
        {
        	sb.append(String.format("%.2f  ", buffer_[i]));
        }
        
        // return the string representation
        return sb.toString();
    }
        
}
