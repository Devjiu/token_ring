import java.util.Arrays;

/**
 * Created by Devjiu on 11/26/2017.
 */
public class Main {
    private static int TOKENS_NUMBER;
    private static int MESSAGES_NUMBER;

    public static void main(String[] args) {
        TOKENS_NUMBER = Integer.parseInt(args[0]);
        MESSAGES_NUMBER = Integer.parseInt(args[1]);

        Node[] tokens = new Node[TOKENS_NUMBER];
        Arrays.fill(tokens, new Node());


        for (int i = 0; i < tokens.length; i++) {
            if (i + 1 != tokens.length ) {
                tokens[i].setConnection(tokens[i + 1]);
            } else {
                tokens[i].setConnection(tokens[0]);
            }
        }

        Msg[] msgs = new Msg[MESSAGES_NUMBER];
        msgs[0] = new Msg(0);
        tokens[0].sendMsg(msgs[0]);
        for(int i = 1; i < msgs.length; i++) {
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
