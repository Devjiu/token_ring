import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Devjiu on 11/26/2017.
 */
public class Main {
    private static final int TOKENS_NUMBER = 5;

    public static void main(String[] args) {
        Node[] tokens = new Node[TOKENS_NUMBER];
        try {
            Arrays.fill(tokens, new Node());
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < tokens.length; i++) {
            if (i + 1 != tokens.length ) {
                tokens[i].setConnection(tokens[i + 1]);
            } else {
                tokens[i].setConnection(tokens[0]);
            }
        }

        Msg[] msgs = new Msg[10];
        for(int i = 0; i < msgs.length; i++) {
            msgs[i] = new Msg(14 + i);
            msgs[i].body = "something " + i;
            tokens[0].sendMsg(msgs[i]);
        }

        for ( Node nd: tokens) {
            Thread t = new Thread(nd);
            t.start();
        }
    }
}
