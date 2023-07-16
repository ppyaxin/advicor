package io.ccoffline.advicor;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public final class Advice implements Comparable<Advice> {

    private String name;
    private String description;
    private Severity severity;
    private List<Advicor> traces;

    private int id;
    private List<Integer> traceIds;

    Advice() {
    }

    @Override
    public int compareTo(Advice o) {
        return this.getSeverity().compareTo(o.getSeverity());
    }

    public List<Advicor> getTraces() {
        return traces == null ? Collections.emptyList() : traces;
    }

    public Advice setName(String name) {
        this.name = name;
        return this;
    }

    public Advice setDescription(String description) {
        this.description = description;
        return this;
    }

    public Advice setSeverity(Severity severity) {
        this.severity = severity;
        return this;
    }

    public Advice setTraces(List<Advicor> traces) {
        this.traces = traces;
        return this;
    }

    public Advice setId(int id) {
        this.id = id;
        return this;
    }

    public Advice setTraceIds(List<Integer> traceIds) {
        this.traceIds = traceIds;
        return this;
    }
}
