package api.utilities;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class ExtentReportManager implements ITestListener {
    public ExtentSparkReporter sparkReporter;
    public ExtentReports extent;
    public ExtentTest test;

    @Override
    public void onStart(ITestContext testContext) {
        new File("./reports").mkdirs();
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        sparkReporter = new ExtentSparkReporter("./reports/Alpaca-API-Report-" + timeStamp + ".html");
        sparkReporter.config().setDocumentTitle("Alpaca Paper Trading API Report");
        sparkReporter.config().setReportName("Order Management & Execution Suite");
        sparkReporter.config().setTheme(Theme.DARK);

        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);
        extent.setSystemInfo("Environment", "Alpaca Paper Sandbox (v2)");
        extent.setSystemInfo("Framework", "REST Assured + TestNG + Jackson");
        extent.setSystemInfo("User", System.getProperty("user.name"));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getName();
        Object[] parameters = result.getParameters();
        if (parameters != null && parameters.length > 0) {
            testName += " [Data: " + Arrays.toString(parameters) + "]";
        }
        test = extent.createTest(testName);
        test.assignCategory(result.getMethod().getGroups());
        test.log(Status.PASS, "Order Lifecycle Iteration Passed");
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                test.info("Parameter [" + i + "]: " + parameters[i]);
            }
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getName();
        Object[] parameters = result.getParameters();
        if (parameters != null && parameters.length > 0) {
            testName += " [Data: " + Arrays.toString(parameters) + "]";
        }
        test = extent.createTest(testName);
        test.assignCategory(result.getMethod().getGroups());
        test.log(Status.FAIL, "Order Lifecycle Iteration Failed");
        if (result.getThrowable() != null) {
            test.log(Status.FAIL, result.getThrowable().getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        test = extent.createTest(result.getName());
        test.log(Status.SKIP, "Test Skipped");
    }

    @Override
    public void onFinish(ITestContext testContext) {
        if (extent != null) {
            extent.flush();
        }
    }
}