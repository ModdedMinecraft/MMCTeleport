package net.moddedminecraft.mmcteleport;

import java.util.function.Function;

public class Util {

    public static Function<Object,Double> doubleTransformer = new Function<Object,Double>() {
        @Override
        public Double apply(Object input) {
            if (input instanceof Double) {
                return (Double) input;
            } else {
                return null;
            }
        }
    };

}
