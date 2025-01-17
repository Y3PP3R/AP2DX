/**
 * 
 */
package AP2DX.coordinator;

import java.net.*;
import java.util.ArrayList;
import java.io.*;

import AP2DX.*;
import AP2DX.specializedMessages.*;
import AP2DX.usarsim.*;
import AP2DX.usarsim.specialized.*;

/**
 * @author Jasper Timmer
 * 
 */
public class Program extends AP2DXBase {
    PrintWriter out;
    UsarSimMessageReader in;
    UsarMessageParser parser;

    /**
     * entrypoint of coordinator
     */
    public static void main(String[] args) {
        new Program();
    }

    /**
     * constructor
     */
    public Program() {
        super(Module.COORDINATOR); // Explicitly calls base constructor
        System.out.println(" Running Coordinator... ");

    }

    /**
     * Sets up the outgoing connection, the messageParserThread and initiates the Robot
     */
    @Override
        protected void doOverride() 
        {
            config = readConfig();

            String address = config.get("sim_address").toString();
            int port = Integer.parseInt(config.get("sim_port").toString());

            try 
            {
                System.out.printf("Connectig to %s on %s for UsarSim...\n", address, port);
                Socket socket = new Socket(address, port);
                System.out.println("Coordinator connected to USARsim");
                out = new PrintWriter(socket.getOutputStream(), true);
                parser = new UsarMessageParser(this, IAM, Module.SENSOR, socket);
                parser.start();
            }
            catch (Exception ex) 
            {
                ex.printStackTrace();
            }

            UsarSimMessage message = new UInitMessage();

            try 
            {
                out.println(message.toString());
            }
            catch (Exception e) 
            {
                e.printStackTrace();
            }
        }

    /**
     * ComponentLogic (for now) creates a message from an USAR_SENSOR message,
     * to forward the sonar sensor messages to our sensor module.
     * 
     * @author Maarten de Waard
     */
    @Override
        public ArrayList<AP2DXMessage> componentLogic(Message msg) {
            ArrayList<AP2DXMessage> messageList = new ArrayList<AP2DXMessage>();
            switch (msg.getMsgType()) {
                case USAR_SENSOR:
                    try {
                        USonarSensorMessage messageIn = new USonarSensorMessage(
                                (UsarSimMessage) msg);
                        // Create a new message to the Sensor module
                        SonarSensorMessage message = new SonarSensorMessage(IAM,
                                Module.SENSOR);
                        // Put the right values in the message
                        message.setRangeArray(messageIn.getData());
                        message.setTime(messageIn.getTime());
                        messageList.add(message);
                    } catch (Exception e) {
                        System.err
                            .println("Some exception occured while making a SonarMessage");
                        System.err.println(e.getMessage());
                    }
                case AP2DX_MOTOR:
                    try
                    {
                        MotorMessage message = new MotorMessage((AP2DXMessage) msg);
                        UDriveMessage usarMessage = message.toUsarSimMessage();
                        out.println(usarMessage);
                    }
                    catch ( Exception e)
                    {
                        System.out.println("Something went wrong with the drive message in Coordinator");
                    }
                default:
            }
            return messageList;
        }
}
