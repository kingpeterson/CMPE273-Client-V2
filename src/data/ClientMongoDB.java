package data;


import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.BasicBSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

public class ClientMongoDB {

	private static DB db = null;
    private static MongoClient mongo = null;
    
//    public static void init() {
//        try {
//            mongo = new MongoClient("localhost", 27017);
//        } catch (final UnknownHostException e) {
//        	e.printStackTrace();
//        }
//        db = mongo.getDB("cmpe273Client");
//    }
    
    public static void init(){        
        try {
            final ServerAddress serverAddress = new ServerAddress("ds045031.mongolab.com", 45031);

            final MongoCredential credential = MongoCredential.createMongoCRCredential("cmpe273team1", "lwm2m", "cmpe273".toCharArray());

            mongo = new MongoClient(serverAddress, Arrays.asList(credential));

            db = mongo.getDB("lwm2m");
        } catch (final UnknownHostException e) {
        	e.printStackTrace();
        }
	}
    
    public static void close(){
    	mongo.close();
    }
    
    public static void insert(String manufacturer, String serialNumber, String objectID, int lightWatts, String lightColor, String registerURI, String updateURI){
    	String data = "{\"Manufacturer\": \""+manufacturer+"\", \"SerialNumber\": \""+serialNumber+"\", \"ObjectID\": \""+objectID+"\", "
    			+ "\"lightWatts\": \""+lightWatts+"\", \"lightColor\": \""+lightColor+"\", "
    			+ "\"registerURI\": \""+registerURI+"\", \"updateURI\": \""+updateURI+"\"}";
    	if(db == null){
    		init();
    	}
        DBCollection collection = db.getCollection("deviceStorage");
    	DBObject object = (DBObject)JSON.parse(data);
    	collection.insert(object);
    }
    
    public static String search(String item){
    	String found = "";
    	if (db == null)
    		init();
    	
    	DBCollection collection = db.getCollection("deviceStorage");
    	DBObject object = collection.findOne();
    	found = ((BasicBSONObject) object).getString(item);
    	
    	return found;
    }
    
    public static void initialize(){
    	if (db == null)
    		init();
    	
        DBCollection collection = db.getCollection("deviceStorage");
        DBObject object = collection.find().one();
        collection.remove(object);
    }
    
    public static int update(String objectID, String newInstance, int status){
    	if (db == null)
    		init();
    	WriteResult result = null;
		try{
	        DBCollection collection = db.getCollection("deviceStorage");
			if (status == 1){
				String data = "{\"ObjectID\": \""+objectID+"\", \"SerialNumber\": \""+newInstance+"\"}";
		    	DBObject object = (DBObject)JSON.parse(data);
		    	result = collection.insert(object);
			}
			else{
		        BasicDBObject query = new BasicDBObject();
		        List<BasicDBObject> obj = new ArrayList<BasicDBObject>();
		        obj.add(new BasicDBObject("ObjectID", objectID));
		        obj.add(new BasicDBObject("SerialNumber", newInstance));
		        query.put("$and", obj);
		        result = collection.remove(query);
			}
//	        DBObject updateData = new BasicDBObject();
//	        updateData.put("$set", new BasicDBObject(newInstance, newValue));
//	     
//	        BasicDBObject query = new BasicDBObject();
//	        List<BasicDBObject> obj = new ArrayList<BasicDBObject>();
//	        obj.add(new BasicDBObject("ObjectID", objectID));
//	        query.put("$and", obj);
//	    	result = collection.update(query, updateData);
		} catch (MongoException e) {
			e.printStackTrace();
		}
    	return result.getN();
    }
    
    public static int action(String objectID, String action, String behavior){
    	if (db == null)
    		init();
    	WriteResult result = null;
		try{
	        DBCollection collection = db.getCollection("deviceStorage");
	        DBObject updateData = new BasicDBObject();
	        updateData.put("$set", new BasicDBObject(action, behavior));
	     
	        BasicDBObject query = new BasicDBObject();
	        List<BasicDBObject> obj = new ArrayList<BasicDBObject>();
	        obj.add(new BasicDBObject("ObjectID", objectID));
	        query.put("$and", obj);
	    	result = collection.update(query, updateData);
		}catch (MongoException e) {
			e.printStackTrace();
		}
    	return result.getN();
    }
    
//    public static int delete(String objectID, String newInstance){
//    	if (db == null)
//    		init();
//    	WriteResult result = null;
//		try{
//	        DBCollection collection = db.getCollection("deviceStorage");
//	        DBObject updateData = new BasicDBObject();
//	        updateData.put("$unset", new BasicDBObject(newInstance, ""));
//	        DBObject query = new BasicDBObject("ObjectID", objectID);
//	    	result = collection.update(query, updateData);
//		} catch (MongoException e) {
//			e.printStackTrace();
//		}
//		return result.getN();
//    }
    
//	public static void main(String[] args){
//		System.out.println(delete("0000000000010001", "Temperature"));
//	}
}
