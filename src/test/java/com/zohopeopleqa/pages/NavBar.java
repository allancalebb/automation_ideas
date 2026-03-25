package com.zohopeopleqa.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

/**
 * Page Object for the Zoho People top navigation bar.
 * Shared by all pages — encapsulates the main-tab navigation selectors and actions.
 */
public class NavBar {

    private final Page page;

    // Main navigation tab selectors (confirmed via DOM inspection)
    public static final String HOME_TAB       = "#zp_maintab_home";
    public static final String LEAVE_TAB      = "#zp_maintab_leavetracker";
    public static final String REPORTS_TAB    = "#zp_maintab_reports";
    public static final String SETTINGS_TAB   = "#zp_maintab_admin";
    public static final String SEARCH_ICON    = "#zpeople_search_icon";
    public static final String NOTIF_ICON     = "#zp_feeds_notifications";

    public NavBar(Page page) {
        this.page = page;
    }

    public void goToHome() {
        page.locator(HOME_TAB).first().click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));
    }

    public void goToLeaveTracker() {
        page.locator(LEAVE_TAB).first().click();
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));
    }

    public void goToReports() {
        page.click(REPORTS_TAB);
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));
    }

    public void goToSettings() {
        page.click(SETTINGS_TAB);
        page.waitForLoadState(LoadState.LOAD, new Page.WaitForLoadStateOptions().setTimeout(15000));
    }

    public boolean isHomeTabVisible()     { return page.isVisible(HOME_TAB); }
    public boolean isLeaveTabVisible()    { return page.isVisible(LEAVE_TAB); }
    public boolean isReportsTabVisible()  { return page.isVisible(REPORTS_TAB); }
    public boolean isSettingsTabVisible() { return page.isVisible(SETTINGS_TAB); }
}
