package com.cybermanager.application;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public final class ApplicationClassContractAssertions {

    private ApplicationClassContractAssertions() {
    }

    public static void assertSinglePublicExecuteMethod(Class<?> type) {
        Method[] executeMethods = Arrays.stream(type.getDeclaredMethods())
                .filter(method -> "execute".equals(method.getName()))
                .toArray(Method[]::new);

        assertEquals(1, executeMethods.length);
        assertFalse(Modifier.isPrivate(executeMethods[0].getModifiers()));
    }

}
