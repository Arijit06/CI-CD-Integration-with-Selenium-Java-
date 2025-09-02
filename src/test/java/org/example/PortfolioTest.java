package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.example.utils.ScreenshotUtil;

import java.time.Duration;

public class PortfolioTest {
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static final ThreadLocal<WebDriverWait> wait = new ThreadLocal<>();
    private static final String BASE_URL = "https://arijit06.github.io/ArijitSinghaRoy/";

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        String headless = System.getProperty("headless");
        if ("true".equalsIgnoreCase(headless)) {
            options.addArguments("--headless");
            options.addArguments("--window-size=1920,1080");
        }
        WebDriver webDriver = new ChromeDriver(options);
        webDriver.manage().window().maximize();
        driver.set(webDriver);
        wait.set(new WebDriverWait(webDriver, Duration.ofSeconds(10)));
    }

    @Test
    public void testHomePageLoads() {
        driver.get().get(BASE_URL);
        driver.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        ScreenshotUtil.takeScreenshot(driver.get(), "homepage");
        String title = driver.get().getTitle();
        Assert.assertTrue(title.contains("Arijit Singha Roy"), "Title should contain the name");
    }

    @Test
    public void testNavigationLinks() {
        driver.get().get(BASE_URL);
        driver.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        ScreenshotUtil.takeScreenshot(driver.get(), "navigation_start");
        // Test Home section
        WebElement aboutLink = wait.get().until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Home')]")));
        aboutLink.click();
        driver.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        ScreenshotUtil.takeScreenshot(driver.get(), "navigation_home_clicked");
        // Test Skills section
        WebElement skillsLink = wait.get().until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Portfolio')]")));
        skillsLink.click();
        driver.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        ScreenshotUtil.takeScreenshot(driver.get(), "navigation_portfolio_clicked");
        // Test Projects section
        WebElement projectsLink = wait.get().until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Contact')]")));
        projectsLink.click();
        ScreenshotUtil.takeScreenshot(driver.get(), "navigation_contact_clicked");
    }

    @Test
    public void testSocialMediaLinks() {
        driver.get().get(BASE_URL);
        // Test LinkedIn link
        WebElement linkedinLink = wait.get().until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'linkedin.com')]")));
        String linkedinHref = linkedinLink.getAttribute("href");
        Assert.assertTrue(linkedinHref.contains("linkedin.com"), "LinkedIn link should be present");
        // Test GitHub link
        WebElement githubLink = wait.get().until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'github.com')]")));
        String githubHref = githubLink.getAttribute("href");
        Assert.assertTrue(githubHref.contains("github.com"), "GitHub link should be present");
    }

    @AfterMethod
    public void tearDown() {
        if (driver.get() != null) {
            driver.get().quit();
            driver.remove();
            wait.remove();
        }
    }
}
