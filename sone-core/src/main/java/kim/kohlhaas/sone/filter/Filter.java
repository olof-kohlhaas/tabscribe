package kim.kohlhaas.sone.filter;

public interface Filter {

    void writeFrame(float[][] frameValues, FilterFlow filterFlow);

}