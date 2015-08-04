package webResource;

import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.BasicDBList;
import com.mongodb.util.JSON;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import data.DeviceDAO;

@Path("/ServerOnDevice")
public class ServerOnDevice {
	@Path("/get")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String get(){
		try {
			if(Device.resource == null)
				Device.resource = new JSONObject().put("Themometer", "4 Celcius");
			if(Device.observation == null)
				Device.observation = new JSONObject();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Device.getName()+"\n"+Device.getResource().toString()+"\n"+Device.getObservation().toString();
	}
	
	@Path("/discover")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String discover(JSONObject input){
		String result = "";
		DeviceDAO.Connect();
		result = DeviceDAO.discoverDevice(input).toString();
		return result;
	}
	
	@Path("/read")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String read(JSONObject input){
		String result = "";
		DeviceDAO.Connect();
		result = DeviceDAO.readDevice(input).toString();
		return result;
	}
	
	@Path("/execute")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String execute(JSONObject input){
		String result = "";
		DeviceDAO.Connect();
		result = DeviceDAO.execute(input).toString();
		return result;
	}
	
//	@Path("/execute/{resource}/{operation}")
//	@GET
//	@Produces(MediaType.TEXT_PLAIN)
//	public String execute(@PathParam("resource") String rsc, @PathParam("operation") String op){
//		DeviceDAO.Connect();
//		if("IceMaker".equals(rsc)){
//			if("On".equals(op)){
//				DeviceDAO.IceOn();
//				return "Success On";
//			}
//			else if("Off".equals(op)){
//				DeviceDAO.IceOff();
//				return "Success Off";
//			}
//			return "Operation not suported";
//		}
//		return "execution fail: operation not supported on the device";
//	}
	
	@Path("/write")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String write(JSONObject input){
		String managerUrl = "http://localhost:8080/CMPE273/webResource/ResourceManage/";

		String result = "";
		JSONObject data = new JSONObject();
		DeviceDAO.Connect();
		JSONObject readResult = DeviceDAO.searchObserve(input);
		
		DeviceDAO.Connect();
		result = DeviceDAO.write(input).toString();

		try {
			if (readResult.getString("Observed").equals("Y")){
				if (!readResult.getString("GreaterThan").equals("")){
					if (Integer.parseInt(input.getString("Value")) > Integer.parseInt(readResult.getString("Value"))){
						data = new JSONObject().put("Resource", input.getString("RscName")).put("Compare", ">").put("Value", input.getString("Value"));
					}
				}
				else if (!readResult.getString("LessThan").equals("")){
					if (Integer.parseInt(input.getString("Value")) < Integer.parseInt(readResult.getString("Value"))){
						data = new JSONObject().put("Resource", input.getString("RscName")).put("Compare", "<").put("Value", input.getString("Value"));
					}
				}
				else if (readResult.getString("Step").equals("1")){
					if (!input.getString("Value").equals(readResult.getString("Value"))){
						data = new JSONObject().put("Resource", input.getString("RscName")).put("Compare", "!=");
					}
				}
				Client client = Client.create();
				WebResource webResource = client.resource(managerUrl+"notification");
				String response = webResource.type(MediaType.APPLICATION_JSON).post(String.class, data);
				System.out.println(response);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
			
		return result;
	}
	
//	@Path("/write/{rscname}")
//	@POST
//	@Consumes(MediaType.TEXT_PLAIN)
//	public String write(@PathParam("rscname") String rscname,String value){
//		DeviceDAO.Connect();
//		DeviceDAO.updateResource(rscname, value);
//		boolean observed = false;
//		String observer = "";
//		
//		DBObject rsc = DeviceDAO.getDevice((BasicDBObject) new BasicDBObject().put("SN", "cmpe273"));
//		BasicDBList lst = (BasicDBList) rsc.get("Resource");
//		BasicDBObject[] rscarr = lst.toArray(new BasicDBObject[0]);
//		for(BasicDBObject dbObj : rscarr) {
//			    // shows each item from the lights array
//			    if(((String)dbObj.get("Name")).equals(rscname)){
//			    	observed = ((String) dbObj.get("Observed")).equals("Y");
//			    	observer = (String) dbObj.get("Observer");
//			    }
//		}
//		if(observed){
//			try {
//				Client client = Client.create();
//				WebResource r = client.resource(observer);
//				ClientResponse response = r.type("application/json")
//						.post(ClientResponse.class, new JSONObject().put("Resource",rscname).put("Value", value));
//				if (response.getStatus() > 202) {
//				   throw new RuntimeException("Failed : HTTP error code : "
//					+ response.getStatus());
//				}
//				String result = response.getEntity(String.class);
//				if("stop".equals(result))
//					DeviceDAO.stopObserve(rscname);
//				
//			  } catch (Exception e) {
//				e.printStackTrace();
//			  }
//			//if responses cancel, stop observation
//			return "success (reported to server)";
//		}
//		return "success";
//	}

	@Path("/observe")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String observe(JSONObject input){
//		System.out.println(input);
		String result = "";
		DeviceDAO.Connect();
		result = DeviceDAO.insertObserve(input).toString();
		return result;
	}
	
	@Path("/cancelObserve")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String cancelObserve(JSONObject input){
//		System.out.println(input);
		String result = "";
		DeviceDAO.Connect();
		result = DeviceDAO.cancelObserve(input).toString();
		return result;
	}
	
//	@Path("/create")
//	@POST
//	@Consumes(MediaType.APPLICATION_JSON)
//	public String create(String objString){
//		DeviceDAO.Connect();
//		//JSONObject obj = new JSONObject(objString);
//		BasicDBObject dbj = (BasicDBObject) JSON.parse(objString);
//		DeviceDAO.createResource(dbj);
//		return "success";
//	}
	
//	@Path("/delete")
//	@POST
//	@Consumes(MediaType.TEXT_PLAIN)
//	public String delete(String Name){
//		DeviceDAO.Connect();
//		DeviceDAO.deleteResource(Name);
//		return "success";
//	}

	
//	@Path("/set")
//	@POST
//	@Consumes(MediaType.APPLICATION_JSON)
//	public Response setName(String objString){
//		//{"Resource":<Name>,"Value":<val>}
//		JSONObject obj;
//		try {
//			obj = new JSONObject(objString);
//			String key = (String) obj.get("Resource");
//			Object val = obj.get("Value");
//			
//			return Response.status(201).entity(Device.setResource(key, val)).build();
//		} catch (JSONException e) {
//			e.printStackTrace();
//			return Response.status(202).entity("rename failed").build();
//		}
//	}
	
//	@Path("/observe")
//	@POST
//	@Consumes(MediaType.APPLICATION_JSON)
//	public Response Observe(String objString){
//		//{"Resource":<Name>,"Url":<Url>}
//		JSONObject obj;
//		try {
//			obj = new JSONObject(objString);
//			String rsc = (String) obj.get("Resource");
//			String url = (String) obj.get("Url");
//			DeviceDAO.startObserve(rsc, url);
//			return Response.status(201).entity("observation added").build();
//		} catch (JSONException e) {
//			e.printStackTrace();
//			return Response.status(202).entity("observation failed").build();
//		}
//	}
	
//	@Path("/writeAttr")
//	@POST
//	@Consumes(MediaType.APPLICATION_JSON)
//	public Response WriteAttr(String objString){
//		//{"Resource":<Name>,"Cancel":"True/False"}
//		JSONObject obj;
//		try {
//			obj = new JSONObject(objString);
//			String rsc = (String) obj.get("Resource");
//			String cancel = (String) obj.get("Cancel");
//			if("True".equals(cancel))
//				DeviceDAO.stopObserve(rsc);
//			return Response.status(201).entity("attribute written").build();
//		} catch (JSONException e) {
//			e.printStackTrace();
//			return Response.status(202).entity("write attribute fail").build();
//		}
//	}
//	public static void main(String[] args) throws JSONException{
//		JSONObject data = new JSONObject();
//		data = new JSONObject().put("Manufacturer", "DAIKIN").put("Model", "AC").put("SN", "CMPE273AC001").put("RscName", "Thermometer").put("When", ">").put("Value", "5");
//		observe(data);
//		data = new JSONObject().put("Manufacturer", "DAIKIN").put("Model", "AC").put("SN", "CMPE273AC001").put("RscName", "Thermometer").put("Value", "4");
//		write(data);
//	}
}
