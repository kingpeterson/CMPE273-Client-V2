package webResource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import data.ClientMongoDB;


@Path("/HandleRequest")
public class HandleRequest {
	@POST
	@Path("Read")
//	@Produces(MediaType.APPLICATION_JSON)
	public Response Read(String input){
		String result = "";
		try{
			JSONObject obj = new JSONObject(input);
			String objectID = obj.getString("ObjectID");
			result = JSON.serialize(ClientMongoDB.read(objectID));
		}catch (Exception e){
			e.printStackTrace();
		}
		return Response.status(201).entity(result).build();
	}
	
	@POST
	@Path("Discover")
	public Response Discover(String input){
		String result = "";
		try{
			JSONObject obj = new JSONObject(input);
			String objectID = obj.getString("ObjectID");
			String instance = obj.getString("Instance");
			String resourceID = obj.getString("Resource");
			DBObject rsc = ClientMongoDB.read(objectID);
			BasicDBList lst = (BasicDBList) rsc.get("Resource");

		}catch (Exception e){
			e.printStackTrace();
		}
		return Response.status(201).entity(result).build();
	}
	
	@POST
	@Path("Write")
	public Response Write(String input){
		String result = "failed";
		boolean observed = false;
		try{
			JSONObject obj = new JSONObject(input);
			String objectID = obj.getString("ObjectID");
			String instance = obj.getString("Instance");
			String field = obj.getString("Field");
			String value = obj.getString("Value");
			int status = ClientMongoDB.write(objectID, instance, field, value);
			if (status == 1)
				result= "success";
		}catch (Exception e){
			e.printStackTrace();
		}
		
		//action for the device being observed on certain parameter
		if (observed){
			
		}
		
		return Response.status(201).entity(result).build();
	}
	
	@POST
	@Path("Create")
	public Response Create(String input){
		String result = "";
		try{
			JSONObject obj = new JSONObject(input);
			String objectID = obj.getString("ObjectID");
			String newInstance = obj.getString("NewInstance");
			int status = ClientMongoDB.update(objectID, newInstance, 1);
			String status1 = Lightbulb.update(objectID, newInstance, 1);
//			if (status == 1 || status1.equals("1"))
			result = "success";
//			else
//				result = "failed";
		}catch (Exception e){
			e.printStackTrace();
		}
		return Response.status(201).entity(result).build();

	}
	
	@POST
	@Path("Delete")
	public Response Delete(String input){
		String result = "";
		try{
			JSONObject obj = new JSONObject(input);
			String objectID = obj.getString("ObjectID");
			String newInstance = obj.getString("NewInstance");
			int status = ClientMongoDB.update(objectID, newInstance, 0);
			String status1 = Lightbulb.update(objectID, newInstance, 0);
			if (status == 1 && status1.equals("1"))
				result = "success";
			else
				result = "failed";
		}catch (Exception e){
			e.printStackTrace();
		}
		return Response.status(201).entity(result).build();

	}
	
	@POST
	@Path("Execute")
	public Response Execute(String input){
		String result = "";
		try{
			JSONObject obj = new JSONObject(input);
			String objectID = obj.getString("ObjectID");
			String action = obj.getString("Action");
			String behavior = obj.getString("Behavior");
			int status = ClientMongoDB.action(objectID, action, behavior);
			if (status == 1)
				result = "success";
			else
				result = "failed";

		}catch (Exception e){
			e.printStackTrace();
		}
		return Response.status(201).entity(result).build();

	}
}
