package kim.kohlhaas.sone.filter;

public interface FilterFlow {
    
    public void next(float frameValues[][]);
    public void next(float frameValues[][], int offset, int length);
}
