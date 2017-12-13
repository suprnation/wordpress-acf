package com.suprnation.cms.utils;

import lombok.SneakyThrows;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ReflectionUtils {

    @SneakyThrows
    public static <T, U> void setField(T obj, String fieldName, U value) {
        Field field = getField(fieldName, obj.getClass());
        field.setAccessible(true);
        field.set(obj, value);

    }

    @SneakyThrows
    public static <T, U> void setField(T obj, Field field, U value) {
        field.setAccessible(true);
        field.set(obj, value);
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static <U> Field getField(String name, Class<U> valueClazz) {
        return valueClazz.getDeclaredField(name);
    }


    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static <U> Method getMethod(String name, Class<U> valueClazz) {
        return Arrays.stream(valueClazz.getDeclaredMethods()).filter(method -> method.getName().equals(name)).findFirst().orElse(null);
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static <T, U> U getFieldValue(T obj, Field field, Class<U> valueClazz) {
        field.setAccessible(true);
        return (U) field.get(obj);
    }

    public static Set<Field> getFieldsForClassWithAnnotationAndSetAccessible(Class<?> clazz, Class<? extends Annotation> annotation) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> String.class == field.getType() && (field.getDeclaredAnnotation(annotation) != null || clazz.getDeclaredAnnotation(annotation) != null))
                .collect(Collectors.toSet());
    }

    @SneakyThrows
    public static <T> T instantiate(Class<T> clazz) {
        Constructor<T> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

}
