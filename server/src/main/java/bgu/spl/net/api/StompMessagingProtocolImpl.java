package bgu.spl.net.api;

import bgu.spl.net.impl.stomp.StompFrame;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.Connectionsimpl;

public class StompMessagingProtocolImpl implements StompMessagingProtocol<StompFrame> {
    private boolean shouldTerminate=false;
    private Integer ownersConnectionID; 
    private Connectionsimpl connections;
    private ConnectionHandler connectionHandler;

    public StompMessagingProtocolImpl(Connectionsimpl connections){
        this.connections = connections;
    }

    @Override
    public void start(int connectionId, Connections connections) {
        ownersConnectionID = connectionId;
    }

    public void process(StompFrame message){
        if(message.getStompCommand().equals("CONNECT") ||
            message.getStompCommand().equals("SUBSCRIBE") ||
            message.getStompCommand().equals("UNSUBSCRIBE") ||
            message.getStompCommand().equals("SEND") ||
            message.getStompCommand().equals("DISCONNECT"))

            System.out.println("Processing message:\n----\n"+message.toString()+"\n----");

        if(message.getStompCommand().equals("DISCONNECT")){
            if(!message.getHeaders()[0][0].equals("receipt")){
                sendError(message, "the receipt header is missing/misswritten", message.getHeaders()[0][1]);
            }else{
                shouldTerminate=true;
                sendReceipt(message.getHeaders()[0][1]);
                connections.disconnect(ownersConnectionID);
        
        }
    }
        if(message.getStompCommand().equals("UNSUBSCRIBE")){
            String receipt=null;
            int id=-1;
            for(String[] header : message.getHeaders()){
                if(header[0].equals("receipt"))receipt = header[1];
                if(header[0].equals("id"))id = Integer.parseInt(header[1]);  
            }
            String response = connections.unSubscribe(ownersConnectionID,id);
            if(!response.equals("true")){
                sendError(message,response, receipt);
            }
            if(receipt!=null && response.equals("true"))sendReceipt(receipt);
        }
        if(message.getStompCommand().equals("SUBSCRIBE")){


            String destination = null , receipt = null;
            int id=-1;
            for(String[] header : message.getHeaders()){
                if(header[0] .equals("receipt"))receipt = header[1];
                if(header[0] .equals("id"))id = Integer.parseInt(header[1]);
                if(header[0] .equals("destination"))destination = header[1];  
            }
            String response =connections.subscribe(ownersConnectionID,destination,id);
            if(!response.equals("true")){
                sendError(message, response, receipt);
            }
            else{
            if(receipt!=null && response.equals("true"))sendReceipt(receipt);
        }
    }
        if(message.getStompCommand().equals("SEND")){
            String destination=null;
            String receipt=null;
            for(String[] header : message.getHeaders()){
                if(header[0] .equals("receipt"))receipt = header[1];
                if(header[0] .equals("destination"))destination = header[1];
            }
            String[][] headers = {{"subscription",ownersConnectionID.toString()},{"message-id" , connections.generateMessageID().toString()},{"destination" , destination}};
            String FrameBody = RemoveExtraLinesFromFrameBody(message.getFrameBody());
            StompFrame msg = new StompFrame("MESSAGE", headers, FrameBody);
            String response = connections.send(destination,msg,ownersConnectionID);
            
            if(!response.equals("true")){
                sendError(message, response, receipt);
            }
            if(receipt!=null && response.equals("true"))sendReceipt(receipt);
        }
        if(message.getStompCommand().equals("CONNECT")){
            String userName = null ;
            String passCode = null ;
            String host = null ;
            String acceptVersion = null;
            String receipt = null;

            for(String[] header : message.getHeaders()){
                if(header[0] .equals("accept-version"))acceptVersion = header[1];
                if(header[0] .equals("host"))host = header[1];
                if(header[0] .equals("passcode"))passCode = header[1];
                if(header[0] .equals("login"))userName = header[1];
                if(header[0] .equals("receipt"))receipt = header[1];
            }
            String response  = connections.connect(acceptVersion,host,userName,passCode,connectionHandler);
            if(!(response.substring(0, 4)).equals("true")){
                sendError(message, response, receipt);
            }else{
                start(Integer.parseInt(response.substring(4, response.length())),connections);
                StompFrame connected = new StompFrame("CONNECTED", new String[][]{{"version" , acceptVersion}} , "");

                connections.send(ownersConnectionID, connected);
                if(receipt!=null)sendReceipt(receipt);
            }    
        }
    }

    private void sendReceipt(String receiptId){
        String[][] h = new String[1][2];
        h[0][0] ="receipt-id";
        h[0][1]=receiptId; 
        StompFrame receipt = new StompFrame("RECEIPT",h, "");
        connections.send( ownersConnectionID , receipt);

    }
    
    private void sendError(StompFrame FaultyFrame,String message,String receiptId){
        String[][] headers;
        if(receiptId!=null){
            headers = new String[][]{{"message" , message},{"receipt-id",receiptId}};
        }else{
            headers = new String[][]{{"message" , message}};
        }
        StompFrame error = new StompFrame(
                    "ERROR" ,
                     headers ,
                     "The message:\n----\n"+FaultyFrame.toString()+"\n----"
        );
        
        if(ownersConnectionID!=null)
            connections.send(ownersConnectionID, error);
        else{    
            connectionHandler.send(error);
        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    public void setConnectionHandler(ConnectionHandler connectionHandler){
        this.connectionHandler = connectionHandler;
    }
    
    private String RemoveExtraLinesFromFrameBody(String frameBody){
        String[] lines = frameBody.split("\n");
        String newFrameBody = "";
        for(String line : lines){
            if(!line.equals("")){
                newFrameBody = newFrameBody + line + "\n";
            }
        }
        return newFrameBody;
    }
}
