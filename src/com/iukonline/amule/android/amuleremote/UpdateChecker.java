package com.iukonline.amule.android.amuleremote;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;

public class UpdateChecker {
    
    private static final String AC_UPDATE_URL = "http://dl.dropbox.com/u/15068406/AmuleRemote/updates.xml";
    private static final long AC_UPDATE_INTERVAL = 24L*60L*60L*1000L;
    
    interface UpdatesWatcher {
        public void notifyUpdate(String newReleaseURL, String releaseNotes);
    }
    
    UpdatesWatcher mUpdatesWatcher;
    long mLatestUpdatesCheck;
    boolean mLatestUpdatePublished = true;
    
    private String mReleaseNotes;
    private String mNewReleaseURL;
    
    private int mCurrentVersionCode;
    
    UpdateChecker(int currentVersionCode) {
        mCurrentVersionCode = currentVersionCode;
    }
    
    public void registerUpdatesWatcher(UpdatesWatcher watcher) {
        mUpdatesWatcher = watcher;
        if (watcher != null) {
            if (! mLatestUpdatePublished) {
                mUpdatesWatcher.notifyUpdate(mNewReleaseURL, mReleaseNotes);
                mLatestUpdatePublished = true;
            } else {
                checkForUpdates();
            }
        }
    }
    
    private void checkForUpdates() {
        long now = System.currentTimeMillis();
        if (now - mLatestUpdatesCheck > AC_UPDATE_INTERVAL) {
            // Schedule update check Task, which will result in an update refresh and publish
            
            AsyncTask<String, Void, String> fetchReleaseXML = new AsyncTask<String, Void, String>() {
                
                @Override
                protected String doInBackground(String... params) {
                    return getTextFileFromURL(params[0]);
                }
                
                @Override
                protected void onPostExecute(String result) {
                    if (result != null) parseUpdatesXML(result);
                }
            };
            fetchReleaseXML.execute(AC_UPDATE_URL);
            mLatestUpdatesCheck = now;
        }
    }
    
    private void parseUpdatesXML(String xml) {

        String latestReleaseXML = getXMLElement(xml, "latestRelease");
        if (latestReleaseXML != null) {
            int latestVersionCode = Integer.parseInt(getXMLElement(latestReleaseXML, "versionCode"));
            String latestVersionReleaseNotesURL = getXMLElement(latestReleaseXML, "releaseNotesURL");
            String latestVersionDownloadURL = getXMLElement(latestReleaseXML, "downloadURL");
            
            if (latestVersionCode > mCurrentVersionCode) {
                mNewReleaseURL = latestVersionDownloadURL;
                AsyncTask<String, Void, String> fetchReleaseNotes = new AsyncTask<String, Void, String>() {
                    
                    @Override
                    protected String doInBackground(String... params) {
                        return getTextFileFromURL(params[0]);
                    }
                    
                    @Override
                    protected void onPostExecute(String result) {
                        if (result != null) {
                            mReleaseNotes = result;
                            
                            if (mUpdatesWatcher != null) {
                                mUpdatesWatcher.notifyUpdate(mNewReleaseURL, mReleaseNotes);
                            } else {
                                mLatestUpdatePublished = false;
                            }
                        }
                    }
                };
                fetchReleaseNotes.execute(latestVersionReleaseNotesURL);
            }
        }
    }
    
    
    private String getXMLElement(String xml, String tag) {
        int tagStart = xml.indexOf("<" + tag);
        
        if (tagStart >= 0) {
            int contentStart = xml.indexOf(">", tagStart);
            if (contentStart > tagStart) {
                contentStart++;
                int tagEnd = xml.indexOf("</" + tag, contentStart);
                if (tagEnd > contentStart) {
                    return xml.substring(contentStart, tagEnd);
                }
            }
        }
        return null;
        
    }
    
    
    
    private String getTextFileFromURL(String url) {
        URL getURL;
        StringBuilder sb = new StringBuilder();
        try {
            getURL = new URL(url);
            getURL.openConnection();
            InputStream reader = getURL.openStream();
            byte[] buffer = new byte[8096];

            int bytesRead = 0;
            while ((bytesRead = reader.read(buffer)) > 0) {
               for (int i = 0; i < bytesRead; i++) {
                   sb.append((char) buffer[i]);
               }
            }
            
        } catch (MalformedURLException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
        
        return sb.toString();
    }
}
