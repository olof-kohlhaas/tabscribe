package kim.kohlhaas.sone.filter;



import kim.kohlhaas.sone.filter.FilterProcess.FilterConfig;

public interface ManagedFilter extends Filter {
    
    public void init(FilterConfig config);
    public void onLineChanged();
    
}
