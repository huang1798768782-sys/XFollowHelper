package com.example.xfollowhelper;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FollowAccessibilityService extends AccessibilityService {
    private static final Set<String> FOLLOW_TEXTS = new HashSet<>(Arrays.asList(
            "Follow", "关注"
    ));
    private long lastAttempt = 0L;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;
        if (!"com.twitter.android".contentEquals(event.getPackageName())) return;

        SharedPreferences sp = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE);
        long until = sp.getLong(MainActivity.KEY_ARMED_UNTIL, 0L);
        long nowWall = System.currentTimeMillis();
        if (until <= nowWall) {
            if (until != 0L) sp.edit().remove(MainActivity.KEY_ARMED_UNTIL).apply();
            return;
        }

        long now = SystemClock.elapsedRealtime();
        if (now - lastAttempt < 500L) return;
        lastAttempt = now;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;
        AccessibilityNodeInfo target = findClickableFollow(root);
        if (target != null) {
            boolean clicked = target.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            if (clicked) {
                sp.edit().remove(MainActivity.KEY_ARMED_UNTIL).apply();
            }
        }
    }

    private AccessibilityNodeInfo findClickableFollow(AccessibilityNodeInfo node) {
        if (node == null) return null;
        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        boolean exact = (text != null && FOLLOW_TEXTS.contains(text.toString().trim())) ||
                (desc != null && FOLLOW_TEXTS.contains(desc.toString().trim()));

        if (exact) {
            AccessibilityNodeInfo clickable = node;
            while (clickable != null && !clickable.isClickable()) {
                clickable = clickable.getParent();
            }
            if (clickable != null) return clickable;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo found = findClickableFollow(node.getChild(i));
            if (found != null) return found;
        }
        return null;
    }

    @Override
    public void onInterrupt() {}
}
