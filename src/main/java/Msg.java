/**
 * Created by Devjiu on 11/25/2017.
 */
public class Msg {
    public long destination;
    public String body;
    public long chrono;

    Msg(long destinationId) {
        destination = destinationId;
        chrono = System.nanoTime();
    }
}
