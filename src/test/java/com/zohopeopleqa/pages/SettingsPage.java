package com.zohopeopleqa.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

/**
 * Page Object for the Zoho People Settings (Admin) page.
 * Encapsulates selectors and actions for the admin settings grid.
 */
public class SettingsPage {

    private final Page page;

    // Selectors confirmed via DOM inspection
    public static final String SETTINGS_ICON     = "#zp_maintab_admin";
    public static final String SERVICE_CONTAINER = "#servicPageContainer";
    public static final String PAGE_WRAPPER      = "#page-wrapper";

    public SettingsPage(Page page) {
        this.page = page;
    }

    /** Navigate to Settings by clicking the gear icon */
    public void navigateTo() {
        page.click(SETTINGS_ICON);
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));
        page.waitForSelector(SERVICE_CONTAINER, new Page.WaitForSelectorOptions().setTimeout(10000));
    }

    /** Wait for the settings grid to be visible */
    public void waitForGrid() {
        page.waitForSelector(SERVICE_CONTAINER, new Page.WaitForSelectorOptions().setTimeout(10000));
    }

    /** Return true if the named tile is present in the service grid */
    public boolean isTilePresent(String tileName) {
        return page.locator(SERVICE_CONTAINER + " h5:has-text('" + tileName + "')").count() > 0;
    }

    /** Get the tile heading text */
    public String getTileText(String tileName) {
        return page.locator(SERVICE_CONTAINER + " h5:has-text('" + tileName + "')").innerText().trim();
    }

    /** Click a service tile by name */
    public void clickTile(String tileName) {
        page.locator(SERVICE_CONTAINER + " a:has(h5:has-text('" + tileName + "'))").first().click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));
    }

    /** Count all service tiles in the grid */
    public int getTileCount() {
        return page.locator(SERVICE_CONTAINER + " h5").count();
    }

    /** Return the current page URL */
    public String getCurrentUrl() {
        return page.url();
    }
}
