*******************************************************************************

Contents:
1) Architecture
2) Build and Run
3) Documentation
4) Testing
5) Configuration
6) TODO

*******************************************************************************

1) Architecture

The app has a MySQL backend and i used JPA for persistence over Hibernate. I
use DTO/entity objects to pass data back and for between layers. You dont have
to start MySQL, Docker does.

I used a generic response JSON structure to handle the errors when uploading a
multi line file. Here is a truncated example of the JSON you will see as a
response to a fraction file upload (the metre readings upload is same struct):

{
    "items": [
        {
            "month": "JAN",
            "profile": "A",
            "fraction": 0.3125,
            "id": "ca81c7be-55bf-42c1-857a-0fb4fcc26a9e"
        },
        {
            "month": "FEB",
            "profile": "A",
            "fraction": 0.0333,
            "id": "770a3c7d-9e8c-4e80-a5f5-191b6a612a31"
        }
    ],
    "errors": [
        {
            "message": "Fractions for profile B dont sum to 1.0, sum: 2.000",
            "code": 5
        }
    ]
}

As you can see, profile A fractions uploaded and you see them in "items" ,
but profile B fractions had an issue and did not upload. You see an error
in the "errors" section. Note that the HTTP status code here would be 200,
even though we have a validation error. Note the error code, this is an
application level error - i have enumerated the validation errors in a
class called Errors. For more serious errors we use HTTP status codes.

A consumption is added during uploading of the metre readings and
can be retrieved, the JSON looks like this:

{
    "metre_id": "0004",
    "month": "DEC",
    "profile": "B",
    "consumption": 1.00,
    "id": "e7798915-6ba0-46fa-b4bf-e9643250a463"
}


The validations are found in the service layers (in the service package).

Standard SpringBOOT package structure has been used..

I did not attempt the AXON implementation.

I used Java 17 to build the app, in Intellij. I commited all the project
files except target binaries (caveat - the jar is included). The main
SpringBOOT entry point is called ElectricApplication. Usually i run
everything inside Docker unless im debugging.

I implemented the CRUD methods mentioned in the spec but I did not implement
all the standard CRUD endpoints for Fractions and Readings because
i did not have time to think through handling these with CSV files or as single
entities. For example, should a Fraction Delete be allowed on a per Fraction
basis? Would it not invalidate the Fraction group it belongs to and any Reading
that was generated using that Fraction? Should we handle fraction Delete based
on a CSV file similar to Insert? I have stubbed out the methods with "Not Yet
Implemented" exceptions.

*******************************************************************************

2) Build and Run

Start Docker (eg. Docker Desktop on Windows)

To build the app:

  ./mvnw clean install
  ./mvnw clean install -DskipTests

To run the app in Docker:
      docker build -t etpa-0.0.1 .
      docker compose up
          -- now the service is running on localhost:9999 and you can use
          postman test cases (included in resources/) or use Swagger to test

The main endpoints:
  http://localhost:9999/etpa/fractions
  http://localhost:9999/etpa/metrereadings
  http://localhost:9999/etpa/consumption/:month/:metre_id

*******************************************************************************

3) Documentation:

Start the app then you will find Swagger REST API documention:
    http://localhost:9999/swagger-ui/index.html

This can be used to drive the endpoints too.

*******************************************************************************

4) Testing

Test cases:
   See the class called ElectricApplicationTests, there are 84% coverage test
   cases in there. These are integration tests done using TestContainers, i did
   not do any mocking based unit tests.

   You can also test via Swagger.

   I have included my Postman workspace in resources/Etpa postman.
   Sample test data:
      java/resources/fractions.csv
      java/resources/readings.csv

   If you run the tests expect to see errors in the logs. This is correct
   because some of the tests expose error handling for testing.

*******************************************************************************

5) Configuration

Config files:
  application.properties
  pom.xml
  compose.yaml
  .env

You should not have to edit anything to get it running locally on port 9999.

*******************************************************************************

6) TODO

I should use the business key from the Fraction file (month, profile) as the
primary key for doing insert/update/delete. The same concept applies to the
Metre Reading file, they business key is (metreId,month,profile)..the foreign
key from MetreReading to fraction is (month,profile).

If i do this then i can implement CRUD using the CSV file in all the
endpoints. POST would fail if you try and insert the same business key twice.
PUT would use the business key to lookup the fraction to modify. DELETE
would use the business key of the row to know which row to delete from
the database.



Paul Brown
November 2024
