package data;

import java.net.UnknownHostException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class DeviceDAO {
	private static MongoClient client = null;
	
	public static MongoClient Connect(){
		if(client!=null)
			return client;
        try {
            final ServerAddress serverAddress = new ServerAddress("ds045031.mongolab.com", 45031);

            final MongoCredential credential = MongoCredential.createMongoCRCredential("cmpe273team1", 
                    "lwm2m", "cmpe273".toCharArray());

            client = new MongoClient(serverAddress, Arrays.asList(credential));
        } catch (final UnknownHostException e) {
        	e.printStackTrace();
        }
		return client;
	}
	
	public static int insertDeviceData(String manufacturer, String model, String sn, JSONObject obj) throws JSONException{
    	WriteResult result = null;

		DB db = client.getDB("lwm2m");
		DBCollection collection = db.getCollection("CLIENTS");

		ArrayList<DBObject> objArray = new ArrayList<DBObject>();
		JSONArray reArray = obj.getJSONArray("Resource");
//		ArrayList<DBObject> objArray1 = new ArrayList<DBObject>();
//		JSONArray serArray = obj.getJSONArray("ServiceProvider");
		String serviceProvider = obj.getString("ServiceProvider");
		
		for (int i = 0; i < reArray.length(); i++){
			try{
				JSONObject jsonObject = reArray.getJSONObject(i);
				//stringArray.add(jsonObject.toString());
				objArray.add((DBObject) JSON.parse(jsonObject.toString()));
			}catch(JSONException e){
				e.printStackTrace();
			}
		}
		
//		for (int i = 0; i < serArray.length(); i++){
//			try{
//				JSONObject jsonObject1 = serArray.getJSONObject(i);
//				objArray1.add((DBObject) JSON.parse(jsonObject1.toString()));
//			}catch(JSONException e){
//				e.printStackTrace();
//			}
//		}
        DBObject updateData = new BasicDBObject();
        updateData.put("$set", new BasicDBObject("Resource", objArray).append("ServiceProvider", serviceProvider));     
        
        BasicDBObject query = new BasicDBObject();
        List<BasicDBObject> queryObj = new ArrayList<BasicDBObject>();
        queryObj.add(new BasicDBObject("Manufacturer", manufacturer));
        queryObj.add(new BasicDBObject("Model", model));
        queryObj.add(new BasicDBObject("SN", sn));
        query.put("$and", queryObj);
        result = collection.update(query, updateData);
        return result.getN();
	}
	
	public static String search(String manufacturer, String model, String sn, String queryItem){
		String result = "";
		DB db = client.getDB("lwm2m");
		DBCollection collection = db.getCollection("CLIENTS");
       
		BasicDBObject query = new BasicDBObject();
        List<BasicDBObject> obj = new ArrayList<BasicDBObject>();
        obj.add(new BasicDBObject("Manufacturer", manufacturer));
        obj.add(new BasicDBObject("Model", model));
        obj.add(new BasicDBObject("SN", sn));
        query.put("$and", obj);
        DBCursor cursor = collection.find(query);

        while (cursor.hasNext()){
        	BasicDBObject object = (BasicDBObject) cursor.next();
        	result = object.getString(queryItem);
        }
		return result;
	}
	
	public static JSONObject discoverDevice(JSONObject obj){
		JSONObject result = new JSONObject();
		JSONObject temp = new JSONObject();
		JsonParser parser = new JsonParser();
		DB db = client.getDB("lwm2m");
		try{
			DBCollection collection = db.getCollection("CLIENTS");
			BasicDBObject query = new BasicDBObject();
	        List<BasicDBObject> queryObj = new ArrayList<BasicDBObject>();
	        queryObj.add(new BasicDBObject("Manufacturer", obj.getString("Manufacturer")));
	        queryObj.add(new BasicDBObject("Model", obj.getString("Model")));
	        queryObj.add(new BasicDBObject("SN", obj.getString("SN")));
	        query.put("$and", queryObj);
	        DBCursor cursor = collection.find(query);
	        
	        while(cursor.hasNext()){
				DBObject dbObj = cursor.next();
				JSONObject obj2 = new JSONObject(dbObj.toString());
				temp.put("Resource", obj2.getJSONArray("Resource"));
			}
			String resources = temp.getString("Resource");
			JsonArray jsonObj = (JsonArray) parser.parse(resources);
			for (int i = 0; i < jsonObj.size(); i++){
				String attrName = ((JsonObject)jsonObj.get(i)).get("Name").getAsString();
				result.put("Attributes "+i, attrName);
		
				//String attributes = ((JsonArray) ((JsonObject)jsonObj.get(i)).get("Attributes")).getAsJsonArray().getAsString();
//				JsonElement attributes = ((JsonObject)jsonObj.get(i)).get("Attributes");
				
//				JsonArray attributesArray = ((JsonObject)jsonObj.get(i)).get("Attributes").getAsJsonArray();

//				System.out.println(attributes.getAsJsonArray().get(0).getAsJsonObject().entrySet().toArray()[0].toString().split("=")[0]);
//				for (int j = 0; j < attributesArray.size(); j++ ){
//					String attributes = attributesArray.get(j).getAsJsonObject().entrySet().toArray()[0].toString().split("=")[0];
//					result.put("Resource"+i+" Attribute"+j, attributes);
//				}
			}
	        
		} catch (MongoException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static JSONObject readDevice(JSONObject obj){
		JSONObject temp = new JSONObject();
		JSONObject result = new JSONObject();
		JsonParser parser = new JsonParser();

		DB db = client.getDB("lwm2m");
		try{
			DBCollection collection = db.getCollection("CLIENTS");
			BasicDBObject query = new BasicDBObject();
	        List<BasicDBObject> queryObj = new ArrayList<BasicDBObject>();
	        queryObj.add(new BasicDBObject("Manufacturer", obj.getString("Manufacturer")));
	        queryObj.add(new BasicDBObject("Model", obj.getString("Model")));
	        queryObj.add(new BasicDBObject("SN", obj.getString("SN")));
	        query.put("$and", queryObj);
	        DBCursor cursor = collection.find(query);
	        while(cursor.hasNext()){
				DBObject dbObj = cursor.next();
				JSONObject obj2 = new JSONObject(dbObj.toString());
				temp.put("Resource", obj2.getJSONArray("Resource"));
	        }
	        String rscName = obj.getString("RscName");
			String resources = temp.getString("Resource");
			JsonArray jsonObj = (JsonArray) parser.parse(resources);
			int writeTime = 0;
			for (int i = 0; i < jsonObj.size(); i++){
				String resName = ((JsonObject)jsonObj.get(i)).get("Name").getAsString();
				if (rscName.equals(resName)){
//					JsonArray attributesArray = ((JsonObject)jsonObj.get(i)).get("Attributes").getAsJsonArray();
					String resValue = ((JsonObject)jsonObj.get(i)).get("Value").getAsString();
					result.put(rscName, resValue);
					writeTime++;
//					for (int j = 0; j < attributesArray.size(); j++ ){
//						String attributes = attributesArray.get(j).getAsJsonObject().entrySet().toArray()[0].toString().split("=")[0];
//						String attrValue = attributesArray.get(j).getAsJsonObject().entrySet().toArray()[0].toString().split("=")[1];
////						String attrValue = attributesArray.get(j).getAsJsonObject().get(attributes).toString();
//						result.put(rscName+" Attribute"+j, attributes + " = " + attrValue);
//					}
				}

			}
			
			if (writeTime == 0)
				result.put("Result", "Not Found");
			
		}catch (MongoException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static JSONObject write(JSONObject obj){
		JSONObject result = new JSONObject();
		WriteResult status = null;

		DB db = client.getDB("lwm2m");
		try{
			DBCollection collection = db.getCollection("CLIENTS");
			BasicDBObject query = new BasicDBObject();
	        List<BasicDBObject> queryObj = new ArrayList<BasicDBObject>();
	        queryObj.add(new BasicDBObject("Manufacturer", obj.getString("Manufacturer")));
	        queryObj.add(new BasicDBObject("Model", obj.getString("Model")));
	        queryObj.add(new BasicDBObject("SN", obj.getString("SN")));
	        queryObj.add(new BasicDBObject("Resource.Name", obj.getString("RscName")));
	        query.put("$and", queryObj);
			BasicDBObject updateData = (BasicDBObject) new BasicDBObject("$set", new BasicDBObject("Resource.$.Value", obj.get("Value")));
			status = collection.update(query, updateData);
			if (status.getN() == 1)
				result = readDevice(obj);
			else
				result.put("Result", "Failed");
		} catch (MongoException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static JSONObject execute(JSONObject obj){
		JSONObject result = new JSONObject();
		JSONObject input = new JSONObject();
		try{
			input.put("Manufacturer", obj.getString("Manufacturer"));
			input.put("Model", obj.getString("Model"));
			input.put("SN", obj.getString("SN"));			
			input.put("RscName", obj.getString("RscName"));
			input.put("Value", obj.getString("Operation"));
			result = write(input);
	        
		} catch (MongoException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}		
		return result;
	}
	
	public static JSONObject searchObserve(JSONObject obj){
		JSONObject result = new JSONObject();
		JSONObject temp = new JSONObject();
		JsonParser parser = new JsonParser();

		try{
			DB db = client.getDB("lwm2m");
			DBCollection collection = db.getCollection("CLIENTS");
			BasicDBObject query = new BasicDBObject();
	        List<BasicDBObject> queryObj = new ArrayList<BasicDBObject>();
	        queryObj.add(new BasicDBObject("Manufacturer", obj.getString("Manufacturer")));
	        queryObj.add(new BasicDBObject("Model", obj.getString("Model")));
	        queryObj.add(new BasicDBObject("SN", obj.getString("SN")));
	        queryObj.add(new BasicDBObject("Resource.Name", obj.getString("RscName")));
	        query.put("$and", queryObj);
	        DBCursor cursor = collection.find(query);
	        while(cursor.hasNext()){
				DBObject dbObj = cursor.next();
				JSONObject obj2 = new JSONObject(dbObj.toString());
				temp.put("Resource", obj2.getJSONArray("Resource"));
	        }
	        
	        String rscName = obj.getString("RscName");
			String resources = temp.getString("Resource");
			JsonArray jsonObj = (JsonArray) parser.parse(resources);
			for (int i = 0; i < jsonObj.size(); i++){
				String resName = ((JsonObject)jsonObj.get(i)).get("Name").getAsString();
				if (rscName.equals(resName)){
					result.put("Observed", ((JsonObject)jsonObj.get(i)).get("Observed").getAsString());
					result.put("GreaterThan", ((JsonObject)jsonObj.get(i)).get("GreaterThan").getAsString());
					result.put("LessThan", ((JsonObject)jsonObj.get(i)).get("LessThan").getAsString());
					result.put("Step", ((JsonObject)jsonObj.get(i)).get("Step").getAsString());
					result.put("Value", ((JsonObject)jsonObj.get(i)).get("Value").getAsString());

				}

			}
		} catch (MongoException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static JSONObject insertObserve(JSONObject obj){
		JSONObject result = new JSONObject();
		WriteResult status = null;
		BasicDBObject updateData = new BasicDBObject();
		try{
			DB db = client.getDB("lwm2m");
			DBCollection collection = db.getCollection("CLIENTS");
			BasicDBObject query = new BasicDBObject();
	        List<BasicDBObject> queryObj = new ArrayList<BasicDBObject>();
	        queryObj.add(new BasicDBObject("Manufacturer", obj.getString("Manufacturer")));
	        queryObj.add(new BasicDBObject("Model", obj.getString("Model")));
	        queryObj.add(new BasicDBObject("SN", obj.getString("SN")));
	        queryObj.add(new BasicDBObject("Resource.Name", obj.getString("RscName")));
	        query.put("$and", queryObj);
        	updateData = (BasicDBObject) new BasicDBObject("$set", new BasicDBObject("Resource.$.Observed", "Y"));
			status = collection.update(query, updateData);

	        if(obj.getString("When").equals("=")){
	        	updateData = (BasicDBObject) new BasicDBObject("$set", new BasicDBObject("Resource.$.Step", "1"));
	        }
	        else if (obj.getString("When").equals(">")){
	        	updateData = (BasicDBObject) new BasicDBObject("$set", new BasicDBObject("Resource.$.GreaterThan", obj.get("Value")));
	        }
	        else{
	        	updateData = (BasicDBObject) new BasicDBObject("$set", new BasicDBObject("Resource.$.LessThan", obj.get("Value")));
	        }
			status = collection.update(query, updateData);
			if (status.getN() == 1)
				result.put("Observation", "Started");
			else
				result.put("Observation", "Failed");
		} catch (MongoException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static JSONObject cancelObserve(JSONObject obj){
		JSONObject result = new JSONObject();
		WriteResult status = null;
		BasicDBObject updateData = new BasicDBObject();
		try{
			DB db = client.getDB("lwm2m");
			DBCollection collection = db.getCollection("CLIENTS");
			BasicDBObject query = new BasicDBObject();
	        List<BasicDBObject> queryObj = new ArrayList<BasicDBObject>();
	        queryObj.add(new BasicDBObject("Manufacturer", obj.getString("Manufacturer")));
	        queryObj.add(new BasicDBObject("Model", obj.getString("Model")));
	        queryObj.add(new BasicDBObject("SN", obj.getString("SN")));
	        queryObj.add(new BasicDBObject("Resource.Name", obj.getString("RscName")));
	        query.put("$and", queryObj);
        	updateData = (BasicDBObject) new BasicDBObject("$set", new BasicDBObject("Resource.$.Observed", "N"));
			status = collection.update(query, updateData);

	        if(status.getN() == 1){
	        	updateData = (BasicDBObject) new BasicDBObject("$set", new BasicDBObject("Resource.$.Step", "0"));
				status = collection.update(query, updateData);
				if (status.getN() == 1){
					updateData = (BasicDBObject) new BasicDBObject("$set", new BasicDBObject("Resource.$.GreaterThan", ""));
					status = collection.update(query, updateData);
					if (status.getN() == 1){
						updateData = (BasicDBObject) new BasicDBObject("$set", new BasicDBObject("Resource.$.LessThan", ""));
						status = collection.update(query, updateData);
					}
				}
	        }
			if (status.getN() == 1)
				result.put("Cancel Observation", "Success");
			else
				result.put("Cancel Observation", "Failed");
		} catch (MongoException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
//	public static DBObject getDevice(BasicDBObject dbj){
//		DB db = client.getDB("lwm2m");
//		DBCollection clnt = db.getCollection("CLIENTS");
//		//BasicDBObject dbj =  (BasicDBObject) JSON.parse(obj.toString());
//		DBCursor rst = clnt.find();
//		return rst.next();
//	}
//	
//	public static void updateResource(String src,String val){
//		DB db = client.getDB("lwm2m");
//		DBCollection clnt = db.getCollection("Device");
//		BasicDBObject query = (BasicDBObject) new BasicDBObject("Resource.Name", src);
//		BasicDBObject update = (BasicDBObject) new BasicDBObject("$set", new BasicDBObject("Resource.$.Value", val));
//		clnt.update(query, update);
//	}
//	
//	public static void startObserve(String src,String url){
//		DB db = client.getDB("Client");
//		DBCollection clnt = db.getCollection("Device");
//		BasicDBObject query = (BasicDBObject) new BasicDBObject("Resource.Name", src);
//		BasicDBObject update = (BasicDBObject) new BasicDBObject("$set", new BasicDBObject("Resource.$.Observed", "Y"));
//		clnt.update(query, update);
//		BasicDBObject update2 = (BasicDBObject) new BasicDBObject("$set", new BasicDBObject("Resource.$.Observer", url));
//		clnt.update(query, update2);
//	}
//	
//	public static void stopObserve(String src){
//		DB db = client.getDB("Client");
//		DBCollection clnt = db.getCollection("Device");
//		BasicDBObject query = (BasicDBObject) new BasicDBObject("Resource.Name", src);
//		BasicDBObject update = (BasicDBObject) new BasicDBObject("$set", new BasicDBObject("Resource.$.Observed", "N"));
//		clnt.update(query, update);
//		BasicDBObject update2 = (BasicDBObject) new BasicDBObject("$set", new BasicDBObject("Resource.$.Observer", ""));
//		clnt.update(query, update2);
//	}
//	
//	public static void IceOn(){
//		updateResource("IceMaker","On");
//	}
//	public static void IceOff(){
//		updateResource("IceMaker","Off");
//	}
//	
//	public static void createResource(BasicDBObject newEntry){
//		DB db = client.getDB("Client");
//		DBCollection clnt = db.getCollection("Device");
//		BasicDBObject query = (BasicDBObject) new BasicDBObject("SN", "cmpe273");
//		BasicDBObject update = (BasicDBObject) new BasicDBObject("$addToSet", new BasicDBObject("Resource",newEntry));
//		clnt.update(query, update);
//	}
//	
//	public static void deleteResource(String Name){
//		DB db = client.getDB("Client");
//		DBCollection clnt = db.getCollection("Device");
//		BasicDBObject query = (BasicDBObject) new BasicDBObject("SN", "cmpe273");
//		BasicDBObject update = (BasicDBObject) new BasicDBObject("$pull", new BasicDBObject("Resource", new BasicDBObject("Name",Name)));
//		clnt.update(query, update);
//	}
//	
//	public static String setDevice(BasicDBObject target,BasicDBObject newval){
//		DB db = client.getDB("Client");
//		DBCollection clnt = db.getCollection("Device");
//		//BasicDBObject dbj =  (BasicDBObject) JSON.parse(obj.toString());
//		BasicDBObject update = new BasicDBObject();
//		update.put("$set", newval);
//		
//		WriteResult rst= clnt.update(target,update);
//		if(rst.getN()==1)
//			return "Update Done";
//		else
//			return "Update aborted";
//	}
	
	public static void main(String[] args) throws JSONException{
		Connect();
		JSONObject data = null;
//		data = new JSONObject().put("Manufacturer", "DAIKIN").put("Model", "AC").put("SN", "CMPE273AC001");
//		System.out.println(discoverDevice(data));
//		data = new JSONObject().put("Manufacturer", "DAIKIN").put("Model", "AC").put("SN", "CMPE273AC001").put("RscName", "Freezer");
//		System.out.println(readDevice(data));
//		data = new JSONObject().put("Manufacturer", "DAIKIN").put("Model", "AC").put("SN", "CMPE273AC001").put("RscName", "Freezer").put("Operation", "On");
//		System.out.println(write(data));
		data = new JSONObject().put("Manufacturer", "DAIKIN").put("Model", "AC").put("SN", "CMPE273AC001").put("RscName", "Thermometer").put("When", ">").put("Value", "5");
		System.out.println(insertObserve(data));

//		BasicDBObject myself = (BasicDBObject) new BasicDBObject().put("SN", "cmpe273");
//		System.out.println(getDevice(myself).toString());
//		updateResource("Thermometer","4");
//		System.out.println(getDevice(myself).toString());
//		
//		JSONObject jbj = new JSONObject().put("Resource", new JSONObject().put("Name","Light").put("Value", "On"));
//		BasicDBObject nrsc = (BasicDBObject) JSON.parse(jbj.toString());
//		//createResource(nrsc);
//		System.out.println(getDevice(myself).toString());
//		deleteResource("Light");
//		System.out.println(getDevice(myself).toString());
		DisConnect();
	}
/*
	public static void InsertSubscriber(JSONObject obj) throws JSONException{
		DB db = client.getDB("Client");
		try{
			String maker = (String)obj.get("Manufacturer");
			DBCollection clnt=db.getCollection(maker);
			BasicDBObject dbj = (BasicDBObject)JSON.parse(obj.toString());
			
			dbj.append("StartTime", new Date().toString()).append("EndTime", "");
			clnt.insert(dbj);
		}catch(JSONException ex){
			System.out.println("unknown manufacturer");
		}
	}
	
	public static boolean FindDevice(String manufacturer,String model){
		DB db=client.getDB("RegServer1");
		DBCollection clnt = db.getCollection("inventory");
		DBCursor rst = clnt.find(new BasicDBObject().append("Manufacturer", manufacturer).append("Model",model));
		if(rst.count()==1){
			return true;
		}else{
			return false;
		}
	}
	
	public static boolean FindSubscriber(JSONObject device) throws JSONException{
		DB db=client.getDB("RegServer1");
		String collection=(String) device.get("Manufacturer");
		
		DBCollection clnt= db.getCollection(collection);
		try{
			DBCursor rst= clnt.find(new BasicDBObject()
										.append("Manufacturer", device.get("Manufacturer"))
										.append("Model",device.get("Model"))
										.append("SN",device.get("SN")));
			if(rst.count()==1) {
				return true;
			}else{
				return false;
			}
		}catch(JSONException ex){
			return false;
		}
	}
	
	public static String UpdateSubscriber(JSONObject device) throws JSONException{
		DB db=client.getDB("RegServer1");
		String collection=(String) device.get("Manufacturer");
		
		DBCollection clnt= db.getCollection(collection);
		try{
			BasicDBObject query = new BasicDBObject()
										.append("Manufacturer", device.get("Manufacturer"))
										.append("Model",device.get("Model"))
										.append("SN",device.get("SN"));
	        BasicDBObject update = new BasicDBObject();
	        update.put("$set", new BasicDBObject("Resources",(BasicDBObject)JSON.parse(device.get("Resources").toString())));
			
	        WriteResult rst= clnt.update(query,update);
	        if(rst.getN()==1)
	        	return "Update Done";
	        else
	        	return "Update aborted";
		}catch(JSONException ex){
			return ex.toString();
		}
	}
	
	public static String DeregisterSubscriber(JSONObject device) throws JSONException{
		DB db=client.getDB("RegServer1");
		String collection=(String) device.get("Manufacturer");
		
		DBCollection clnt= db.getCollection(collection);
		try{
			BasicDBObject query = new BasicDBObject()
										.append("Manufacturer", device.get("Manufacturer"))
										.append("Model",device.get("Model"))
										.append("SN",device.get("SN"));
	        BasicDBObject update = new BasicDBObject();
	        update.put("$set", new BasicDBObject("EndTime",new Date().toString()));
			
	        WriteResult rst= clnt.update(query,update);
	        if(rst.getN()==1)
	        	return "De-register Done";
	        else
	        	return "De-register failed";
		}catch(JSONException ex){
			return ex.toString();
		}
	}
	*/
	public static void DisConnect(){
		client.close();
	}
}
