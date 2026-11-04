package com.cybermanager.domain.exceptions;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DomainExceptionTest {

    @Test
    void shouldExposeDomainErrorCodeAndParameters() {
        DomainException result = new DomainException(
                BusinessErrorType.NOT_FOUND,
                BusinessErrorCode.CUSTOMER_NOT_FOUND,
                "Customer {id} not found",
                Map.of("id", "42")
        );

        assertEquals(BusinessErrorType.NOT_FOUND, result.type());
        assertEquals(BusinessErrorCode.CUSTOMER_NOT_FOUND, result.businessErrorCode());
        assertEquals("CUSTOMER_NOT_FOUND", result.code());
        assertEquals("Customer {id} not found", result.getMessage());
        assertEquals(Map.of("id", "42"), result.parameters());
    }

    @Test
    void shouldUseEmptyParametersWhenNull() {
        DomainException result = new DomainException(
                BusinessErrorType.NOT_FOUND,
                BusinessErrorCode.CUSTOMER_NOT_FOUND,
                "Customer not found",
                null
        );

        assertEquals(Map.of(), result.parameters());
    }

    @Test
    void shouldProtectParametersFromModification() {
        Map<String, Object> parameters = new HashMap<>(Map.of("id", "42"));
        DomainException result = new DomainException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.CUSTOMER_NOT_FOUND, "Message", parameters);

        parameters.put("id", "changed");

        assertEquals(Map.of("id", "42"), result.parameters());
        assertThrows(UnsupportedOperationException.class, () -> result.parameters().put("id", "changed"));
    }

    @Test
    void shouldPreserveMetadataWhenSerialized() throws Exception {
        DomainException original = new DomainException(BusinessErrorType.NOT_FOUND, BusinessErrorCode.CUSTOMER_NOT_FOUND, "Message {id}", Map.of("id", "42"));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(original);
        }

        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            DomainException restored = (DomainException) input.readObject();

            assertEquals(original.type(), restored.type());
            assertEquals(original.businessErrorCode(), restored.businessErrorCode());
            assertEquals(original.getMessage(), restored.getMessage());
            assertEquals(original.parameters(), restored.parameters());
            assertThrows(UnsupportedOperationException.class, () -> restored.parameters().put("id", "changed"));
        }
    }

}
