package com.sjn.stamp.ui.fragment.media_list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.sjn.stamp.R;
import com.sjn.stamp.ui.custom.PeriodSelectLayout;

import java.util.ArrayList;
import java.util.List;

public class RankingPagerFragment extends PagerFragment implements PagerFragment.PageFragmentContainer.Creator {

    private PeriodSelectLayout.Period mPeriod;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mPeriod = new PeriodSelectLayout.Period();
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.ranking, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.period:
                if (getActivity() != null) {
                    final PeriodSelectLayout periodSelectLayout = new PeriodSelectLayout(getActivity(), null, mPeriod);
                    new MaterialDialog.Builder(getContext())
                            .title(R.string.dialog_period_select)
                            .customView(periodSelectLayout, true)
                            .positiveText(R.string.dialog_ok)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    mPeriod = periodSelectLayout.getPeriod();
                                    if (mAdapter == null) {
                                        return;
                                    }
                                    for (int i = 0; i < mAdapter.getCount(); i++) {
                                        Fragment fragment = mAdapter.getItem(i);
                                        if (fragment != null && fragment instanceof RankingFragment) {
                                            ((RankingFragment) fragment).setPeriodAndReload(mPeriod);
                                        }
                                    }
                                }
                            })
                            .contentColorRes(android.R.color.white)
                            .backgroundColorRes(R.color.material_blue_grey_800)
                            .theme(Theme.DARK)
                            .show();
                }
                return false;
            default:
                break;
        }
        return false;
    }

    @Override
    List<PageFragmentContainer> setUpFragmentContainer() {
        List<PageFragmentContainer> fragmentContainerList = new ArrayList<>();
        fragmentContainerList.add(new PageFragmentContainer(getString(R.string.ranking_tab_my_songs), RankingFragment.RankKind.SONG.toString(), this));
        fragmentContainerList.add(new PageFragmentContainer(getString(R.string.ranking_tab_my_artists), RankingFragment.RankKind.ARTIST.toString(), this));
        return fragmentContainerList;
    }

    @Override
    public Fragment create(String fragmentHint) {
        Fragment fragment = new RankingFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PAGER_KIND_KEY, fragmentHint);
        fragment.setArguments(bundle);
        return fragment;
    }

}