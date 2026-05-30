/**
 * Copyright (c) 2025, www.uoquo.com All Rights Reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄
 */
package com.uoquo.gateway.filter;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.PrintWriter;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * 直接运行 Preservation 测试的 main 入口，绕过 Maven surefire skip 配置。
 */
public class PreservationTestRunner {

    public static void main(String[] args) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(LoggingFilterPreservationTest.class))
                .build();

        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        Launcher launcher = LauncherFactory.create();
        launcher.discover(request);
        launcher.execute(request, listener);

        TestExecutionSummary summary = listener.getSummary();
        summary.printFailuresTo(new PrintWriter(System.out, true));

        System.out.println("\n=== Test Summary ===");
        System.out.println("Tests started:  " + summary.getTestsStartedCount());
        System.out.println("Tests passed:   " + summary.getTestsSucceededCount());
        System.out.println("Tests failed:   " + summary.getTestsFailedCount());
        System.out.println("Tests skipped:  " + summary.getTestsSkippedCount());

        if (summary.getTestsFailedCount() > 0) {
            System.out.println("\n=== Failures (unexpected — baseline behavior broken) ===");
            summary.getFailures().forEach(failure -> {
                System.out.println("FAILED: " + failure.getTestIdentifier().getDisplayName());
                System.out.println("Reason: " + failure.getException().getMessage());
            });
            System.exit(1);
        } else {
            System.out.println("\nAll preservation tests PASSED — baseline behavior confirmed.");
        }
    }
}
