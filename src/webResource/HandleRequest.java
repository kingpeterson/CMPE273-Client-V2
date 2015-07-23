package webResource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import data.ClientMongoDB;


@Path("/HandleRequest")
public class HandleRequest {
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
