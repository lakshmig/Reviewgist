package com.reviewgist.rg.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RGListsArray implements Serializable {

    private static final long serialVersionUID = -7957577448455303642L;
    private List<RGLists> mPosts;
    private String mNextPageURL;
    private boolean mLoadedMore; // currently, we can perform only one "load-more" action reliably
    
    public RGListsArray() {
        mPosts = new ArrayList<RGLists>();
    }
    
    public RGListsArray(List<RGLists> posts, String nextPageURL) {
        mPosts = posts;
        mNextPageURL = nextPageURL;
    }

    public void addPost(RGLists post) {
        mPosts.add(post);
    }

    public List<RGLists> getPosts() {
        return mPosts;
    }
    
    public void addPosts(Collection<RGLists> posts) {
        mPosts.addAll(posts);
    }
    
    public String getNextPageURL() {
        return mNextPageURL;
    }
    
    public void setNextPageURL(String mNextPageURL) {
        this.mNextPageURL = mNextPageURL;
    }
    
    public void appendLoadMoreFeed(RGListsArray feed) {
        if (feed == null || feed.getPosts() == null)
            return;
        
        mLoadedMore = true;
        for (RGLists candidate : feed.getPosts())
            if (!mPosts.contains(candidate))
                mPosts.add(candidate);
        mNextPageURL = feed.getNextPageURL();
    }
    
    public boolean isLoadedMore() {
        return mLoadedMore;
    }
    
}
