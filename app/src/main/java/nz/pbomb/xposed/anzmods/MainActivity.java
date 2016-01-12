package nz.pbomb.xposed.anzmods;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;

import common.GLOBAL;
import common.PACKAGES;
import common.PREFERENCES;
import common.XPOSED_STRINGS;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onCreateValidation();
        createDisclaimerDialog();

        if(GLOBAL.DEBUG) {
            setTitle(getTitle() + " (Debug Mode)");
        }
    }

    /**
     * Validates operations when the application is created. Firstly check that the
     * SharedPreferences exist and if it doesn't then create the SharedPreferences accordingly.
     * Also checks whether either ANZ or Semble (any version) is installed
     */
    private void onCreateValidation() {
        // Get the preference fragment displayed on this activity
        PreferenceFragment preferenceFragment = (PreferenceFragment) getFragmentManager().findFragmentById(R.id.mainPrefFragment);
        // Get the SharedPreferences for this module (and produce and editor as well)
        SharedPreferences sharedPref = getSharedPreferences(PREFERENCES.SHARED_PREFS_FILE_NAME, Context.MODE_WORLD_READABLE);
        SharedPreferences.Editor sharedPrefEditor = sharedPref.edit();


        // Create the SharedPreferences and set the defaults if they aren't already created
        if(!sharedPref.contains(PREFERENCES.KEYS.ANZ.ROOT_DETECTION)) {
            sharedPrefEditor.putBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.ANZ.ROOT_DETECTION);
            sharedPrefEditor.putBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, PREFERENCES.DEFAULT_VALUES.ANZ.SPOOF_DEVICE);
            sharedPrefEditor.putBoolean(PREFERENCES.KEYS.SEMBLE.ROOT_DETECTION, PREFERENCES.DEFAULT_VALUES.SEMBLE.ROOT_DETECTION);

            sharedPrefEditor.apply();
        }

        // Checks if ANZ GoMoney is installed and if its not disable the preference option in the
        // fragment and disable any related modifications
        if(!isANZGoMoneyInstalled()) {
            sharedPrefEditor.putBoolean(PREFERENCES.KEYS.ANZ.ROOT_DETECTION, false);
            sharedPrefEditor.putBoolean(PREFERENCES.KEYS.ANZ.SPOOF_DEVICE, false);
            sharedPrefEditor.apply();

            preferenceFragment.findPreference(PREFERENCES.KEYS.MAIN.ANZ).setEnabled(false);
        }

        // Checks if Semble is installed and if its not disable the preference option in the
        // fragment and disable any related modifications
        if(!isSembleInstalled()) {
            sharedPrefEditor.putBoolean(PREFERENCES.KEYS.SEMBLE.ROOT_DETECTION, false);
            sharedPrefEditor.apply();

            preferenceFragment.findPreference(PREFERENCES.KEYS.MAIN.SEMBLE).setEnabled(false);
        }

        // Checks if TVNZ is installed and if its not disable the preference option in the
        // fragment and disable any related modifications
        if(!isTVNZOnDemandInstalled()) {
            preferenceFragment.findPreference(PREFERENCES.KEYS.MAIN.TVNZ).setEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);  // Creates menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Determine which MenuItem was pressed and act accordingly based on the button pressed
        int id = item.getItemId();
        Intent intent = null;
        switch (id) {
            case R.id.action_help:
                intent = new Intent(getApplicationContext(), HelpActivity.class);
                break;
            case R.id.action_donate:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=QNQDESEMGWDPY"));
                break;
            case R.id.action_contact:
                intent = new Intent(getApplicationContext(), ContactActivity.class);
                break;
            case R.id.action_sourceCode:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/pbombnz/SuperKiwi"));
                break;
            case R.id.action_xda:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/xposed/modules/xposed-anz-gomoney-zealand-mods-bypass-t3270623"));
                break;
            case R.id.action_about:
                intent = new Intent(getApplicationContext(), AboutActivity.class);
                break;
        }
        startActivity(intent);
        return true;
    }

    /**
     * Checks if the ANZ GoMoney Application is installed.
     *
     * @return True if it is installed, otherwise return false.
     */
    private boolean isANZGoMoneyInstalled() {
        return isApplicationInstalled(PACKAGES.ANZ_GOMONEY);
    }

    /**
     *  Checks if any of the Semble Application variants (from either 2Degrees, Spark or Vodafone
     *  are installed.
     *
     * @return True if it is installed, otherwise return false.
     */
    private boolean isSembleInstalled() {
        return isApplicationInstalled(PACKAGES.SEMBLE_2DEGREES) || isApplicationInstalled(PACKAGES.SEMBLE_SPARK) || isApplicationInstalled(PACKAGES.SEMBLE_VODAFONE);
    }

    /**
     *  Checks if any of the TVNZ onDemand Application is installed.
     *
     * @return True if it is installed, otherwise return false.
     */
    private boolean isTVNZOnDemandInstalled() {
        return isApplicationInstalled(PACKAGES.TVNZ_ONDEMAND);
    }

    /**
     * Determines if an application is installed or not
     *
     * @param uri The package name of the application
     * @return True, if the package is installed, otherwise false
     */
    private boolean isApplicationInstalled(String uri) {
        PackageManager pm = getPackageManager();
        boolean appInstalled;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            appInstalled = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            appInstalled = false;
        }
        return appInstalled;
    }

    private void createDisclaimerDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setCancelable(false);
        alertDialog.setTitle(XPOSED_STRINGS.DISCLAIMER_TITLE);
        alertDialog.setMessage(XPOSED_STRINGS.DISCLAIMER_SUMMARY);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        new File("/data/data/"+ PACKAGES.MODULE + "/shared_prefs/" + PREFERENCES.SHARED_PREFS_FILE_NAME + ".xml").setReadable(true,false);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        new File("/data/data/"+ PACKAGES.MODULE + "/shared_prefs/" + PREFERENCES.SHARED_PREFS_FILE_NAME + ".xml").setReadable(true,false);
        super.onPause();
    }
}
