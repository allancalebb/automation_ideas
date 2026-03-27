package com.zohopeopleqa.listeners;

import com.zohopeopleqa.base.BaseTest;
import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Declared BEFORE AllureTestNg in testng.xml so that onTestSuccess/onTestFailure
 * fires while the Allure test lifecycle is still open — allowing steps and attachments
 * to be added into the current test result before Allure writes it to disk.
 */
public class AllureAttachmentListener implements ITestListener {

    @Override
    public void onTestSuccess(ITestResult result) {
        attachLogAsStep("✅");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        attachLogAsStep("❌");
        attachFailureScreenshot();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        // Nothing extra for skipped tests
    }

    /**
     * Creates a named step in the test body that contains the full console log as an
     * attachment. The step appears at the bottom of the Steps section and is immediately
     * visible in the test overview — no need to look in a separate "Logs" tab.
     *
     * The Allure lifecycle is still open at this point (we fire before AllureTestNg
     * which writes the result in its own onTestSuccess), so startStep/addAttachment
     * correctly bind to the current test case UUID.
     */
    private void attachLogAsStep(String emoji) {
        try {
            // Flush the in-progress log stream so all bytes reach the file
            java.io.FileOutputStream stream = BaseTest.THREAD_LOG_STREAMS.get(Thread.currentThread().getId());
            if (stream != null) stream.flush();

            String logPath = BaseTest.CURRENT_LOG_PATH.get();
            if (logPath == null) return;

            java.nio.file.Path logFile = Paths.get(logPath);
            if (!Files.exists(logFile) || Files.size(logFile) == 0) return;

            byte[] logBytes = Files.readAllBytes(logFile);

            // Open a "Console Output" step inside the current test case
            String stepUuid = UUID.randomUUID().toString();
            StepResult stepResult = new StepResult();
            stepResult.setName(emoji + " Console Output");
            stepResult.setStatus(Status.PASSED);
            Allure.getLifecycle().startStep(stepUuid, stepResult);

            // Attach the full console log to this step — visible by clicking the step
            Allure.addAttachment("Console Log", "text/plain",
                    new ByteArrayInputStream(logBytes), "log");

            Allure.getLifecycle().stopStep(stepUuid);

        } catch (Exception e) {
            System.err.println("[AllureAttachmentListener] Failed to attach log step: " + e.getMessage());
        }
    }

    private void attachFailureScreenshot() {
        try {
            com.microsoft.playwright.Page page = BaseTest.CURRENT_PAGE.get();
            if (page != null) {
                byte[] screenshot = page.screenshot();
                Allure.addAttachment("❌ Failure Screenshot", "image/png",
                        new ByteArrayInputStream(screenshot), "png");
            }
        } catch (Exception e) {
            System.err.println("[AllureAttachmentListener] Failed to attach failure screenshot: " + e.getMessage());
        }
    }
}
