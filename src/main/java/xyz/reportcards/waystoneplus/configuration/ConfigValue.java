package xyz.reportcards.waystoneplus.configuration;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A configuration value annotation
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigValue {

    /**
     * @return The key for the configuration value
     */
    String key();

}
