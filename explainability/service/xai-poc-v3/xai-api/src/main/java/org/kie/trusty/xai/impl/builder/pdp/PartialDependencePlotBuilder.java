package org.kie.trusty.xai.impl.builder.pdp;

import org.kie.trusty.xai.impl.explainer.global.viz.PartialDependencePlotProvider;

/**
 * Builder class for {@link PartialDependencePlotProvider}
 */
public class PartialDependencePlotBuilder {

    private PartialDependencePlotBuilder() {
    }

    public PartialDependencePlotProvider build() {
        return new PartialDependencePlotProvider();
    }

    public static PartialDependencePlotBuilder newPDPBuilder() {
        return new PartialDependencePlotBuilder();
    }
}
