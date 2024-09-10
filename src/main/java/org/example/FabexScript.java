package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FabexScript {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final Logger logger = Logger.getLogger(FabexScript.class.getName());

    public static void initializeDriver(String pdfDownloadLocation) {
        ChromeOptions options = new ChromeOptions();
        HashMap<String, Object> chromeOptionsMap = new HashMap<String, Object>();
        chromeOptionsMap.put("plugins.always_open_pdf_externally", true);
        chromeOptionsMap.put("download.default_directory", pdfDownloadLocation);
        chromeOptionsMap.put("download.prompt_for_download", false);
        chromeOptionsMap.put("plugins.plugins_disabled", new String[]{"Chrome PDF Viewer"});
        options.setExperimentalOption("prefs", chromeOptionsMap);
        //options.addArguments("--incognito");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-search-engine-choice-screen");
        //options.addArguments("--headless");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    public static void login(String id, String password, String url) {
        try {
            String authUrl = "https://" + id + ":" + password + "@" + url.replace("https://", "");
            logger.info("Navigating to authentication URL");
            driver.get(authUrl);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("imageArea_a6b2ca2a")));
            logger.info("Login successful.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred during the login process", e);
        }
    }

    public static void downloadPdfFromDocuments(String id, String password, String pdfDownloadLocation, String url, String date) {

        try {
            initializeDriver(pdfDownloadLocation);
            login(id, password, url);

            driver.findElement(By.xpath("//a[@href='/workspaces/fabex/SitePages/Les-projets-de-la-FABEX.aspx']")).click();
            driver.findElement(By.xpath("//a[@href='/workspaces/fabex/Shared Documents']")).click();

            List<WebElement> folders = driver.findElements(By.xpath("//div[contains(@class, 'ms-DetailsRow-fields')]" +
                    "[.//img[contains(@src, 'folder.svg')]]" +
                    "//a[contains(@class, 'ms-Link')]"));

            for (WebElement folder : folders) {
                try {
                    String href = folder.getAttribute("href");
                    ((JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", href);
                } catch (Exception e) {
                    logger.warning("Failed to open tab for an article: " + e.getMessage());
                }

            }

            List<String> tabs = new ArrayList<>(driver.getWindowHandles());
            for (String tab : tabs) {
                driver.switchTo().window(tab);
                try {
                    downloadPdfFromFolder();
                    logger.info("Processed folder in tab: " + driver.getTitle());
                } catch (Exception e) {
                    logger.warning("Failed to process folder in tab: " + driver.getTitle() + ". Error: " + e.getMessage());
                }
                finally {
                    driver.close();
                }
            }
            logger.info("PDF download process completed successfully.");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred during the PDF download process", e);
        } finally {
            if (driver != null) {
                driver.quit();
                logger.info("WebDriver quit successfully.");
            }
        }
    }

    private static void downloadPdfFromFolder() {
        try {
            List<WebElement> links = driver.findElements(By.tagName("a"));
            for (WebElement link : links) {
                String href = link.getAttribute("href");
                if (href != null && href.toLowerCase().contains(".pdf")) {
                    logger.info("Found PDF link: " + href);
                    link.click();
                    logger.info("Clicked PDF link to initiate download.");
                    pause(5000);
                    return;
                }
            }
            logger.info("No PDF link found in this article: " + driver.getTitle());
        } catch (Exception e) {
            logger.warning("Error while trying to find or download PDF: " + e.getMessage());
        }
    }

    public static void pause(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Thread was interrupted during sleep");
        }
    }


    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java SeleniumScript <id> <password> <pdfDownloadLocation> [url] [date]");
            System.exit(1);
        }

        String id = args[0];
        String password = args[1];
        String pdfDownloadLocation = args[2];
        String url = args.length > 3 ? args[3] : "https://cbs.cegedim-portal.com/workspaces/fabex";
        String date = args.length > 4 ? args[4] : null;

        downloadPdfFromDocuments(id, password, pdfDownloadLocation, url, date);
    }
}
