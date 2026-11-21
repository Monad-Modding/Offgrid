package dev.tazer.offgrid.platform.condition;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(IfModPresent.List.class)
public @interface IfModPresent {

    String value();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface List {

        IfModPresent[] value();
    }
}
