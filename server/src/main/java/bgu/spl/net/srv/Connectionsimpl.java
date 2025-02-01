package bgu.spl.net.srv;
import bgu.spl.net.impl.stomp.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
public class Connectionsimpl<T> implements Connections<T>
{
    ConcurrentHashMap<String, Integer> uniqueIdByName = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, String> nameByUniqueId = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, ConnectionHandler<T>> handelrsById = new ConcurrentHashMap<>();
    StompServer stompServer;
    AtomicInteger messageID;
    AtomicInteger id;

    public Connectionsimpl(StompServer stompServer)
    {
        this.stompServer = stompServer;
        this.id = new AtomicInteger(0);
        this.messageID = new AtomicInteger(0);
    }

    public void setStompServer(StompServer stompServer){
        this.stompServer = stompServer;
    }

    public String connect(String acceptVersion,String host,String name,String passCode,ConnectionHandler<T> connectionHandler){   
        String outPut = "";
        int result = 0;
        if (passCode == null) 
        {
            outPut = "passcode header is missing/misswritten ,";
        }
        if (name == null) 
        {
            outPut = "login header is missing/misswritten ,";
        }

        else{
        if(uniqueIdByName.get(name)!=null){
            outPut = "this user is connected";
        }
            result = createNewconnect(name,passCode,connectionHandler);
            if (result == -1) 
            {
                outPut = "wrong passowrd for already exists login ,";  
            }
            if (outPut.equals("")) 
            {
                outPut = "true"+result;
            }
        }
        
        if(!host.equals("stomp.cs.bgu.ac.il"))
        {
            outPut = outPut + "you should provide an appropirate host,";
        }
        if (!acceptVersion.equals("1.2")) 
        {
            outPut = outPut + "you should provide an appropirate version ";
        }
        return outPut;
    }
    
    public int createNewconnect(String name ,String passCode ,ConnectionHandler<T> connectionHandler) 
    {
        if (stompServer.getPasscodes().containsKey(name)) 
        {
            if (!stompServer.getPasscodes().get(name).equals(passCode)) 
            {
                return -1;
            }
        }
        else
        {   
            stompServer.getPasscodes().put(name, passCode);
        }
        id.incrementAndGet();
        uniqueIdByName.put(name,id.get());
        nameByUniqueId.put(id.get(), name);
        handelrsById.put(id.get(), connectionHandler);
        return id.get();
    }
    public String subscribe(int connectionId, String destination, int subscriptionID){
        String outPut = "";
        if (subscriptionID == -1) 
        {
            outPut = "id header is missing/misswritten ,";
        }
        if (destination == null) 
        {
            outPut = "destination header is missing/misswritten";
        }
        if(stompServer.getUserSubscribesByChannel().get(connectionId) == null)
        {
            stompServer.getUserSubscribesByChannel().put(connectionId, new ConcurrentHashMap<>());
        }
        if(stompServer.getUserSubscribesByIdSub().get(connectionId) == null)
        {
            stompServer.getUserSubscribesByIdSub().put(connectionId, new ConcurrentHashMap<>());
        }
        if (stompServer.getUserSubscribesByChannel().get(connectionId).get(destination) != null) 
        {
            outPut =  "you had already subscribed to this channel ,";
        }        
        
        if(stompServer.getChannelsSubscribers().get(destination) == null)
        {
            stompServer.getChannelsSubscribers().put(destination, new ConcurrentHashMap<>());
        }

        for(int subId : stompServer.getUserSubscribesByIdSub().get(connectionId).keySet())
        {
            if (subId == subscriptionID) 
            {
                outPut = outPut + "you had used this subscription ID for another channel ,";
            }
        }
        if (outPut.equals("")) 
        {
            stompServer.getUserSubscribesByChannel().get(connectionId).put(destination, subscriptionID);
            stompServer.getUserSubscribesByIdSub().get(connectionId).put(subscriptionID, destination);
            stompServer.getChannelsSubscribers().get(destination).put(subscriptionID,handelrsById.get(connectionId));     
            outPut = "true";
        }
        return outPut;
    }

    public String unSubscribe(int connectionId, int subscriptionID)
    {
        String destination = stompServer.getUserSubscribesByIdSub().get(connectionId).get(subscriptionID);
        String outPut = "";
        if (subscriptionID == -1) 
        {
            outPut = "id header is is missing/misswritten ,";
        }
        if (destination == null) 
        {
            outPut = outPut + "you cant unsubscribe because you hadn't subscribed";
        }

        if (outPut.equals("")) 
        {
            stompServer.getUserSubscribesByIdSub().get(connectionId).remove(subscriptionID);
            stompServer.getChannelsSubscribers().get(destination).remove(subscriptionID);
            stompServer.getUserSubscribesByChannel().get(connectionId).remove(destination);
            outPut = "true";
        }
        return outPut;
    }



    public boolean send(int connectionId, T msg)
    {
        if (handelrsById.get(connectionId) != null) 
        {
            handelrsById.get(connectionId).send(msg);
            return true;
        }
        else
        {
            return false;
        }
    }

    public String send(String channel, T msg, Integer senderId)
    {   
        String outPut = "";
        if (channel == null){
            outPut = "destination header is missing/misswritten";
        }
        else
        {
            ConcurrentHashMap<Integer,ConnectionHandler> subscribers = stompServer.getChannelsSubscribers().get(channel);

            if(!subscribers.containsKey(stompServer.getUserSubscribesByChannel().get(senderId).get(channel))){

                outPut = "the sender is not subscribed to the desired channel";
            }
            else{
                for (ConnectionHandler connectionHandler : subscribers.values()) {
                connectionHandler.send(msg);    
            }
            outPut = "true";
            }
        }
        return outPut;
    }
    
    public void disconnect(int connectionId)
    {  
        if(stompServer.getUserSubscribesByChannel().get(connectionId)!=null){
        for (String channel : stompServer.getUserSubscribesByChannel().get(connectionId).keySet()) 
        {
            stompServer.getChannelsSubscribers().get(channel).remove(stompServer.getUserSubscribesByChannel().get(connectionId).get(channel));
        }
        stompServer.getUserSubscribesByChannel().remove(connectionId);
        stompServer.getUserSubscribesByIdSub().remove(connectionId);
        }
        String name = nameByUniqueId.remove(connectionId);
        uniqueIdByName.remove(name);
        handelrsById.remove(connectionId);
    }
    
    public ConcurrentHashMap<String, Integer> getUniqueIdByName() 
    {
        return uniqueIdByName;
    }
    
    public ConcurrentHashMap<Integer, String> getNameByUniqueId() 
    {
        return nameByUniqueId;
    }
    
    public ConcurrentHashMap<Integer, ConnectionHandler<T>> getHandlersById() 
    {
        return handelrsById;
    }
    
    public StompServer getStompServer() 
    {
        return stompServer;
    }

    public Integer generateMessageID()
    {
        return messageID.incrementAndGet();
    }
}