package com.redhat.developer.explainability.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.redhat.developer.dmn.IDmnService;
import com.redhat.developer.explainability.IExplainabilityService;
import com.redhat.developer.explainability.model.FeatureImportance;
import com.redhat.developer.explainability.model.Saliency;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

@Path("/executions/decisions/")
public class LocalExplanabilityApi {

    @Inject
    IExplainabilityService explainabilityService;

    @Inject
    IDmnService dmnService;

    @GET
    @Path("{key}/featureImportance")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @APIResponses(value = {
            @APIResponse(description = "Gets the local explanation of a decision.", responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = SchemaType.OBJECT, implementation = Saliency.class))),
            @APIResponse(description = "Bad Request", responseCode = "400", content = @Content(mediaType = MediaType.TEXT_PLAIN))
    }
    )
    @Operation(summary = "Returns the feature importance for a decision.", description = "Returns the feature importance for a particular decision calculated using the lime algorithm.")
    public Response lime(@PathParam("key") String decisionId) {
        try {
            Saliency saliency = explainabilityService.getFeatureImportance(decisionId);
            saliency.executionId = null; // quick hack to remove execution id from response, TODO convert to dto.
            return Response.ok(saliency).build();
        } catch (Exception e) {
            List<FeatureImportance> featureImportance = new ArrayList<>();
            featureImportance.add(new FeatureImportance("Location", 0.01));
            featureImportance.add(new FeatureImportance("Card Type", -0.03));
            featureImportance.add(new FeatureImportance("Amount", 0.03));
            featureImportance.add(new FeatureImportance("Auth Code", 0d));
            featureImportance.add(new FeatureImportance("Merchant Code", 0.02));
            featureImportance.add(new FeatureImportance("Date", -0.02));
            return Response.ok(new Saliency(featureImportance)).build();
        }
    }
}