package io.ccoffline.advicor;

import reactor.core.publisher.Mono;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestAdvicor extends Advicor {

    private final int id;

    public TestAdvicor(int id) {
        super(String.format("TestAdvice %s", id));
        this.id = id;
    }

    @Override
    public Mono<Advice> adviceMono() {
        if (id <= 0) {
            return Mono.error(new RuntimeException("id <= 0"));
        } else {
            Random random = new Random();
            return Mono.just(createAdvice(Severity.values()[random.nextInt(4)]).setTraces(
                    IntStream.rangeClosed(1, 1 + random.nextInt(1))
                            .mapToObj(i -> new TestAdvicor(id - i))
                            .collect(Collectors.toList())));
        }
    }
}
