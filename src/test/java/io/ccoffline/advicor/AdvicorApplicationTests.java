package io.ccoffline.advicor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AdvicorApplicationTests {

    @Test
    void contextLoads() {
        new TestAdvicor(5).adviceAll().doOnNext(advices -> advices.forEach(advice -> {
            System.out.printf("%s. %s [%s]\n", advice.getId(), advice.getName(), advice.getSeverity());
            if (advice.getDescription() != null) {
                System.out.printf("%s\n", advice.getDescription());
            }
            if (!advice.getTraceIds().isEmpty()) {
                System.out.printf("Please refer to the following advices: %s\n", advice.getTraceIds());
            }
            System.out.println();
        })).block();
    }

}
