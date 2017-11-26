import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Devjiu on 11/25/2017.
 */
public class MsgQueue {
    private ConcurrentLinkedQueue<Msg> buffer;

    MsgQueue() {
        buffer = new ConcurrentLinkedQueue<>();
    }
    public void putMsg(Msg input) { buffer.add(input); }

    public Msg getMsg() { return buffer.poll();}
}
