/**
 * 
 */
package AP2DX;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import AP2DX.specializedMessages.*;

/**
 * @author Jasper Timmer
 *
 */
public class AP2DXMessage extends Message {
	
	public AP2DXMessage(String in, Module source, Module destination) {
		super(in, source, destination);
        parseMessage();
	}
	
	public AP2DXMessage(String in, Module source) {
		super(in, source);
        parseMessage();
	}

	/**
	 * @see AP2DX.Message#parseMessage()
	 */
	@Override
	public void parseMessage() 
    {
		JSONParser parser = new JSONParser();
		try 
        {
			values = (Map)parser.parse(messageString);
            destinationModuleId = Module.valueOf(values.get("destinationModuleId").toString());
            sourceModuleId = Module.valueOf(values.get("sourceModuleId").toString());
        }
        catch (ParseException pe) 
        {   
            System.out.println("Error in AP2DXMessage.parseMessage()");
			System.out.println("position: " + pe.getPosition());
			System.out.println(pe);
		}
	}

    public void compileMessage()
    {
               
    }
}








