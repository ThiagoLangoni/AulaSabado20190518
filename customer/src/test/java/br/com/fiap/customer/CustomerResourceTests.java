package br.com.fiap.customer;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class CustomerResourceTests {

    @Autowired
    private CustomerRepository customerRepository;

    @LocalServerPort
    private Integer port;

    @Before
    public void setup() {
        stubCreateCustomer();

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = this.port;
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .addHeader("Content-Type", "application/json")
                .build();
    }

    private void stubCreateCustomer() {

        Customer customer = new Customer();
        customer.setName("Thiago");
        customer.setLastname("Langoni");
        customer.setGender("M");
        customer.setAge(34);

        customerRepository.save(customer);
    }

    @Test
    public void shouldFindCustomerById() {

        RestAssured.get( "/customer/1")
                   .then()
                   .assertThat()
                   .statusCode(200)
                   .body(  "name", Matchers.is(  "Thiago"))
                   .body(  "lastname", Matchers.is(  "Langoni"))
                   .body(  "gender", Matchers.is(  "M"))
                   .body(  "age", Matchers.is(  34));
    }

    @Test
    public void cannotFindCustomerById() {

        RestAssured.get( "/customer/300")
                .then()
                .assertThat()
                .statusCode(500)
                .body(  "message", Matchers.is(  "404 Customer Not Found"));
    }

    @Test
    public void cannotCreateCustomerWhenGenderIsInvalid() {

        CustomerRequest customerRequest = new CustomerRequest();
        customerRequest.setName("Thiago");
        customerRequest.setLastname("Langoni");
        customerRequest.setGender("A");
        customerRequest.setAge(38);

        RestAssured.given()
                .body(customerRequest)
                .post("/customer/")
                .then()
                .assertThat()
                .statusCode(404)
                .body("message", Matchers.is("Gender is Invalid"));
    }
}
