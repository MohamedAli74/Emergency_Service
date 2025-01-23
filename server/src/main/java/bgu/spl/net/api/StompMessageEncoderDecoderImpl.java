package bgu.spl.net.api;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import bgu.spl.net.impl.stomp.StompFrame;

public class StompMessageEncoderDecoderImpl implements MessageEncoderDecoder<StompFrame>{
    private ByteBuffer currentMessage;
    /**
     * add the next byte to the decoding process
     *
     * @param nextByte the next byte to consider for the currently decoded
     * message
     * @return a message if this byte completes one or null if it doesnt.
     */

     @Override
     public StompFrame decodeNextByte(byte nextByte) {
        currentMessage.put(nextByte);
        if(nextByte == 0){
            String messageString = new String(currentMessage.array());
            String[] lines = messageString.split("\n");
            String StompCommand = lines[0];
            ArrayList<String[]> Headers = new ArrayList<>();
            int i ,numberOfHeaders=0;
            if(StompCommand.equals("DISCONNECT"))numberOfHeaders=0;
            if(StompCommand.equals("SEND"))numberOfHeaders=1;
            if(StompCommand.equals("SUBSCRIBE"))numberOfHeaders =2;
            if(StompCommand.equals("UNSUBSCRIBE"))numberOfHeaders =1;
            if(StompCommand.equals("CONNECT"))numberOfHeaders =4;
            for(String line : lines)if(line.length()>7 && line.substring(0, 7).equals("receipt"))numberOfHeaders+=1;

            for(i = 1 ;i < numberOfHeaders + 1 ; i++){
                String[] splitted = lines[i].split(":");
                Headers.add(splitted);
            }
            String FrameBody = "";
            for(;i < lines.length; i++){
                FrameBody = FrameBody + lines[i];
            }
            return new StompFrame(StompCommand, (String[][])Headers.toArray(), FrameBody);
        }
        return null;
     }

    /**
     * encodes the given message to bytes array
     *
     * @param message the message to encode
     * @return the encoded bytes
     */

     @Override
    public byte[] encode(StompFrame message) {
        byte[] output = null;
        output = message.toString().getBytes(StandardCharsets.UTF_8);
        return output;
    }

}
