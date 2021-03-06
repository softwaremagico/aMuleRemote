/*
 * Copyright (c) 2015. Gianluca Vegetti
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.iukonline.amule.android.amuleremote.search;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.iukonline.amule.android.amuleremote.AmuleRemoteApplication;
import com.iukonline.amule.android.amuleremote.BuildConfig;
import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ECSearchListWatcher;

import java.util.ArrayList;

public class SearchResultsListFragment extends ListFragment implements ECSearchListWatcher {
    
    
    
    public interface SearchResultsListFragmentContainter {
        void viewResultDetails(int selected) ;
    }

    private final static String TAG = AmuleRemoteApplication.AC_LOGTAG;
    private final static boolean DEBUG = BuildConfig.DEBUG;
    
    AmuleRemoteApplication mApp;
    SearchResultsListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mApp = (AmuleRemoteApplication) getActivity().getApplication();
        super.onCreate(savedInstanceState);
        

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            //return null;
        }
        
        if (DEBUG) Log.d(TAG, "SearchResultsListFragment.onCreateView: Inflating view");
        View v = inflater.inflate(R.layout.frag_search_results_list, container, false);
        if (DEBUG) Log.d(TAG, "SearchResultsListFragment.onCreateView: Inflated view");
        return v;
    }

    
    
    @Override
    public void onResume() {
        updateECSearchList(mApp.mECHelper.registerForECSsearchList(this));
        super.onResume();
    }
    
    @Override
    public void onPause() {
        mApp.mECHelper.unRegisterFromECSearchList(this);
        super.onPause();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ((SearchResultsListFragmentContainter) getActivity()).viewResultDetails(position);
    }

    

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    @Override
    public String getWatcherId() {
        return this.getClass().getName();
    }

    @Override
    public UpdateResult updateECSearchList(ArrayList<SearchContainer> searches) {
        
        if (searches == null) {
            if (mAdapter != null) mAdapter.clear();
            return UpdateResult.DO_NOTHING;
        }
        if (mAdapter == null) {
            if (DEBUG) Log.d(TAG, "SearchResultsListFragment.updateECSearchList: Creating new adapter");
            mAdapter = new SearchResultsListAdapter(getActivity(), R.layout.frag_search_results_list, new ArrayList<SearchContainer>());
            setListAdapter(mAdapter);
        }
        
        int searchesSize = searches.size();
        int adapterSize = mAdapter.getCount();
        
        if (DEBUG) Log.d(TAG, "SearchResultsListFragment.updateECSearchList: Search List update received. Size is " + searchesSize + " while current adapter size is " + adapterSize);
        
        if (searchesSize == 0 ) {
            if (DEBUG) Log.d(TAG, "SearchResultsListFragment.updateECSearchList: Empty search list");
            mAdapter.clear();
        } else {
            if (adapterSize == 0) {
                // Adapter is empty
                if (DEBUG) Log.d(TAG, "SearchResultsListFragment.updateECSearchList: Adapter is empty");
                
                // mAdapter.addAll(searches); Doesn't seem to work
                for (SearchContainer s : searches) {
                    mAdapter.add(s);
                }                
                
            } else if (searches.get(searchesSize - 1) != mAdapter.getItem(adapterSize - 1)) {
                // Eldest search item is different... => Two different search lists, let's clear and re-populate
                if (DEBUG) Log.d(TAG, "SearchResultsListFragment.updateECSearchList: Search List changed");
                mAdapter.clear();
                for (SearchContainer s : searches) {
                    mAdapter.add(s);
                }
            } else if (adapterSize < searchesSize) {
                // Eldest search item is the same, but size differs. We need to add new elements
                if (DEBUG) Log.d(TAG, "SearchResultsListFragment.updateECSearchList: Search List is bigger");
                for (int i = 0 ; i < searchesSize - adapterSize; i++) {
                    mAdapter.insert(searches.get(i), i);
                }
            } else if (adapterSize == searchesSize) {
                if (DEBUG) Log.d(TAG, "SearchResultsListFragment.updateECSearchList: Search List is the same");
                // Do nothing. The list is the same.
                mAdapter.notifyDataSetChanged();
            } else {
                // TODO Raise exception
                if (DEBUG) Log.d(TAG, "SearchResultsListFragment.updateECSearchList: This should never happen...");
                mAdapter.clear();
            }
        }
        return UpdateResult.DO_NOTHING;
    }
}
