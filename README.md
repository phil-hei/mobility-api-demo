# mobility-api-demo
Simple examples on how to consume APIs related to smart mobility or how to read in test data from a vehicle.

The application is basically a Spring Boot application that runs at localhost:8080 after the application has been started.
We defined an ExampleController that controls the application and calls the APIs. Thymeleaf templates are displaying the values as HTML.

There are four examples provided:

## Opensense Box
Example on how to retrieve data from an open sense box deplyoed at TU Dortmund.
You can invoke the example by calling localhost:8080/sensebox

## Telraam
Example on how to retrieve traffic count data from a single telraam segment.
The example call gets the amount of vehicles for the last hour (traffic snapshot).
You can invoke the example by calling localhost:8080/telraam

Note that you have to register at the telraam platform and provide your API key from there into the source code (cf. the according variable)

## Vehicle Test Data
The third example demonstrate on how to parse test data from a .json file located in "resources/static".
The test data can be modified according to your use case

## Weather Forecast
The last exmaple show how get the hourly weather forecast for a distinct location.
For the reason of simplicity we just printed the values at the console.
