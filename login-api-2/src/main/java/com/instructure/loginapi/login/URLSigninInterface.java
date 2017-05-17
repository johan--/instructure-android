package com.instructure.loginapi.login;

@Deprecated
public interface URLSigninInterface{
    public void handleNightlyBuilds();
    public void refreshWidgets();
    public void deleteCachedFiles();
    public String getUserAgent();
    public int getRootLayout();
    public boolean shouldShowHelpButton();
}