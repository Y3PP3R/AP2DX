package AP2DX;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.Thread;
import java.net.Socket;

import com.sun.xml.internal.ws.api.message.Message;

/**
 * 
 * This class represents a connection with another module. It continues to listen for
 * incoming messages and puts them in the BlockingQueue of the abstract base. There
 * the message will wait until it can be processed by the Logic thread. <br />
 * <br />
 * Besides listening for message, it can also send messages. 
 * @author Maarten Inja
 * */ 
public class ConnectionHandler extends Thread
{
    
    private Socket socket;
    private AP2DXBase base;
    public final Module moduleID;

    private PrintWriter out;
    private BufferedReader in;

   
    /**
     * The constructor, saves arguments and reads in- and outcoming streams
     * from the socket.
     * 
     * This constructor is for the base-class self-started connections.
     * 
     * @throws IOException 
     */ 
    public ConnectionHandler(AP2DXBase base, Socket socket, Module moduleID) throws IOException
    {
        this.base = base;
        this.socket = socket;
        this.moduleID = moduleID;
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    
    /**
     * The constructor, saves arguments and reads in- and outcoming streams
     * from the socket.
     * 
     * This constructor is for the incoming connections.
     * 
     * @throws IOException 
     */ 
    public ConnectionHandler(AP2DXBase base, Socket socket) throws IOException
    {
        this.base = base;
        this.socket = socket;
        
        Message firstIncomingMessage = in.readLine();
        this.moduleID = (Module)firstIncomingMessage.get("component");
        
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    /** Thread logic. */
    public void run()
    {
        try
        {
            String plainIncomingMessage = in.readLine();
            base.getBlockingQueue().put(plainIncomingMessage);
        }    
        catch (Exception e)
        {
            base.logger.severe(e.getMessage() + " IN ConnectionHandler.run, attempting to read message.");
        }
    } 

    /** Sends a message object, this is currently only the message object 
    * toString().
    */
    public void sendMessage(Message message)
    {
        out.print(message.toString());
    }


}
