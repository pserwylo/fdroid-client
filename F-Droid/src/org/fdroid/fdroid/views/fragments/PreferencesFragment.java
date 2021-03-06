package org.fdroid.fdroid.views.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.v4.preference.PreferenceFragment;
import android.text.Html;
import android.text.TextUtils;

import org.fdroid.fdroid.FDroidApp;
import org.fdroid.fdroid.Preferences;
import org.fdroid.fdroid.PreferencesActivity;
import org.fdroid.fdroid.R;
import org.fdroid.fdroid.Utils;
import org.fdroid.fdroid.installer.InstallIntoSystemDialogActivity;
import org.fdroid.fdroid.installer.Installer;

import java.util.Locale;

public class PreferencesFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String[] summariesToUpdate = {
        Preferences.PREF_UPD_INTERVAL,
        Preferences.PREF_UPD_WIFI_ONLY,
        Preferences.PREF_UPD_NOTIFY,
        Preferences.PREF_UPD_HISTORY,
        Preferences.PREF_ROOTED,
        Preferences.PREF_INCOMP_VER,
        Preferences.PREF_THEME,
        Preferences.PREF_COMPACT_LAYOUT,
        Preferences.PREF_IGN_TOUCH,
        Preferences.PREF_LOCAL_REPO_BONJOUR,
        Preferences.PREF_LOCAL_REPO_NAME,
        Preferences.PREF_LOCAL_REPO_HTTPS,
        Preferences.PREF_LANGUAGE,
        Preferences.PREF_CACHE_APK,
        Preferences.PREF_EXPERT,
        Preferences.PREF_SYSTEM_INSTALLER,
        Preferences.PREF_ENABLE_PROXY,
        Preferences.PREF_PROXY_HOST,
        Preferences.PREF_PROXY_PORT,
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    protected void checkSummary(String key, int resId) {
        CheckBoxPreference pref = (CheckBoxPreference)findPreference(key);
        pref.setSummary(resId);
    }

    protected void entrySummary(String key) {
        ListPreference pref = (ListPreference)findPreference(key);
        pref.setSummary(pref.getEntry());
    }

    protected void textSummary(String key, int resId) {
        EditTextPreference pref = (EditTextPreference)findPreference(key);
        pref.setSummary(getString(resId, pref.getText()));
    }

    protected void updateSummary(String key, boolean changing) {

        int result = 0;

        switch (key) {
        case Preferences.PREF_UPD_INTERVAL:
            ListPreference listPref = (ListPreference)findPreference(
                    Preferences.PREF_UPD_INTERVAL);
            int interval = Integer.parseInt(listPref.getValue());
            Preference onlyOnWifi = findPreference(
                    Preferences.PREF_UPD_WIFI_ONLY);
            onlyOnWifi.setEnabled(interval > 0);
            if (interval == 0) {
                listPref.setSummary(R.string.update_interval_zero);
            } else {
                listPref.setSummary(listPref.getEntry());
            }
            break;

        case Preferences.PREF_UPD_WIFI_ONLY:
            checkSummary(key, R.string.automatic_scan_wifi_on);
            break;

        case Preferences.PREF_UPD_NOTIFY:
            checkSummary(key, R.string.notify_on);
            break;

        case Preferences.PREF_UPD_HISTORY:
            textSummary(key, R.string.update_history_summ);
            break;

        case Preferences.PREF_COMPACT_LAYOUT:
            checkSummary(key, R.string.compactlayout_on);
            break;

        case Preferences.PREF_THEME:
            entrySummary(key);
            if (changing) {
                result |= PreferencesActivity.RESULT_RESTART;
                getActivity().setResult(result);
            }
            break;

        case Preferences.PREF_INCOMP_VER:
            checkSummary(key, R.string.show_incompat_versions_on);
            break;

        case Preferences.PREF_ROOTED:
            checkSummary(key, R.string.rooted_on);
            break;

        case Preferences.PREF_IGN_TOUCH:
            checkSummary(key, R.string.ignoreTouch_on);
            break;

        case Preferences.PREF_LOCAL_REPO_BONJOUR:
            checkSummary(key, R.string.local_repo_bonjour_on);
            break;

        case Preferences.PREF_LOCAL_REPO_NAME:
            textSummary(key, R.string.local_repo_name_summary);
            break;

        case Preferences.PREF_LOCAL_REPO_HTTPS:
            checkSummary(key, R.string.local_repo_https_on);
            break;

        case Preferences.PREF_LANGUAGE:
            langSpinner(key);
            entrySummary(key);
            if (changing) {
                result |= PreferencesActivity.RESULT_RESTART;
                getActivity().setResult(result);
                FDroidApp.updateLanguage(this.getActivity());
            }
            break;

        case Preferences.PREF_CACHE_APK:
            checkSummary(key, R.string.cache_downloaded_on);
            break;

        case Preferences.PREF_EXPERT:
            checkSummary(key, R.string.expert_on);
            break;

        case Preferences.PREF_SYSTEM_INSTALLER:
            checkSummary(key, R.string.system_installer_on);
            break;

        case Preferences.PREF_ENABLE_PROXY:
            CheckBoxPreference checkPref = (CheckBoxPreference) findPreference(key);
            checkPref.setSummary(R.string.enable_proxy_summary);
            break;

        case Preferences.PREF_PROXY_HOST:
            EditTextPreference textPref = (EditTextPreference) findPreference(key);
            String text = Preferences.get().getProxyHost();
            if (TextUtils.isEmpty(text) || text.equals(Preferences.DEFAULT_PROXY_HOST))
                textPref.setSummary(R.string.proxy_host_summary);
            else
                textPref.setSummary(text);
            break;

        case Preferences.PREF_PROXY_PORT:
            EditTextPreference textPref2 = (EditTextPreference) findPreference(key);
            int port = Preferences.get().getProxyPort();
            if (port == Preferences.DEFAULT_PROXY_PORT)
                textPref2.setSummary(R.string.proxy_port_summary);
            else
                textPref2.setSummary(String.valueOf(port));
            break;

        }
    }

