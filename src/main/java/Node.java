import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Devjiu on 11/22/2017.
 */
public class Node implements Runnable{
    private MsgQueue inputMsgs;
    private Node nextNode;
    private PrintWriter out = new PrintWriter(new FileWriter("D:/python_proj/log.txt"));

    public Node() throws IOException {
        inputMsgs = new MsgQueue();
    }

    public void setConnection(Node next) {
        nextNode = next;
    }

    public void sendMsg(Msg msg) {
        //System.out.println("Achieved message: " + msg.destination + " and with text: " + msg.body +
        //        ". Node num: " + Thread.currentThread().getId());
        inputMsgs.putMsg(msg);
    }

    public void readIncomeMsg() {
        Msg msg = inputMsgs.getMsg();
        //System.out.println("I am listening: " + Thread.currentThread().getId());
        if (msg == null) return;
        if (msg.destination == 19) {
            out.println("Node: " +
                    Thread.currentThread().getId() +
                    " Time: " + (System.nanoTime() - msg.chrono) + " ns");
            msg.chrono =  System.nanoTime();
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
