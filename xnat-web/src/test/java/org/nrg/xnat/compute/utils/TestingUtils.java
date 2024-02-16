package org.nrg.xnat.compute.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.transaction.TestTransaction;

@Slf4j
public class TestingUtils {

    public static void commitTransaction() {
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
    }

}
