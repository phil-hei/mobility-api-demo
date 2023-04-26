package de.fhdo.api_mobility_demo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.fhdo.api_mobility_demo.model.VehicleBreakdown;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;

@Controller
public class ExampleController {

    private static final String OPEN_SENSE_MAP = "https://api.opensensemap.org/boxes/6079ad3416aef4001b9a77db";
    private static final String TELRAM_API_TOKEN = "YOUR API KEY";
    private static final String TELRAM_API = "https://telraam-api.net/v1/reports/traffic_snapshot_live";

    // Get the current temperature for the sense box deployed at TU Dortmund ()
    @GetMapping("/sensebox")
    public String getSenseBox(Model model) {
        RestTemplate restTemplate = new RestTemplate();
        JsonNode apiResponse = restTemplate.getForObject(OPEN_SENSE_MAP, JsonNode.class);

        JsonNode sensorsNode = apiResponse.get("sensors");
        JsonNode firstSensorNode = sensorsNode.get(0);
        JsonNode valueNode = firstSensorNode.get("lastMeasurement").get("value");
        double temperature = valueNode.asDouble();

        JsonNode secondSensorNode = sensorsNode.get(1);
        JsonNode humidityValueNode = secondSensorNode.get("lastMeasurement").get("value");
        double humidity = humidityValueNode.asDouble();

        JsonNode thirdSensorNode = sensorsNode.get(2);
        JsonNode lightValueNode = thirdSensorNode.get("lastMeasurement").get("value");
        double light = lightValueNode.asDouble();

        JsonNode fourthSensorNode = sensorsNode.get(3);
        JsonNode pm25ValueNode = fourthSensorNode.get("lastMeasurement").get("value");
        double pm25 = pm25ValueNode.asDouble();

        JsonNode fithSensorNode = sensorsNode.get(1);
        JsonNode pm10ValueNode = fithSensorNode.get("lastMeasurement").get("value");
        double pm10 = pm10ValueNode.asDouble();

        // Ausgabe der Temperatur
        System.out.println("Aktuelle Temperatur für den Standort TU Dortmund: " + temperature);
        model.addAttribute("temperature", temperature);
        model.addAttribute("humidity", humidity);
        model.addAttribute("light", light);
        model.addAttribute("pm25", pm25);
        model.addAttribute("pm10", pm10);
        return "sensebox";
    }

    // Get the current traffic count for a street in Cologne ()
    @GetMapping("/telraam")
    public String getTelraamVehicleCount(Model model) {
        long locationId = 105504;
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", TELRAM_API_TOKEN);
        headers.set("content-type", "application/json");

        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.exchange(
                TELRAM_API,
                HttpMethod.GET,
                entity,
                String.class
        );

        JSONObject json = new JSONObject(response.getBody());
        JSONArray features = json.getJSONArray("features");

        for (int i = 0; i < features.length(); i++) {
            JSONObject feature = features.getJSONObject(i);
            JSONObject properties = feature.getJSONObject("properties");

            long segmentId = properties.getInt("segment_id");
            if (segmentId == locationId) {
                model.addAttribute("cars", properties.getInt("car"));
                model.addAttribute("pedestrians", properties.getInt("pedestrian"));
                model.addAttribute("bikes", properties.getInt("bike"));
                model.addAttribute("trucks", properties.getInt("heavy"));
            }
        }

        return "trafficCount";
    }

    @GetMapping("/vehicle")
    public String getVehicleData(Model model) throws IOException {
// Lese die JSON-Datei aus dem static-Ordner ein
        File jsonFile = new File("src/main/resources/static/vehicleTestData.json");
        ObjectMapper objectMapper = new ObjectMapper();
        VehicleBreakdown[] testData = objectMapper.readValue(jsonFile, VehicleBreakdown[].class);

        model.addAttribute("vehicleBreakdowns", testData);

        // Gib jeden Testfall auf der Konsole aus
        for (VehicleBreakdown testCase : testData) {
            model.addAttribute("model", testCase.getModel());
            model.addAttribute("timestamp", testCase.getTimestamp().toString());
            model.addAttribute("location", (testCase.getLocation().getLatitude() + "(lat) " + testCase.getLocation().getLongitude() + "(lon)").toString());
            model.addAttribute("Hardware Fault Code", testCase.getErrorCode());
        }

        return "vehicle";
    }

    @GetMapping("/weather")
    public String getWeatherForecast(Model model) throws IOException {
        double latitude =  51.48;
        double longitude = 7.40;

        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("api.open-meteo.com")
                .path("/v1/forecast")
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .queryParam("hourly", "temperature_2m,relativehumidity_2m,windspeed_10m")
                .queryParam("timezone", "auto")
                .build();

        String url = uriComponents.toUriString();
        RestTemplate restTemplate = new RestTemplate();

        String response = restTemplate.getForObject(url, String.class);
        JsonObject rootObject = JsonParser.parseString(response).getAsJsonObject();
        JsonObject hourlyData = rootObject.getAsJsonObject("hourly");

        JsonArray timeStamps = hourlyData.getAsJsonArray("time");
        JsonArray temperatureForecast = hourlyData.getAsJsonArray("temperature_2m");
        JsonArray humidityForecast = hourlyData.getAsJsonArray("relativehumidity_2m");
        JsonArray windForecast = hourlyData.getAsJsonArray("windspeed_10m");


        model.addAttribute("timeStamps", timeStamps);
        model.addAttribute("temperature", temperatureForecast);
        model.addAttribute("humidity", humidityForecast);
        model.addAttribute("wind", windForecast);

        for(int i =0; i< timeStamps.size(); i++){
            System.out.println("Time: " + timeStamps.get(i) + " // Temperature: " + temperatureForecast.get(i) + " // Humidity: " + humidityForecast.get(i) + " // Wind: " + windForecast.get(i));
        }

        return "weather";
    }
}
