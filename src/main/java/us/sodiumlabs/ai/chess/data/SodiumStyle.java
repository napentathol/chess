package us.sodiumlabs.ai.chess.data;

import org.immutables.value.Value;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PACKAGE, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS) // Make it class retention for incremental compilation
@Value.Style(
    get = {"is*", "get*"}, // Detect 'get' and 'is' prefixes in accessor methods
    init = "with*", // Builder initialization methods will have 'set' prefix
    builder = "new", // construct builder using 'new' instead of factory method
    visibility = Value.Style.ImplementationVisibility.PUBLIC) // Disable copy methods by default
public @interface SodiumStyle {
}
