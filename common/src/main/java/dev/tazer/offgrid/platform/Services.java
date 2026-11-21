package dev.tazer.offgrid.platform;

import java.util.ServiceLoader;

public class Services {

    public static final Platform PLATFORM = load(Platform.class);

    public static <T> T load(Class<T> type) {
        return ServiceLoader.load(type, Services.class.getClassLoader()).findFirst().orElseThrow(() -> new IllegalStateException("No implementation of " + type.getName() + " was found"));
    }
}
