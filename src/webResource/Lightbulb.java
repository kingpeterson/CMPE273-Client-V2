package webResource;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

import org.json.*;

import com.sun.jersey.api.client.*;

import data.ClientMongoDB;


public class Lightbulb {
//	private static final String URN = "0000007-1234-4321-123456654321";
	private static final String URI = "http://localhost:8080/CMPE273/webResource/ServerBootstrap/postClientInfo";
	private static final String serviceURI = "http://localhost:8080/CMPE273/webResource/Rest/postInfo";
	private static String registerURI = "";
	private static String updateURI = "";
//	private static final int serverID = 0;
//	private static final String objectID = "0000000000010001";
//	private static final int objectInstanceID = 0000000000000011;
//	private static final int resourceID = 0000000000000111;
//	private static final int resourceInstanceID = 0000000000001111;
	static boolean isBootstrap = false;
	static String manufacturer = getManufacturer();
	static String productType = getProductType();
	static String objectID = getObjectID();
	static String modelNumber = getModelNumber();
	static String serialNumber = getSerialNumber();
	static String firmwareVersion = getFirmwareVersion();
	static int errorCode = getErrorCode();
	public static int lightWatts = 0;
	public static String lightColor = "white";
//	public static String onTime = "00:00:00";
//	public static String offTime = "00:00:00";
	private static Scanner scan;
	
	
    public static String getManufacturer() {
        return "Philips";
    }
    
    public static String getProductType() {
    	return "Lightbulb";
    }
    
    public static String getObjectID() {
    	return "0000000000010001";
    }

    public static String getModelNumber() {
        return "Model-911";
    }

    public static String getSerialNumber() {
        return "KP-911-000-0001";
    }

    public static String getFirmwareVersion() {
        return "1.0.0";
    }

    public static int getErrorCode() {
        return 0;
    }

	public static void main(String[] args){
		menu();
		scan = new Scanner(System.in);
		int input = scan.nextInt();
		while (input != 0){
			if (input == 1){
				if (!isBootstrap){
					isBootstrap = bootstrap();
					if (isBootstrap)
						System.out.println("Bootstrap completed\n");
				}
				else
					System.out.println("Bootstrap already completed\n");
			}
			
			else if (input == 2){
				register(1);
			}
			
			else if (input == 3){
				register(0);
			}
			
			else if (input == 4){
				System.out.println("Please update the Registration lifetime:");
				int myTimer = scan.nextInt();
				register(myTimer);
			}
			else if (input == 5){
				service();
			}
//			menu();
			input = scan.nextInt();
				
		}
		register(0);
		ClientMongoDB.initialize();
		System.out.println("Device shut down....");
		

	}
	
	public static boolean bootstrap(){
		boolean isBootstrap = false;
		try{
			Client client = Client.create();
			WebResource webResource = client.resource(URI);
			String clientInfo = "{\"Manufacturer\": \""+manufacturer+"\", \"ProductType\": \""+productType+"\", \"ObjectID\": \""+objectID+"\", \"ModelNumber\": \""+modelNumber+"\", \"SerialNumber\": \""+serialNumber+"\", \"FirmwareVersion\": \""+firmwareVersion+"\"}";
			ClientResponse response = webResource.type("application/json").post(ClientResponse.class, clientInfo);
			if(response.getStatus() != 201){
				throw new RuntimeException("Failed HTTP error code:" + response.getStatus());
			}
			String output = response.getEntity(String.class);
			JSONObject obj = new JSONObject(output);
			String found = obj.getString("found");
			if (found.equals("true")){
				lightWatts = obj.getInt("lightWatts");
				lightColor = obj.getString("lightColor");
				registerURI = obj.getString("registerURI");
				updateURI = obj.getString("updateURI");
				System.out.println("Watts: "+lightWatts+"");
				System.out.println("Light Color: "+lightColor+"");
//				System.out.println(registerURI);

				ClientMongoDB.insert(manufacturer, serialNumber, objectID, lightWatts, lightColor, registerURI, updateURI);
				isBootstrap = true;
			}
			else
				System.out.println(found);
			
		} catch(Exception e){
			e.printStackTrace();
		}
		return isBootstrap;
	}
	
	
	public static void register(int myStatus){
		registerURI = ClientMongoDB.search("registerURI");
		Client client = Client.create();
		WebResource webResource = client.resource(registerURI);
		String clientInfo = "{\"Manufacturer\": \""+manufacturer+"\", \"SerialNumber\": \""+serialNumber+"\", \"ObjectID\": \""+objectID+"\", \"Status\": \""+myStatus+"\"}";
		ClientResponse response = webResource.type("application/json").post(ClientResponse.class, clientInfo);
		if(response.getStatus() != 201){
			throw new RuntimeException("Failed HTTP error code:" + response.getStatus());
		}
		String output = response.getEntity(String.class);
		System.out.println(output);
	}
	
	public static void service(){
		try{
			Client client = Client.create();
			WebResource webResource = client.resource(serviceURI);
			String myTime =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
			String input ="{\"Manufacturer\": \""+manufacturer+"\", \"SerialNumber\": \""+serialNumber+"\", \"ObjectID\": \""+objectID+"\", \"Data\": \""+myTime+"\"}";
			ClientResponse response = webResource.type("application/json").post(ClientResponse.class, input);
			
			if (response.getStatus() != 201){
				throw new RuntimeException("Failed HTTP error code:" + response.getStatus());
			}
			
			System.out.println("Output from Server\n");
			String output = response.getEntity(String.class);
			System.out.println(output);
			
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public static void menu(){
		System.out.println("Press 0 to Shut down\n");
		System.out.println("Press 1 to Bootstrap\n");
		System.out.println("Press 2 to Register\n");
		System.out.println("Press 3 to DeRegister\n");
		System.out.println("Press 4 to Update registration\n");
		System.out.println("Press 5 to Use service\n");
	}
	
	public static String update(String objectID, String newInstance, String newValue){
		updateURI = ClientMongoDB.search("updateURI");
		Client client = Client.create();
		WebResource webResource = client.resource(updateURI);
		String clientInfo = "{\"ObjectID\": \""+objectID+"\", \"NewInstance\": \""+newInstance+"\", \"NewValue\": \""+newValue+"\"}";
		ClientResponse response = webResource.type("application/json").post(ClientResponse.class, clientInfo);
		if(response.getStatus() != 201){
			throw new RuntimeException("Failed HTTP error code:" + response.getStatus());
		}
		String output = response.getEntity(String.class);
		return output;
	}
	
	public static String helloWorld(){
		return "Hello World";
	}

}
