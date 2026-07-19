package io.echotrace.demo.scenario;

import java.time.Instant;
import java.util.List;

public final class SimulationResponse {

    private final String scenarioId;
    private final int requestedOrders;
    private final int successfulOrders;
    private final int failedOrders;
    private final int failurePercentage;
    private final Instant generatedAt;
    private final List<String> journeyIds;
    private final String serviceVersion;
    private final String deploymentId;

    public SimulationResponse(String scenarioId, int requestedOrders, int successfulOrders,
                              int failedOrders, int failurePercentage, Instant generatedAt,
                              List<String> journeyIds, String serviceVersion, String deploymentId) {
        this.scenarioId = scenarioId;
        this.requestedOrders = requestedOrders;
        this.successfulOrders = successfulOrders;
        this.failedOrders = failedOrders;
        this.failurePercentage = failurePercentage;
        this.generatedAt = generatedAt;
        this.journeyIds = List.copyOf(journeyIds);
        this.serviceVersion = serviceVersion;
        this.deploymentId = deploymentId;
    }

    public String getScenarioId() { return scenarioId; }
    public int getRequestedOrders() { return requestedOrders; }
    public int getSuccessfulOrders() { return successfulOrders; }
    public int getFailedOrders() { return failedOrders; }
    public int getFailurePercentage() { return failurePercentage; }
    public Instant getGeneratedAt() { return generatedAt; }
    public List<String> getJourneyIds() { return journeyIds; }
    public String getServiceVersion() { return serviceVersion; }
    public String getDeploymentId() { return deploymentId; }
}
