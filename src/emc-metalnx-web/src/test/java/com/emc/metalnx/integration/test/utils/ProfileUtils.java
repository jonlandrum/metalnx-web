package com.emc.metalnx.integration.test.utils;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.emc.metalnx.test.generic.UITest;

public class ProfileUtils {
    public static void accessAddNewProfileForm(WebDriver driver) {
        driver.get(UITest.PROFILES_URL);
        WebDriverWait wait = new WebDriverWait(driver, 8);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("addProfileButton")));

        driver.findElement(By.id("addProfileButton")).click();
    }

    public static void fillProfileForm(String name, String description, List<String> groupsNames, WebDriver driver) {
        driver.findElement(By.id("inputProfileName")).sendKeys(name);
        driver.findElement(By.id("inputDescription")).sendKeys(description);

        if (groupsNames != null) {
            for (String groupName : groupsNames) {
                driver.findElement(By.cssSelector("#groupsListTable_filter input")).clear();
                driver.findElement(By.cssSelector("#groupsListTable_filter input")).sendKeys(groupName);
                driver.findElement(By.id("chk_" + groupName)).click();
            }
        }
    }

    public static void submitProfileForm(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, 8);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("submitProfileFormBtn")));

        driver.findElement(By.id("submitProfileFormBtn")).click();
    }

    public static void addUserProfile(String name, String description, List<String> groupsNames, WebDriver driver) {
        driver.get(UITest.ADD_PROFILES_URL);
        WebDriverWait wait = new WebDriverWait(driver, 8);

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("inputProfileName")));

        fillProfileForm(name, description, groupsNames, driver);
        submitProfileForm(driver);
    }

    public static boolean isProfileInList(String name, WebDriver driver) {
        if (!driver.getCurrentUrl().equals(UITest.PROFILES_URL)) {
            driver.get(UITest.PROFILES_URL);

        }
        WebDriverWait wait = new WebDriverWait(driver, 8);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("userProfilesListTable")));

        List<WebElement> profilesNamesElements = driver.findElements(By.className("col-profile-name"));

        for (WebElement e : profilesNamesElements) {
            if (e.getText().equals(name)) {
                return true;
            }
        }

        return false;
    }

    public static void clickOnRemoveIcon(String name, WebDriver driver) {
        if (!driver.getCurrentUrl().equals(UITest.PROFILES_URL)) {
            driver.get(UITest.PROFILES_URL);

        }
        WebDriverWait wait = new WebDriverWait(driver, 8);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("userProfilesListTable")));

        driver.findElement(By.id("btn_removal_" + name)).click();

        wait.until(ExpectedConditions.elementToBeClickable(By.id("btnConfProfileRemoval_Yes")));
    }

    public static void removeProfile(String name, WebDriver driver) {
        if (isProfileInList(name, driver)) {
            clickOnRemoveIcon(name, driver);
            driver.findElement(By.id("btnConfProfileRemoval_Yes")).click();
        }
    }

    public static boolean errorMessageIsDisplayed(String errorMessageElementId, WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, 8);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id(errorMessageElementId)));

        WebElement invalidMsg = driver.findElement(By.id(errorMessageElementId));
        return invalidMsg.isDisplayed();
    }
}