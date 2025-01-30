package bgu.spl.net.impl.stomp;

public class StompFrame {
    private String StompCommand;
    private String[][] Headers;
    private String FrameBody;

    public StompFrame(String StompCommand, String[][] Headers, String FrameBody) {
        this.FrameBody = FrameBody;
        this.StompCommand = StompCommand;
        this.Headers = Headers;
    }

    public String getStompCommand() {
        return this.StompCommand;
    }

    public String[][] getHeaders() {
        return Headers;
    }

    public String getFrameBody() {
        return FrameBody;
    }

    public String toString() {
        String outPut = "";
        outPut = outPut + StompCommand + "\n";

        for (int i = 0; i < Headers.length; i++) {
            outPut = outPut + Headers[i][0] + ": " + Headers[i][1] + "\n";
        }
        if(FrameBody!=null)outPut = outPut + FrameBody + "\n";
        outPut = outPut + '\u0000';
        return outPut;
    }
}
 
