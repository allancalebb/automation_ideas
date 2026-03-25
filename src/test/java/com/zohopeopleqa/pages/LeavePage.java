package com.zohopeopleqa.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

/**
 * Page Object for the Zoho People Leave Tracker page.
 * Encapsulates selectors and actions for leave management.
 */
public class LeavePage {

    private final Page page;

    // Selectors confirmed via DOM inspection
    public static final String LEAVE_TAB      = "#zp_maintab_leavetracker";
    public static final String SUBTAB_CONTAINER = "#tltabcontainer";
    public static final String APPLY_LEAVE_BTN  = "button.ZP-Add";
    public static final String ADD_REQUEST_BTN  = "button:has-text('Add Request')";
    public static final String ASSIGN_SHIFT_BTN = "button:has-text('Assign shift')";

    public LeavePage(Page page) {
        this.page = page;
    }

    /** Navigate to Leave Tracker via main nav tab */
    public void navigateTo() {
        page.locator(LEAVE_TAB).first().click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));
        page.waitForSelector(SUBTAB_CONTAINER, new Page.WaitForSelectorOptions().setTimeout(10000));
    }

    /** Click a top-level tab by display text */
    public void clickTopTab(String tabText) {
        page.locator("#tltabcontainer a:has-text('" + tabText + "')").first().click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));
    }

    public boolean isSubTabContainerVisible() {
        return page.isVisible(SUBTAB_CONTAINER);
    }

    public boolean isApplyLeaveButtonVisible() {
        return page.isVisible(APPLY_LEAVE_BTN);
    }

    public String getApplyLeaveButtonText() {
        return page.locator(APPLY_LEAVE_BTN).innerText().trim();
    }

    public String getCurrentUrl() {
        return page.url();
    }
}
