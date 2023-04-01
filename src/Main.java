import com.google.gson.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import org.json.simple.parser.ParseException;

public class Main {
    private final static int port = 4444;
    private static ServerSocket server;
    private static Socket socket;
    static JSONParser parser = null;
    static Object clientObject = null;
    static JSONObject clientRequest = null;
    static String filepath = "/Users/hello/Desktop/JAVA22/AlphaServer/src/data.json";
    static Gson gson = null;

    public static void main(String[] args) {

        System.out.println("Welcome to Server!");

        while (true) {
            try {

                server = new ServerSocket(port);
                System.out.println("Server started");
                System.out.println("Waiting for a client ...");

                socket = server.accept();
                System.out.println("Client connected");

                // Handle client communication, read and write messages
                responseClient(inputClient());

                // Close the client socket when the client disconnects
                socket.close();
                System.out.println("Client disconnected");

            } catch (Exception e) {
                System.out.println("Problem with connect");
            } finally {
                // Close the server socket and restart the loop for a new connection
                if (server != null) {
                    try {
                        server.close();
                    } catch (IOException e) {
                        System.out.println("Error closing the server socket");
                    }
                }
            }
        }
    }

    static String inputClient() {
        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = reader.readLine();
            System.out.println("Client line: " + line);

            try {

                parser = new JSONParser();
                clientObject = parser.parse(line);
                clientRequest = (JSONObject) clientObject;

                String httpMethod = clientRequest.get("HTTPMethod").toString();
                String contentType = clientRequest.get("ContentType").toString();
                String urlParameter = clientRequest.get("URLParametrar").toString();

                JSONObject body = null;
                JSONObject motorcycle = null;
                JSONObject motorcycleGroup = null;

                System.out.println("method: " + httpMethod);
                System.out.println("content: " + contentType);
                System.out.println("url: " + urlParameter);

                String message = "";

                switch (urlParameter) {

                    case "/sport":

                        if (httpMethod.equals("get")) {

                            message = getJsonObjectFromFile("sport");

                            System.out.println("message: " + message);

                        } else {

                            // working well
                            body = (JSONObject) clientRequest.get("Body");
                            motorcycle = (JSONObject) body.get("motorcycle");
                            motorcycleGroup = (JSONObject) motorcycle.get("sport");
                            addNewObjectInJsonFile(motorcycleGroup, "sport");

                            message = "succefully";

                        }
                        break;

                    case "/classic":

                        if (httpMethod.equals("get")) {

                            message = getJsonObjectFromFile("classic");

                            System.out.println("message: " + message);

                        } else {

                            body = (JSONObject) clientRequest.get("Body");
                            motorcycle = (JSONObject) body.get("motorcycle");
                            motorcycleGroup = (JSONObject) motorcycle.get("classic");
                            addNewObjectInJsonFile(motorcycleGroup, "classic");

                            message = "succefully";
                        }

                        break;
                }

                return message;

            } catch (Exception e) {
                System.out.println(e);
            }
        } catch (Exception e) {
            System.out.println("Client's input invalid");
        }
        return "Error - couldn't read input";
    }

    static void responseClient(String message){
        try {

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            JSONObject object = new JSONObject();

            if (message.equals("failure")){
                object.put("httpStatusCode", "404");
                object.put("Body", message);
            } else if (message.equals("succefully")) {
                object.put("httpStatusCode", "202");
                object.put("Body", message);
            } else {
                object.put("httpStatusCode", "200");
                object.put("Body", message);
            }

            writer.write(object.toJSONString());
            writer.newLine();
            writer.flush();

        } catch (Exception e) {
            System.out.println("JSON: " + e.getMessage());
        }
    }

    static String getJsonObjectFromFile(String motorcycleType) {
        try {

            // Get JSON objects from the file
            parser = new JSONParser();
            clientObject = parser.parse(new FileReader(filepath));
            gson = new Gson();
            JsonObject jsonObject = gson.fromJson(clientObject.toString(), JsonObject.class);

            // Set the "motorcycleType" object from the JSON file to Array
            JsonObject motorcycleObject = jsonObject.getAsJsonObject("motorcycle");
            JsonArray motorcycleClassArray = motorcycleObject.getAsJsonArray(motorcycleType);

            System.out.println("Motorcycles from the JSON file: " + motorcycleClassArray.toString());

            return motorcycleClassArray.toString();

        } catch(Exception e) {
            System.out.println("openJsonFile: " + e.getMessage());
        }

        return "failure";

    }

    static String addNewObjectInJsonFile(JSONObject motorcycleGroup, String motorcycleClass) {
        try {

            // Get JSON objects from the file
            parser = new JSONParser();
            clientObject = parser.parse(new FileReader(filepath));
            gson = new Gson();
            JsonObject jsonObject = gson.fromJson(clientObject.toString(), JsonObject.class);

            // Get the "motorcycleClass" object from jsonObject
            JsonObject motorcycleObject = jsonObject.getAsJsonObject("motorcycle");
            JsonArray motorcycleClassArray = motorcycleObject.getAsJsonArray(motorcycleClass);

            System.out.println("motorcycleClassArray from the jsonfile: " + motorcycleClassArray.toString());

            // convert the motorcycleGroup object to a JsonObject
            JsonObject motorcycleJson = gson.fromJson(motorcycleGroup.toJSONString(), JsonObject.class);

            // add the JSON string to the sportMotorcycleArray
            motorcycleClassArray.add(motorcycleJson);

            // add the sportMotorcycleArray to the motorcycleObject
            motorcycleObject.add(motorcycleClass, motorcycleClassArray);

            // convert the motorcycleObject to a JSON string and print it
            System.out.println("jsonfile + line from client: " + gson.toJson(motorcycleObject));

            // Add the data in motorcycle property
            JsonObject jsonFile = new JsonObject();
            jsonFile.add("motorcycle", motorcycleObject);

            System.out.println("Adding this to the JSON file: " + jsonFile.toString());

            // Write the updated JSONObject back to the JSON file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
                writer.write(jsonFile.toString());
            } catch (IOException e) {
                e.getMessage();
            }

        } catch (IOException | ParseException e) {
            e.getMessage();
        }
        return "successfully";
    }
}