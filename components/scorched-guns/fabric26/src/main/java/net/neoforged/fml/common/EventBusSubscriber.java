/* Adapted from JEG fabric.compat.neoforge (GPL-3.0) for the Scorched Fabric 26.3 bridge. */
package net.neoforged.fml.common;

import net.neoforged.api.distmarker.Dist;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EventBusSubscriber {
    String modid() default "";
    Dist[] value() default {};
}
