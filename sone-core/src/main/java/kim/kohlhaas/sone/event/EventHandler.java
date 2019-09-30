package kim.kohlhaas.sone.event;

public interface EventHandler<T extends Event> {
    public void handle(T event);
}
