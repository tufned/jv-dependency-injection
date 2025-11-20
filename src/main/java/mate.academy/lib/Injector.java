package mate.academy.lib;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new IllegalStateException("Class must have @Component annotation");
        }
        Object clazzImplInstance = null;
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object dependency = getInstance(field.getType());
                clazzImplInstance = createInstance(clazz);
                try {
                    field.setAccessible(true);
                    field.set(clazzImplInstance, dependency);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Couldn't inject field: " + field.getName(), e);
                }
            }
        }
        if (clazzImplInstance == null) {
            clazzImplInstance = createInstance(clazz);
        }
        return clazzImplInstance;
    }

    private Object createInstance(Class<?> clazz) {
        try {
            if (instances.containsKey(clazz)) {
                return instances.get(clazz);
            }
            Object instance = clazz.getConstructor().newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (NoSuchMethodException | InstantiationException
                 | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName());
        }
    }

    private <T> Class<?> findImplementation(Class<T> interfaceClazz) {
        Map<Class<?>, Class<?>> impls = new HashMap<>();
        impls.put(FileReaderService.class, FileReaderServiceImpl.class);
        impls.put(ProductService.class, ProductServiceImpl.class);
        impls.put(ProductParser.class, ProductParserImpl.class);
        if (interfaceClazz.isInterface()) {
            return impls.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
