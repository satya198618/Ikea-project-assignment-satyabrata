package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.is;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusIntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WarehouseEndpointIT {

  private static final String BASE_PATH = "warehouse";
  private static final String WAREHOUSE_001 = "MWH.001";
  private static final String WAREHOUSE_012 = "MWH.012";
  private static final String WAREHOUSE_023 = "MWH.023";
  private static final String NEW_WAREHOUSE = "MWH.NEW";

  @Test
  @Order(1)
  public void testGetAllWarehouses() {
    given()
            .when()
            .get(BASE_PATH)
            .then()
            .statusCode(200)
            .body(
                    containsString(WAREHOUSE_001),
                    containsString(WAREHOUSE_012),
                    containsString(WAREHOUSE_023)
            );
  }

  @Test
  @Order(2)
  public void testGetWarehouseById() {
    given()
            .when()
            .get(BASE_PATH + "/1")
            .then()
            .statusCode(200)
            .body(containsString(WAREHOUSE_001));
  }

  @Test
  @Order(3)
  public void testCreateWarehouse() {
    String newWarehouseJson = String.format("""
            {
                "businessUnitCode": "%s",
                "location": "AMSTERDAM-001"
            }
            """, NEW_WAREHOUSE);

    given()
            .contentType("application/json")
            .body(newWarehouseJson)
            .when()
            .post(BASE_PATH)
            .then()
            .statusCode(201);

    // Verify the new warehouse is in the list
    given()
            .when()
            .get(BASE_PATH)
            .then()
            .statusCode(200)
            .body(containsString(NEW_WAREHOUSE));
  }

  @Test
  @Order(4)
  public void testUpdateWarehouse() {
    String updatedWarehouseJson = """
            {
                "businessUnitCode": "MWH.UPDATED",
                "location": "ROTTERDAM-002"
            }
            """;

    given()
            .contentType("application/json")
            .body(updatedWarehouseJson)
            .when()
            .put(BASE_PATH + "/1")
            .then()
            .statusCode(200)
            .body(containsString("MWH.UPDATED"));

    // Verify the update
    given()
            .when()
            .get(BASE_PATH + "/1")
            .then()
            .statusCode(200)
            .body(containsString("ROTTERDAM-002"));
  }

  @Test
  @Order(5)
  public void testArchiveWarehouse() {
    // Archive the warehouse
    given()
            .when()
            .delete(BASE_PATH + "/1")
            .then()
            .statusCode(204);

    // Verify the archived warehouse is not in the list
    given()
            .when()
            .get(BASE_PATH)
            .then()
            .statusCode(200)
            .body(not(containsString("MWH.UPDATED")));

    // But should still be accessible directly
    given()
            .when()
            .get(BASE_PATH + "/1")
            .then()
            .statusCode(200)
            .body("archived", is(true));
  }

  @Test
  @Order(6)
  public void testGetNonExistentWarehouse() {
    given()
            .when()
            .get(BASE_PATH + "/999")
            .then()
            .statusCode(404);
  }

  @Test
  @Order(7)
  public void testCreateWarehouseWithInvalidData() {
    String invalidWarehouseJson = """
            {
                "businessUnitCode": "",
                "location": ""
            }
            """;

    given()
            .contentType("application/json")
            .body(invalidWarehouseJson)
            .when()
            .post(BASE_PATH)
            .then()
            .statusCode(400);
  }
}