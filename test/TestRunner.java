import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.junit.platform.engine.discovery.DiscoverySelectors;

import java.io.PrintWriter;

public class TestRunner {
    public static void main(String[] args) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
            .selectors(
                DiscoverySelectors.selectClass(GraphNodeTest.class),
                DiscoverySelectors.selectClass(HistoryCleanTest.class),
                DiscoverySelectors.selectClass(MermaidReaderTest.class),
                DiscoverySelectors.selectClass(PipelineStageTest.class),
                DiscoverySelectors.selectClass(SimpleReaderTest.class)
            )
            .build();

        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        Launcher launcher = LauncherFactory.create();
        launcher.discover(request);
        launcher.execute(request, listener);

        TestExecutionSummary summary = listener.getSummary();
        if (!summary.getFailures().isEmpty()) {
            summary.printFailuresTo(new PrintWriter(System.err, true));
        }

        long found   = summary.getTestsFoundCount();
        long passed  = summary.getTestsSucceededCount();
        long failed  = summary.getTestsFailedCount();
        long skipped = summary.getTestsSkippedCount();
        System.out.printf("%nTests run: %d, Passed: %d, Failed: %d, Skipped: %d%n",
            found, passed, failed, skipped);
        if (failed > 0) System.exit(1);
    }
}
