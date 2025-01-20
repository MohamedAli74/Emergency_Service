package bgu.spl.net.api;

import bgu.spl.net.impl.stomp.StompFrame;
import bgu.spl.net.srv.BlockingConnectionHandler;
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
        if(message.getStompCommand()=="DISCONNECT"){
            shouldTerminate=true;
            String response = connections.disconnect(ownersConnectionID);//should be boolean ?
        if(!response.equals("true")){//TO DO:to handle the errors
            sendError(message, 0, response, message.getHeaders()[0][1]);
        }else{
            sendReceipt(message.getHeaders()[0][1]);
        
        }
    }
        if(message.getStompCommand() == "UNSUBSCRIBE"){
            String receipt=null;
            int id;
            for(String[] header : message.getHeaders()){
                if(header[0] == "receipt")receipt = header[1];
                if(header[0] == "id")id = Integer.parseInt(header[1]);  
            }
            String response = connections.unsubscribe(ownersConnectionID,id);
            if(!response.equals("true")){
                sendError(message,response, receipt);
            }
            if(receipt!=null && response.equals("true"))sendReceipt(receipt);
        }
        if(message.getStompCommand() == "SUBSCRIBE"){
            String destination , receipt;
            int id;
            for(String[] header : message.getHeaders()){
                if(header[0] == "receipt")receipt = header[1];
                if(header[0] == "id")id = Integer.parseInt(message.getHeaders()[0][1]);
                if(header[0] == "destination")destination = header[1];  
            }
            String responce =connections.subscribe(ownersConnectionID,destination,id)
            if(!responce.equals("true")){
                sendError(message, responce, receipt);
            }
            else{
            if(receipt!=null && responce.equals("true"))sendReceipt(message);
        }
    }
        if(message.getStompCommand() == "SEND"){
            String destination;
            String receipt;
            for(String[] header : message.getHeaders()){
                if(header[0] == "receipt")receipt = header[1];
                if(header[0] == "destination")destination = header[1];
            }
            
            String[][] headers = {{"subscription",ownersConnectionID.toString()},{"message-id" , connections.generateMessageID().toString()},{"destination" , destination}};
            StompFrame msg = new StompFrame("MESSAGE", headers, message.getFrameBody());
            String response = connections.send(destination,msg));
            if(!response.equals("true")){
                sendError(message, response, receipt);
            }if(receipt!=null && response.equals("true"))sendReceipt(receipt);
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
            String response  = connections.connect(acceptVersion,host,userName,passCode,connectionHandler);
            if(!(receipt.substring(0, 4)).equals("true")){
                start(Integer.parseInt(response.substring(4, response.length())),connections);
                sendError(message, response, receipt);
            }else{
                if(receipt!=null)sendReceipt(receipt);
            }
            
            
        }
    }

    private void sendReceipt(String receiptId){
        String[][] h = new String[1][2];
            h[0][0] = "receipt-id";
            h[0][1]=receiptId; 
            StompFrame receipt = new StompFrame("RECEIPT",h, "");
            connections.send( ownersConnectionID,receipt);
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
                     "The message:\n----\n"+FaultyFrame.toString()+"\n----");
        connections.send(ownersConnectionID, error);
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    public void setConnectionHandler(ConnectionHandler connectionHandler){
        this.connectionHandler = connectionHandler;
    }
}
