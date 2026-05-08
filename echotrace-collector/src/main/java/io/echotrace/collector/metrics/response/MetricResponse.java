package io.echotrace.collector.metrics.response;

import java.util.List;

public class MetricResponse {

    private List<String> labels;
    private List<Long> values;

    public MetricResponse(List<String> labels, List<Long> values) {
        this.labels = labels;
        this.values = values;
    }

    public List<String> getLabels() {
        return labels;
    }

    public List<Long> getValues() {
        return values;
    }
}
