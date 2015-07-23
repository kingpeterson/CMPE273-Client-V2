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
			String newValue = obj.getString("NewValue");
			int status = ClientMongoDB.create(objectID, newInstance, newValue);
			String status1 = Lightbulb.update(objectID, newInstance, newValue);
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
	@Path("Delete")
	public Response Delete(String input){
		String result = "";
		try{
			JSONObject obj = new JSONObject(input);
			String objectID = obj.getString("ObjectID");
			String newInstance = obj.getString("NewInstance");
			int status = ClientMongoDB.delete(objectID, newInstance);
			String status1 = Lightbulb.update(objectID, newInstance, "-1");
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
			
		}catch (Exception e){
			e.printStackTrace();
		}
		return Response.status(201).entity(result).build();

	}
}
