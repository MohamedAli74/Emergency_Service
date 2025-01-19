package bgu.spl.net.api;

import bgu.spl.net.impl.stomp.StompFrame;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.Connectionsimpl;

public class StompMessagingProtocolImpl implements StompMessagingProtocol<StompFrame> {
    private boolean shouldTerminate=false;
    private int ownersConnectionID; 
    private Connectionsimpl connections;
    private ConnectionHandler connectionHandler;

    @Override
    public void start(int connectionId, Connections connections) {
        this.connections = (Connectionsimpl)connections;
        ownersConnectionID = connectionId;
    }

    public void process(StompFrame message){
        if(message.getStompCommand() == "DISCONNECT"){
            if(message.getHeaders().length==1){
            shouldTerminate=true;
            sendReceipt(message);
            connections.disconnect(ownersConnectionID);
        }else{
            String[][] h = new String[2][2];
            h[0][0] = "receipt-id";
            h[0][1]=message.getHeaders()[0][1];
            h[1][0] = "message";
            h[1][1] = "wrong number of headers";

            String FrameBody = "The message:\n----\n" + message.toString() +"\n----";

            StompFrame error = new StompFrame("RECEIPT",h, "");
            connections.send( ownersConnectionID,error);
        }
    }
        if(message.getStompCommand() == "UNSUBSCRIBE"){
            String receipt=null;
            int id;
            for(String[] header : message.getHeaders()){
                if(header[0] == "receipt")receipt = header[1];
                if(header[0] == "id")id = Integer.parseInt(header[1]);  
            }
            connections.unsubscribe(ownersConnectionID,id);
            if(receipt!=null)sendReceipt(message);
        }
        if(message.getStompCommand() == "SUBSCRIBE"){
            String destination , receipt;
            int id;
            for(String[] header : message.getHeaders()){
                if(header[0] == "receipt")receipt = header[1];
                if(header[0] == "id")id = Integer.parseInt(message.getHeaders()[0][1]);
                if(header[0] == "destination")destination = header[1];  
            }
            if(receipt!=null)sendReceipt(message);
            connections.subscribe(ownersConnectionID,destination,id);
        }
        if(message.getStompCommand() == "SEND"){
            String destination;
            String receipt;
            for(String[] header : message.getHeaders()){
                if(header[0] == "receipt")receipt = header[1];
                if(header[0] == "destination")destination = header[1];
            }
            if(receipt!=null)sendReceipt(message);
            connections.tryingToSend(destination,message.getFrameBody());
        }
        if(message.getStompCommand() == "CONNECT"){
            String userName = null ;
            String passCode = null ;
            String host = null ;
            String acceptVersion = null;
            String receipt;

            for(String[] header : message.getHeaders()){
                if(header[0] == "accept-version")acceptVersion = header[1];
                if(header[0] == "host")host = header[1];
                if(header[0] == "passcode")passCode = header[1];
                if(header[0] == "login")userName = header[1];
                if(header[0] == "receipt")receipt = header[1];
            }
            if(receipt!=null)sendReceipt(message);
            connections.connect(acceptVersion,host,userName,passCode,connectionHandler);
        }
    }

    private void sendReceipt(StompFrame message){
        String[][] h = new String[1][2];
            h[0][0] = "receipt-id";
            h[0][1]=message.getHeaders()[0][1]; 
            StompFrame receipt = new StompFrame("RECEIPT",h, "");
            connections.send( ownersConnectionID,receipt);
    }
    

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
    
    
}
