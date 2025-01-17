package bgu.spl.net.api;

import bgu.spl.net.impl.stomp.StompFrame;
import bgu.spl.net.srv.Connections;

public class StompMessagingProtocolImpl implements StompMessagingProtocol<StompFrame> {
    private boolean shouldTerminate=false;
    private int ownersConnectionID; 
    Connections connections;

    @Override
    public void start(int connectionId, Connections connections) {
        this.connections = connections;
        ownersConnectionID = connectionId;
    }

    public void process(StompFrame message){
        if(message.getStompCommand() == "DISCONNECT"){
            shouldTerminate=true;
        }
        if(message.getStompCommand() == "UNSUBSCRIBE"){
            int id = Integer.parseInt(message.getHeaders()[0][1]);
            connections.unsubscribe(id);
        }
        if(message.getStompCommand() == "SUBSCRIBE"){
            String destination;
            int id;

            if(message.getHeaders()[0][0] == destination){
                destination = message.getHeaders()[0][1];
                id = Integer.parseInt(message.getHeaders()[1][1]);                
            }else{
                destination = message.getHeaders()[1][1];
                id = Integer.parseInt(message.getHeaders()[0][1]);
            }
            connections.subscribe(destination,id);
        }
        if(message.getStompCommand() == "SEND"){
            String destination = message.getHeaders()[0][1];
            connections.tryingToSend(destination,message.getFrameBody());
        }
        if(message.getStompCommand() == "CONNECT"){
            String userName = null ;
            String passCode = null ;
            String host = null ;
            String acceptVersion = null;

            for(String[] header : message.getHeaders()){
                if(header[0] == "accept-version")acceptVersion = header[1];
                if(header[0] == "host")host = header[1];
                if(header[0] == "passcode")passCode = header[1];
                if(header[0] == "login")userName = header[1];
            }

            connections.connect(acceptVersion,host,userName,passCode);
        }
    }
    

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
    
    
}