    /**
     * Initializes SystemInstaller preference, which can only be enabled when F-Droid is installed as a system-app
     */
    protected void initSystemInstallerPreference() {
        CheckBoxPreference pref = (CheckBoxPreference) findPreference(Preferences.PREF_SYSTEM_INSTALLER);

        // we are handling persistence ourself!
        pref.setPersistent(false);

        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                final CheckBoxPreference pref = (CheckBoxPreference) preference;

                if (pref.isChecked()) {
                    if (Installer.hasSystemPermissions(getActivity(), getActivity().getPackageManager())) {
                        // system-permission are granted, i.e. F-Droid is a system-app
                        SharedPreferences.Editor editor = pref.getSharedPreferences().edit();
                        editor.putBoolean(Preferences.PREF_SYSTEM_INSTALLER, true);
                        editor.commit();
                        pref.setChecked(true);
                    } else {
                        // system-permission not available
                        SharedPreferences.Editor editor = pref.getSharedPreferences().edit();
                        editor.putBoolean(Preferences.PREF_SYSTEM_INSTALLER, false);
                        editor.commit();
                        pref.setChecked(false);

                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
                        alertBuilder.setTitle(R.string.system_permission_denied_title);
                        String message = getActivity().getString(R.string.system_permission_denied_body) +
                                "<br/><br/>";
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            message += getActivity().getString(R.string.system_install_question_lollipop);
                        } else {
                            message += getActivity().getString(R.string.system_install_question);
                        }
                        alertBuilder.setMessage(Html.fromHtml(message));
                        alertBuilder.setPositiveButton(R.string.system_permission_install_via_root, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent installIntent = new Intent(getActivity(), InstallIntoSystemDialogActivity.class);
                                installIntent.setAction(InstallIntoSystemDialogActivity.ACTION_INSTALL);
                                startActivity(installIntent);
                            }
                        });
                        alertBuilder.setNegativeButton(R.string.cancel, null);
                        alertBuilder.create().show();
                    }
                } else {
                    SharedPreferences.Editor editor = pref.getSharedPreferences().edit();
                    editor.putBoolean(Preferences.PREF_SYSTEM_INSTALLER, false);
                    editor.commit();
                    pref.setChecked(false);
                }

                return true;
            }
        });
    }

    protected void initUninstallSystemAppPreference() {
        Preference pref = findPreference(Preferences.PREF_UNINSTALL_SYSTEM_APP);
        pref.setPersistent(false);

        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent uninstallIntent = new Intent(getActivity(), InstallIntoSystemDialogActivity.class);
                uninstallIntent.setAction(InstallIntoSystemDialogActivity.ACTION_UNINSTALL);
                startActivity(uninstallIntent);

                return true;
            }
        });
    }

    private void langSpinner(String key) {
        final ListPreference pref = (ListPreference)findPreference(key);
        final String[] langValues = getResources().getStringArray(R.array.languageValues);
        String[] langNames = new String[langValues.length];
        langNames[0] = getString(R.string.pref_language_default);
        for (int i = 1; i < langValues.length; i++) {
            final Locale appLoc = Utils.getLocaleFromAndroidLangTag(langValues[i]);
            langNames[i] = appLoc == null ? langValues[i] : appLoc.getDisplayName(appLoc);
        }
        pref.setEntries(langNames);
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        for (final String key : summariesToUpdate) {
            updateSummary(key, false);
        }

        initSystemInstallerPreference();
        initUninstallSystemAppPreference();
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(
            SharedPreferences sharedPreferences, String key) {
        updateSummary(key, true);
    }


}
