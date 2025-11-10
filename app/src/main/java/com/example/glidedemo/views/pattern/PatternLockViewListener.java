package com.example.glidedemo.views.pattern;

import java.util.List;

/**
 * The callback interface for detecting patterns entered by the user
 */
public interface PatternLockViewListener {

    /**
     * Fired when the pattern drawing has just started
     */
    void onStarted();

    /**
     * Fired when the pattern is still being drawn and progressed to
     */
    void onProgress(List<Dot> progressPattern);

    /**
     * Fired when the user has completed drawing the pattern and has moved their finger away
     * from the view
     */
    void onComplete(List<Dot> pattern);

    /**
     * Fired when the patten has been cleared from the view
     */
    void onCleared();
}
