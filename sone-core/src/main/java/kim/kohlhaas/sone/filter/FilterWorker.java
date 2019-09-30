package kim.kohlhaas.sone.filter;

import javax.sound.sampled.AudioFormat;

import kim.kohlhaas.sone.buffer.PrimitiveDoubleQueue2D;
import kim.kohlhaas.sone.buffer.PrimitiveFloatQueue2D;
import kim.kohlhaas.sone.filter.FilterProcess.FilterConfig;

public interface FilterWorker {
    public PrimitiveFloatQueue2D getInput();
    public FilterConfig getConfig();
    public void terminate();
    public float[][] emptyLastChunk();
    public float[][] emptyInput();
    public double getFilterTimeScale();
    public Filter getFilter();
    public int getFramesInProgress();
    public void onAddedToFilterChain();
    public void onRemovedFromFilterChain();
    public void onLineChanged(AudioFormat format);
    public void flush();
}
