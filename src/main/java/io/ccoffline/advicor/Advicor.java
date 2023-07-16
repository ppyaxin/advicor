package io.ccoffline.advicor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Advicor {

    private final String name;
    private final AtomicReference<Advice> advice;

    protected Advicor(String name) {
        Objects.requireNonNull(name, "Advicor must has a name");
        this.name = name;
        this.advice = new AtomicReference<>();
    }

    protected Advice createAdvice(Severity severity) {
        return new Advice().setName(name).setSeverity(severity);
    }

    public abstract Mono<Advice> adviceMono();

    public CompletableFuture<Advice> adviceAsync() {
        return adviceMono().toFuture();
    }

    public Advice adviceSync() {
        return adviceMono().block();
    }

    public final Mono<List<Advice>> adviceAll() {
        Map<String, Advicor> advicors = new ConcurrentHashMap<>();
        return adviceDistinct(advicors)
                .reduce(Stream.<Advice>builder(), Stream.Builder::add)
                .map(b -> {
                    // sort by severity and set all the ids
                    List<Advice> advices = b.build().sorted().collect(Collectors.toList());
                    for (int i = 0; i < advices.size(); i++) {
                        advices.get(i).setId(i + 1);
                    }
                    // set traces id
                    for (Advice advice : advices) {
                        advice.setTraceIds(advice.getTraces().stream()
                                // get the advice result of the first advicor
                                .map(advicor -> advicors.get(advicor.name).advice.get())
                                // filter out advicors without advice
                                .filter(Objects::nonNull).map(Advice::getId).toList());
                    }
                    return advices;
                });
    }

    private Flux<Advice> adviceDistinct(Map<String, Advicor> advicors) {
        Advicor advicor = advicors.putIfAbsent(this.name, this);
        if (advicor == null) {
            // first advicor, run all traces advicors
            Mono<Advice> base = this.adviceMono()
                    .onErrorResume(t -> Mono.just(adviceOnError(t)))
                    // set the advice
                    .doOnNext(this.advice::set)
                    // 1. create the advice
                    // 2. use this advice to create traces advice
                    // if we do not cache here, this mono would be run twice
                    .cache();
            Flux<Advice> traces = base.flatMapIterable(Advice::getTraces)
                    // run all the trace advicors
                    .flatMap(a -> a.adviceDistinct(advicors));
            return base.concatWith(traces);
        } else {
            // duplicated advicor, do not run
            return Flux.empty();
        }
    }

    private Advice adviceOnError(Throwable throwable) {
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return createAdvice(Severity.WARNING)
                .setDescription("Advicor encounters an error. Please check manually or rerun this advicor.\n" + writer);
    }

}
