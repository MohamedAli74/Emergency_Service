package bgu.spl.net.impl.stomp;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import bgu.spl.net.srv.*;
import bgu.spl.net.api.*;


public class StompServer 
{
    private ConcurrentHashMap<String, ConcurrentHashMap<Integer,ConnectionHandler>> channelsSubscribers = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, ConcurrentHashMap<String, Integer>> userSubscribesByChannel = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, String>> userSubscribesByIdSub = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> passcodes = new ConcurrentHashMap<>();
    private Server server;

    public StompServer(Server server)
    {
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
    

    public static void main(String[] args) 
    {
        StompServer stompServer;
        int numThreads = 99;//TO EDIT
        if (args[2] == "reactor") 
        {
            stompServer = new StompServer(new Reactor<StompFrame>(numThreads, Integer.parseInt(args[1]),()->new StompMessagingProtocolImpl(),()->new StompMessageEncoderDecoderImpl()));//TO EDIT
        }
        else if(args[2] == "tpc")
        {   
            stompServer = new StompServer(Server.threadPerClient(Integer.parseInt(args[1]), null, null));//TO EDIT
        }
        else
        {
            stompServer = null;
            System.out.println("give me an appropirate arguments please!");
        }

        Connectionsimpl connectionsimp = new Connectionsimpl<>(stompServer); 
        stompServer.getServer().serve();

    }
}
