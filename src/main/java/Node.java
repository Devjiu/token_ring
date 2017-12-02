import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Created by Devjiu on 11/22/2017.
 */
public class Node implements Runnable{
    private MsgQueue inputMsgs;
    private Node nextNode;
    private PrintWriter out;
    private long firstVisitTime = 0;

    public Node() {
        try {
            inputMsgs = new MsgQueue();
            out = new PrintWriter(new FileWriter("D:/python_proj/log.txt"));
        } catch (Exception e) {
            System.out.println("Log file print error.");
            System.exit(13);
        }
    }

    public void setConnection(Node next) {
        nextNode = next;
    }

    public void sendMsg(Msg msg) {
        inputMsgs.putMsg(msg);
    }

    public void readIncomeMsg() {
        Msg msg = inputMsgs.getMsg();
        if (msg == null) return;
        if ( msg.destination == 0) {
            if (firstVisitTime == 0) {
                firstVisitTime = System.nanoTime();
            } else {
                out.println("Node: " +
                        Thread.currentThread().getId() +
                        " Full Circle Latency: " + (System.nanoTime() - firstVisitTime) + " ns. "
                );
                firstVisitTime = System.nanoTime();
            }
        }
        if ( msg.destination == Thread.currentThread().getId()) {
            System.out.println("Achieved message for me: " + msg.destination + " and with text: " + msg.body);
        } else {
            nextNode.sendMsg(msg);
        }

    }

    public void run() {
        while (true) {
            this.readIncomeMsg();
        }
    }
}
