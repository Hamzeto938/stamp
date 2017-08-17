package com.sjn.stamp.ui.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.sjn.stamp.R;
import com.sjn.stamp.controller.UserSettingController;
import com.sjn.stamp.ui.DialogFacade;
import com.sjn.stamp.utils.LogHelper;
import com.sjn.stamp.utils.RealmHelper;

public class SettingFragment extends PreferenceFragmentCompat {

    private static final String TAG = LogHelper.makeLogTag(SettingFragment.class);

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        findPreference("import_backup").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogFacade.createConfirmDialog(getActivity(), R.string.dialog_confirm_import, new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        switch (which) {
                            case NEGATIVE:
                                return;
                            case POSITIVE:
                                ProgressDialog progressDialog = new ProgressDialog(getActivity());
                                progressDialog.setMessage(getString(R.string.message_processing));
                                progressDialog.show();
                                RealmHelper.importBackUp(getActivity());
                                progressDialog.dismiss();
                                DialogFacade.createRestartDialog(getActivity(), new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        getActivity().recreate();
                                    }
                                }).show();
                        }
                    }
                }, new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                    }
                }).show();
                return true;
            }
        });

        findPreference("export_backup").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                DialogFacade.createConfirmDialog(getActivity(), R.string.dialog_confirm_export, new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        switch (which) {
                            case NEGATIVE:
                                return;
                            case POSITIVE:
                                ProgressDialog progressDialog = new ProgressDialog(getActivity());
                                progressDialog.setMessage(getString(R.string.message_processing));
                                progressDialog.show();
                                RealmHelper.exportBackUp(getActivity());
                                progressDialog.dismiss();
                        }
                    }
                }, new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                    }
                }).show();
                return true;
            }
        });

        findPreference("licence").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogFacade.createLicenceDialog(getActivity()).show();
                return true;
            }
        });

        findPreference("setting_songs_new_song_days").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (preference instanceof EditTextPreference) {
                    ((EditTextPreference) preference).setText(String.valueOf(new UserSettingController().getNewSongDays()));
                }
                return true;
            }
        });
        findPreference("setting_songs_new_song_days").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                try {
                    int newSongDays = Integer.parseInt(newValue.toString());
                    if (newSongDays >= 0 && newSongDays <= 999) {
                        new UserSettingController().setNewSongDays(newSongDays);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        findPreference("setting_songs_most_played_song_size").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (preference instanceof EditTextPreference) {
                    ((EditTextPreference) preference).setText(String.valueOf(new UserSettingController().getMostPlayedSongSize()));
                }
                return true;
            }
        });
        findPreference("setting_songs_most_played_song_size").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                try {
                    int mostPlayedSongSize = Integer.parseInt(newValue.toString());
                    if (mostPlayedSongSize >= 0 && mostPlayedSongSize <= 999) {
                        new UserSettingController().setMostPlayedSongSize(mostPlayedSongSize);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FloatingActionButton mFab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        if (mFab != null) {
            mFab.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        FloatingActionButton mFab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        if (mFab != null) {
            mFab.setVisibility(View.VISIBLE);
        }
    }

}
