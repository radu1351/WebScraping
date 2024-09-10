package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CbsScript {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final Logger logger = Logger.getLogger(CbsScript.class.getName());

    public static void initializeDriver(String pdfDownloadLocation) {
        ChromeOptions options = new ChromeOptions();
        HashMap<String, Object> chromeOptionsMap = new HashMap<String, Object>();
        chromeOptionsMap.put("plugins.always_open_pdf_externally", true);
        chromeOptionsMap.put("download.default_directory", pdfDownloadLocation);
        chromeOptionsMap.put("download.prompt_for_download", false);
        chromeOptionsMap.put("plugins.plugins_disabled", new String[]{"Chrome PDF Viewer"});
        chromeOptionsMap.put("profile.default_content_setting_values.automatic_downloads", 1);
        chromeOptionsMap.put("profile.default_content_setting_values.notifications", 1);
        chromeOptionsMap.put("download.safebrowsing.enabled", true);
        options.setExperimentalOption("prefs", chromeOptionsMap);
        // options.addArguments("--incognito");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-search-engine-choice-screen");
        options.addArguments("disable-features=DownloadBubble,DownloadBubbleV2");
        // options.addArguments("--headless");

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
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("navbar")));
            logger.info("Login successful.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred during the login process", e);
        }
    }

    public static void downloadSY125PDF(String id, String password, String pdfDownloadLocation, String url) {
        try {
            initializeDriver(pdfDownloadLocation);
            login(id, password, url);

            WebElement navbarItem = driver.findElement(By.id("navbarItem/?SPNavigationNodeId=2080"));
            navbarItem.click();
            logger.info("Navbar item clicked successfully.");
            pause(500);

            WebElement actualitesButton = driver.findElement(By.cssSelector("a[href='/Pages/actualites.aspx']"));
            actualitesButton.click();
            logger.info("'Actualites' button clicked successfully.");
            pause(500);

            WebElement dropdownElement = driver.findElement(By.name("ctl00$ctl38$g_c1a26a2b_1554_4618_9935_f1fd2e02fc13$drpListThematic"));
            Select dropdown = new Select(dropdownElement);
            pause(500);
            dropdown.selectByIndex(1);
            logger.info("Dropdown option selected successfully.");
            pause(500);

            WebElement SY125 = driver.findElement(By.cssSelector("a[href='https://cbs.cegedim-portal.com/Pages/News/Nouvelle-release-SY-business-1-25.aspx']"));
            SY125.click();
            logger.info("SY125 link clicked successfully.");
            pause(500);

            downloadPdfFromArticle();

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

    public static void downloadPdfFromFactures(String id, String password, String pdfDownloadLocation, String url, String date) {
        try {
            initializeDriver(pdfDownloadLocation);
            login(id, password, url);

            WebElement navbarItem = driver.findElement(By.xpath("//*[@id=\"navbarItem/?SPNavigationNodeId=2080\"]"));
            navbarItem.click();
            logger.info("Navbar item clicked successfully.");
            // pause(500);

            WebElement actualitesButton = driver.findElement(By.xpath("//*[@id=\"Navigation\"]/ul/li[1]/ul/li[1]/a"));
            actualitesButton.click();
            logger.info("'Actualites' button clicked successfully.");
            //  pause(500);

            WebElement dropdownElementCategory = driver.findElement(By.xpath("//*[@id=\"ctl00_ctl38_g_c1a26a2b_1554_4618_9935_f1fd2e02fc13_drpListThematic\"]"));
            Select dropdownCategory = new Select(dropdownElementCategory);
            //  pause(500);
            dropdownCategory.selectByIndex(1);
            logger.info("Dropdown category selected successfully.");
            //  pause(500);

            if (date != null) {
                WebElement dropdownElementDate = driver.findElement(By.xpath("//*[@id=\"ctl00_ctl38_g_c1a26a2b_1554_4618_9935_f1fd2e02fc13_drpListYears\"]"));
                Select dropdownDate = new Select(dropdownElementDate);
                //     pause(500);
                dropdownDate.selectByVisibleText(date);
                logger.info("Dropdown date selected successfully.");
                //     pause(500);
            }

            List<WebElement> articles = driver.findElements(By.cssSelector(".col-sm-3.col-flex.article"));
            for (WebElement article : articles) {
                try {
                    String clicklnk = Keys.chord(Keys.CONTROL, Keys.RETURN);
                    article.findElement(By.tagName("a")).sendKeys(clicklnk);
                    logger.info("Opened new tab for article");
                } catch (Exception e) {
                    logger.warning("Failed to open tab for an article: " + e.getMessage());
                }
            }

            List<String> tabs = new ArrayList<>(driver.getWindowHandles());
            for (String tab : tabs) {
                driver.switchTo().window(tab);
                try {
                    downloadPdfFromArticle();
                    logger.info("Processed article in tab: " + driver.getTitle());
                } catch (Exception e) {
                    logger.warning("Failed to process article in tab: " + driver.getTitle() + ". Error: " + e.getMessage());
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

    private static void downloadPdfFromArticle() {
        try {
            List<WebElement> links = driver.findElements(By.tagName("a"));
            for (WebElement link : links) {
                String href = link.getAttribute("href");
                if (href != null && href.toLowerCase().contains(".pdf")) {
                    logger.info("Found PDF link: " + href);
                    link.click();
                    logger.info("Clicked PDF link to initiate download");
                    pause(3000);
                    logger.info("PDF download completed");
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
        String url = args.length > 3 ? args[3] : "https://cbs.cegedim-portal.com/Pages/home.aspx";
        String date = args.length > 4 ? args[4] : null;

        downloadPdfFromFactures(id, password, pdfDownloadLocation, url, date);
    }
}