package bgu.spl.net.srv;
import bgu.spl.net.impl.stomp.*;
import java.util.concurrent.ConcurrentHashMap;
public class Connectionsimpl<T>
{
    ConcurrentHashMap<String, Integer> uniqueIdByName = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, String> nameByUniqueId = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, ConnectionHandler<T>> handelrsById = new ConcurrentHashMap<>();
    StompServer stompServer;
    int id = 0;

    public Connectionsimpl(StompServer stompServer)
    {
        this.stompServer = stompServer;
        this.id = 0;
    }

    public int connect(String acceptVersion,String host,String name,String passCode,ConnectionHandler<T> connectionHandler)
    {
        int outPut;
        outPut = createNewconnect(name, connectionHandler);
        if (outPut == -1 || !host.equals("stomp.cs.bgu.ac.il") || !acceptVersion.equals("version 1.2")) 
        {
            return -1;   
        }
        stompServer.getPasscodes().put(name, passCode);
        return outPut;
    }
    
    public int createNewconnect(String name , ConnectionHandler<T> connectionHandler) 
    {
        if (uniqueIdByName.containsKey(name)) 
        {
            return -1;
        }
        id++;
        uniqueIdByName.put(name,id);
        nameByUniqueId.put(id, name);
        handelrsById.put(id, connectionHandler);
        return id;
    }
    public boolean subscribe(int connectionId, String destination, int subscriptionID)
    {
        if(stompServer.getUserSubscribesByChannel().get(connectionId) == null)
        {
            stompServer.getUserSubscribesByChannel().put(connectionId, new ConcurrentHashMap<>());
        }
        if(stompServer.getUserSubscribesByIdSub().get(connectionId) == null)
        {
            stompServer.getUserSubscribesByIdSub().put(connectionId, new ConcurrentHashMap<>());
        }
        if (stompServer.getUserSubscribesByChannel().get(connectionId).get(destination) != null || stompServer.getUserSubscribesByIdSub().get(connectionId).get(destination) != null) 
        {
            return false;
        }        
        
        if(stompServer.getChannelsSubscribers().get(destination) == null)
        {
            stompServer.getChannelsSubscribers().put(destination, new ConcurrentHashMap<>());
        }
        for (int channelIDs : stompServer.getChannelsSubscribers().get(destination).keySet()) 
        {
            if (channelIDs == subscriptionID) 
            {
                return false;
            }
        }
        for(int subId : stompServer.getUserSubscribesByIdSub().get(connectionId).keySet())
        {
            if (subId == subscriptionID) 
            {
                return false;
            }
        }
        stompServer.getUserSubscribesByChannel().get(connectionId).put(destination, subscriptionID);
        stompServer.getUserSubscribesByIdSub().get(connectionId).put(subscriptionID, destination);
        stompServer.getChannelsSubscribers().get(destination).put(subscriptionID,handelrsById.get(connectionId));

        return true;
    }

    public boolean unSubscribe(int connectionId, int subscriptionID)
    {
        String destination = stompServer.getUserSubscribesByIdSub().get(connectionId).get(subscriptionID);
        if (destination == null ||stompServer.getChannelsSubscribers().get(destination) == null || stompServer.getChannelsSubscribers().get(destination).get(subscriptionID) == null) 
        {
            return false;
        }
        if (stompServer.getUserSubscribesByChannel().get(connectionId) == null || stompServer.getUserSubscribesByChannel().get(connectionId).get(destination) != connectionId)
        {
            return false;
        }
        stompServer.getUserSubscribesByIdSub().get(connectionId).remove(subscriptionID);
        stompServer.getChannelsSubscribers().get(destination).remove(subscriptionID);
        stompServer.getUserSubscribesByChannel().get(connectionId).remove(destination);

        return true;
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

    public void send(String channel, T msg)
    {
        ConcurrentHashMap<Integer,ConnectionHandler> subscribers = stompServer.getChannelsSubscribers().get(channel);
        for (ConnectionHandler connectionHandler : subscribers.values()) 
        {
            connectionHandler.send(msg);    
        }
    }

    public void disconnect(int connectionId)
    {  
        for (String channel : stompServer.getUserSubscribesByChannel().get(connectionId).keySet()) 
        {
            stompServer.getChannelsSubscribers().get(channel).remove(stompServer.getUserSubscribesByChannel().get(connectionId).get(channel));
        }
        stompServer.getUserSubscribesByChannel().remove(connectionId);
        stompServer.getUserSubscribesByIdSub().remove(connectionId);
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
}