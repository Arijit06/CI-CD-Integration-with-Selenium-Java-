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
    private WebDriver driver;
    private WebDriverWait wait;
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
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Test
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        ScreenshotUtil.takeScreenshot(driver, "homepage");
        String title = driver.getTitle();
        Assert.assertTrue(title.contains("Arijit Singha Roy"), "Title should contain the name");
    }

    @Test
    public void testNavigationLinks() {
        driver.get(BASE_URL);
        ScreenshotUtil.takeScreenshot(driver, "navigation_start");
        // Test Home section
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Home')]")));
        aboutLink.click();
        ScreenshotUtil.takeScreenshot(driver, "navigation_home_clicked");
        // Test Skills section
        WebElement skillsLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Portfolio')]")));
        skillsLink.click();
        ScreenshotUtil.takeScreenshot(driver, "navigation_portfolio_clicked");
        // Test Projects section
        WebElement projectsLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Contact')]")));
        projectsLink.click();
        ScreenshotUtil.takeScreenshot(driver, "navigation_contact_clicked");
    }

    @Test
    public void testSocialMediaLinks() {
        driver.get(BASE_URL);
        
        // Test LinkedIn link
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'linkedin.com')]")));
        String linkedinHref = linkedinLink.getAttribute("href");
        Assert.assertTrue(linkedinHref.contains("linkedin.com"), "LinkedIn link should be present");

        // Test GitHub link
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href,'github.com')]")));
        String githubHref = githubLink.getAttribute("href");
        Assert.assertTrue(githubHref.contains("github.com"), "GitHub link should be present");
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
