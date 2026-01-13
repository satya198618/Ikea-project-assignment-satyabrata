package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.InvalidWarehouseStateException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.LocationNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

class LocationGatewayTest {

  private LocationGateway locationGateway;

  @BeforeEach
  void setUp() {
    locationGateway = new LocationGateway();
  }

  // POSITIVE TEST CASES
  @Test
  void testResolveByIdentifier_ValidLocation_ReturnsLocation() {
    // Given
    String validIdentifier = "ZWOLLE-001";

    // When
    Location result = locationGateway.resolveByIdentifier(validIdentifier);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.identification).isEqualTo("ZWOLLE-001");
    assertThat(result.maxNumberOfWarehouses).isOne();
    assertThat(result.maxCapacity).isEqualTo(40);
  }

  @Test
  void testResolveByIdentifier_AmsterdamLocation_ReturnsCorrectData() {
    // Given
    String validIdentifier = "AMSTERDAM-001";

    // When
    Location result = locationGateway.resolveByIdentifier(validIdentifier);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.identification).isEqualTo("AMSTERDAM-001");
    assertThat(result.maxNumberOfWarehouses).isEqualTo(5);
    assertThat(result.maxCapacity).isEqualTo(100);
  }

  @Test
  void testResolveByIdentifier_AllLocations_Success() {
    // Test all predefined locations
    String[] locationIdentifiers = {
        "ZWOLLE-001", "ZWOLLE-002", "AMSTERDAM-001", "AMSTERDAM-002",
        "TILBURG-001", "HELMOND-001", "EINDHOVEN-001", "VETSBY-001"
    };

    for (String identifier : locationIdentifiers) {
      assertThat(catchThrowable(() -> locationGateway.resolveByIdentifier(identifier)))
          .as("Should not throw for identifier: " + identifier)
          .isNull();
    }
  }

  // NEGATIVE TEST CASES
  @Test
  void testResolveByIdentifier_NullIdentifier_ThrowsException() {
    // When & Then
    assertThatThrownBy(() -> locationGateway.resolveByIdentifier(null))
        .isInstanceOf(InvalidWarehouseStateException.class)
        .hasMessage("Location identifier cannot be null or empty");
  }

  @Test
  void testResolveByIdentifier_EmptyIdentifier_ThrowsException() {
    // When & Then
    assertThatThrownBy(() -> locationGateway.resolveByIdentifier(""))
        .isInstanceOf(InvalidWarehouseStateException.class)
        .hasMessage("Location identifier cannot be null or empty");
  }

  @Test
  void testResolveByIdentifier_BlankIdentifier_ThrowsException() {
    // When & Then
    assertThatThrownBy(() -> locationGateway.resolveByIdentifier("   "))
        .isInstanceOf(InvalidWarehouseStateException.class)
        .hasMessage("Location identifier cannot be null or empty");
  }

  @Test
  void testResolveByIdentifier_NonExistentLocation_ThrowsException() {
    // When & Then
    assertThatThrownBy(() -> locationGateway.resolveByIdentifier("NONEXISTENT-999"))
        .isInstanceOf(LocationNotFoundException.class)
        .hasMessageContaining("NONEXISTENT-999");
  }

  @Test
  void testResolveByIdentifier_CaseSensitive_ThrowsException() {
    // When & Then - lowercase when uppercase is expected
    assertThatThrownBy(() -> locationGateway.resolveByIdentifier("amsterdam-001"))
        .isInstanceOf(LocationNotFoundException.class)
        .hasMessageContaining("amsterdam-001");
  }
}
