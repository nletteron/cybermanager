package com.cybermanager.auth.application;

import org.junit.jupiter.api.Assertions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class ContractTestSupport {

    private ContractTestSupport() {
    }

    public static void assertRecordContract(Class<?> type) {
        Assertions.assertTrue(type.isRecord());
        RecordComponent[] components = type.getRecordComponents();
        Class<?>[] parameterTypes = new Class<?>[components.length];
        Object[] values = new Object[components.length];
        for (int index = 0; index < components.length; index++) {
            parameterTypes[index] = components[index].getType();
            values[index] = valueFor(components[index].getType(), components[index].getGenericType(), components[index].getName(), type.getSimpleName());
        }
        Object first = instantiate(type, parameterTypes, values);
        Object second = instantiate(type, parameterTypes, values);
        Assertions.assertEquals(first, second);
        Assertions.assertEquals(first.hashCode(), second.hashCode());
        Assertions.assertTrue(first.toString().contains(type.getSimpleName()));
        for (int index = 0; index < components.length; index++) {
            Object actual = invokeAccessor(components[index], first);
            assertSameValue(values[index], actual);
        }
    }

    public static <E extends Enum<E>> void assertEnumContract(Class<E> type) {
        E[] constants = type.getEnumConstants();
        Assertions.assertNotNull(constants);
        Assertions.assertTrue(constants.length > 0);
        for (E constant : constants) {
            Assertions.assertEquals(constant, Enum.valueOf(type, constant.name()));
        }
    }

    private static Object invokeAccessor(RecordComponent component, Object instance) {
        Object result;
        try {
            var accessor = component.getAccessor();
            accessor.setAccessible(true);
            result = accessor.invoke(instance);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
        return result;
    }

    private static Object instantiate(Class<?> type, Class<?>[] parameterTypes, Object[] values) {
        Object result;
        try {
            Constructor<?> constructor = type.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            result = constructor.newInstance(values);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
        return result;
    }

    private static Object valueFor(Class<?> type, Type genericType, String name, String ownerName) {
        Object result;
        if (type == String.class) {
            result = (name.toLowerCase().contains("email") || ownerName.toLowerCase().contains("emailaddress")) ? "user@cybermanager.local" : "value";
        } else if (type == int.class || type == Integer.class) {
            result = 1;
        } else if (type == long.class || type == Long.class) {
            result = 1L;
        } else if (type == boolean.class || type == Boolean.class) {
            result = true;
        } else if (type == BigDecimal.class) {
            result = new BigDecimal("12.50");
        } else if (type == UUID.class) {
            result = UUID.fromString("11111111-1111-1111-1111-111111111111");
        } else if (type == LocalDate.class) {
            result = LocalDate.of(2026, 1, 15);
        } else if (type == LocalDateTime.class) {
            result = LocalDateTime.of(2026, 1, 15, 10, 30);
        } else if (type == Instant.class) {
            result = Instant.parse("2026-01-15T10:30:00Z");
        } else if (type == byte[].class) {
            result = "content".getBytes();
        } else if (InputStream.class.isAssignableFrom(type)) {
            result = new ByteArrayInputStream("content".getBytes());
        } else if (List.class.isAssignableFrom(type)) {
            result = listValue(genericType);
        } else if (Set.class.isAssignableFrom(type)) {
            result = Set.of("ADMIN");
        } else if (type.isEnum()) {
            result = type.getEnumConstants()[0];
        } else if (type.isRecord()) {
            RecordComponent[] components = type.getRecordComponents();
            Class<?>[] parameterTypes = new Class<?>[components.length];
            Object[] values = new Object[components.length];
            for (int index = 0; index < components.length; index++) {
                parameterTypes[index] = components[index].getType();
                values[index] = valueFor(components[index].getType(), components[index].getGenericType(), components[index].getName(), type.getSimpleName());
            }
            result = instantiate(type, parameterTypes, values);
        } else {
            result = instantiateClass(type);
        }
        return result;
    }

    private static List<?> listValue(Type genericType) {
        List<?> result;
        if (genericType instanceof ParameterizedType parameterizedType && parameterizedType.getActualTypeArguments()[0] instanceof Class<?> componentType) {
            result = List.of(valueFor(componentType, componentType, "value", componentType.getSimpleName()));
        } else {
            result = List.of();
        }
        return result;
    }

    private static Object instantiateClass(Class<?> type) {
        Object result = null;
        Constructor<?>[] constructors = type.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (result == null) {
                Object[] values = new Object[constructor.getParameterCount()];
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                for (int index = 0; index < parameterTypes.length; index++) {
                    values[index] = valueFor(parameterTypes[index], parameterTypes[index], "value", type.getSimpleName());
                }
                try {
                    constructor.setAccessible(true);
                    result = constructor.newInstance(values);
                } catch (ReflectiveOperationException | IllegalArgumentException exception) {
                    result = null;
                }
            }
        }
        if (result == null) {
            throw new AssertionError("No usable constructor for " + type.getName());
        }
        return result;
    }

    private static void assertSameValue(Object expected, Object actual) {
        if (expected instanceof byte[] expectedBytes && actual instanceof byte[] actualBytes) {
            Assertions.assertArrayEquals(expectedBytes, actualBytes);
        } else {
            Assertions.assertEquals(expected, actual);
        }
    }

}
