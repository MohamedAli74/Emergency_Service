package bgu.spl.net.impl.stomp;
import java.util.concurrent.ConcurrentHashMap;
import bgu.spl.net.srv.*;
import bgu.spl.net.api.*;


public class StompServer 
{
    private ConcurrentHashMap<String, ConcurrentHashMap<Integer,ConnectionHandler>> channelsSubscribers = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, ConcurrentHashMap<String, Integer>> userSubscribesByChannel = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, String>> userSubscribesByIdSub = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> passcodes = new ConcurrentHashMap<>();
    private Server server;
    private Connectionsimpl connectionsimpl;

    public StompServer(Server server, Connectionsimpl connectionsimpl)
    {   
        connectionsimpl.setStompServer(this);
        this.connectionsimpl = connectionsimpl;
        this.server = server;
    }
    // Getter for channelsSubscribers
    public ConcurrentHashMap<String, ConcurrentHashMap<Integer, ConnectionHandler>> getChannelsSubscribers() {
        return channelsSubscribers;
    }

    // Getter for userSubscribes
    public ConcurrentHashMap<Integer, ConcurrentHashMap<String, Integer>> getUserSubscribesByChannel() {
        return userSubscribesByChannel;
    }

    // Getter for userSubscribes
    public ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, String>> getUserSubscribesByIdSub() {
        return userSubscribesByIdSub;
    }
    

    // Getter for passcodes
    public ConcurrentHashMap<String, String> getPasscodes() 
    {
        return passcodes;
    }


    // Getter for server
    public Server getServer() 
    {
        return server;
    }
    
    public void setConnectionsimpl(Connectionsimpl connectionsimpl){
        this.connectionsimpl = connectionsimpl;
    }

    public static void main(String[] args) 
    {
        StompServer stompServer;
        int numThreads = 99;//TO EDIT

        if (args[1].equals("reactor")) 
        {
            Connectionsimpl connectionsimpl = new Connectionsimpl<>(null);
            stompServer = new StompServer(
                    new Reactor<StompFrame>(
                    numThreads, Integer.parseInt(args[0]),
                    ()->new StompMessagingProtocolImpl(connectionsimpl),
                    ()->new StompMessageEncoderDecoderImpl()
                ),
                connectionsimpl
            );
            
        }
        
        else if(args[1].equals("tpc"))
        {   
            Connectionsimpl connectionsimpl = new Connectionsimpl<>(null);
            stompServer = new StompServer(
                    Server.threadPerClient(
                        Integer.parseInt(args[0]),
                        ()->new StompMessagingProtocolImpl(connectionsimpl),
                        ()->new StompMessageEncoderDecoderImpl()
                    ),
                    connectionsimpl
            );//TO EDIT
        }
        else
        {
            stompServer = null;
            System.out.println("give me an appropirate arguments please!");
        }
        stompServer.getServer().serve();

    }
}
