package com.travel.farecalc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.travel.farecalc.bean.Address;
import com.mashape.unirest.http.*;
import com.travel.farecalc.dto.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

@RestController
public class AddressController
{
    static final double baseFare = 2.85;
    static final double pricePerMile = 2.7;
    public double extractStringFromObject(String typeToExtract, Object myObject)
    {
        double result = 0;

        //We need to get the next two things from the list
        String newString = myObject.toString().substring(1, myObject.toString().length() -2);

        //Get Rid of the unnecessary characters
        String newString1 = newString.substring(1, newString.length() -3);

        //Split the string to a string array then grab the necessary data.
        String[] dataArray = newString1.split(",");
        String[] tempLat = dataArray[0].split("=");
        String[] tempLon = dataArray[1].split("=");
        String lat = tempLat[1];
        String lon = tempLon[1];

        if (typeToExtract.equals("latitude"))
        {
            result = Double.parseDouble(lat);
        }
        else
        {
            result = Double.parseDouble(lon);
        }

        return result;
    }

    @PostMapping("/getCost")
    public ResponseDto getDistanceBetweenAddresses(@RequestBody Address[] addressArray) throws UnirestException
    {
        double sourceLatitude = 0,
               sourceLongitude = 0,
               destinationLatitude = 0,
               destinationLongitude = 0,
               distance = 0;

        String apiKey = System.getenv("apiKEY");

        Address sourceAddress = addressArray[0];
        Address destinationAddress = addressArray[1];

        //Set the coordinates of the source and destination address.
        String sourceURL = System.getenv("baseURL") + "/geocode/forward?query="
                + sourceAddress.getStreetAddress().replace(' ', '+' ) + "+"
                + sourceAddress.getCity() + "+"
                + sourceAddress.getState();

        String destinationURL = System.getenv("baseURL") + "/geocode/forward?query="
                + destinationAddress.getStreetAddress().replace(' ', '+' ) + "+"
                + destinationAddress.getCity() + "+"
                + destinationAddress.getState();

        Unirest.setTimeouts(0,0);
        HttpResponse<String> sourceResponse = Unirest.get(sourceURL)
                .header("Authorization", apiKey)
                .asString();

        HttpResponse<String> destinationResponse = Unirest.get(destinationURL)
                .header("Authorization", apiKey)
                .asString();

        ObjectMapper objectMapper = new ObjectMapper();

        try
        {
            //Get source data first
            Map<String, Object> sourceData = objectMapper.readValue(sourceResponse.getBody(), new TypeReference<>(){});
            Object sourceObject = sourceData.get("addresses");

            sourceLatitude = extractStringFromObject("latitude", sourceObject);
            sourceLongitude = extractStringFromObject("longitude", sourceObject);

            //Lastly get destination data
            Map<String, Object> destinationData = objectMapper.readValue(destinationResponse.getBody(), new TypeReference<>(){});
            Object destinationObject = destinationData.get("addresses");

            destinationLatitude = extractStringFromObject("latitude", destinationObject);
            destinationLongitude = extractStringFromObject("longitude", destinationObject);

        }
        catch(Error | JsonProcessingException e)
        {
            System.out.println(e);
        }

        //Now that we have the coordinates, we can build the url and call the Distance API to aid in our calculation
        String distanceURL = System.getenv("baseURL") + "/route/distance?origin=" +
                sourceLatitude + "," + sourceLongitude + "&destination=" +
                destinationLatitude + "," + destinationLongitude + "&modes=car&units=imperial";


        HttpResponse<String> distanceResponse = Unirest.get(distanceURL)
                .header("Authorization", apiKey)
                .asString();

        try
        {
            Map<String, Object> sourceData = objectMapper.readValue(distanceResponse.getBody(), new TypeReference<>(){});
            Object distanceObject = sourceData.get("routes");

            //We need to get the next two things from the list
            String newString = distanceObject.toString().substring(1, distanceObject.toString().length() -2);
            String[] tmpDistanceStringArray = newString.split("}");

            String[] tmpDistanceStringArray2 = tmpDistanceStringArray[3].split(",");
            String[] tmpDistanceStringArray3 = tmpDistanceStringArray2[2].split("=");
            String[] tmpDistanceStringArray4 = tmpDistanceStringArray3[1].split(" ");

            distance = Double.parseDouble(tmpDistanceStringArray4[0]);

            System.out.println("Hi");

        }
        catch(Error | JsonProcessingException e)
        {
            System.out.println(e);
        }

        double cost = (baseFare + pricePerMile * distance);

        ResponseDto myResponse = new ResponseDto();

        myResponse.setMessage("The fare was calculated successfully.");
        myResponse.setStatus(HttpStatus.OK.value());
        myResponse.setTimestamp(new Date());
        myResponse.setData(cost);

        return myResponse;

    }
}