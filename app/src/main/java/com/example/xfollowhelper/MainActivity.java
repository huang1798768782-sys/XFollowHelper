package com.example.xfollowhelper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    public static final String PREFS = "follow_helper";
    public static final String KEY_ARMED_UNTIL = "armed_until";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int pad = dp(20);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        root.setGravity(Gravity.TOP);

        TextView title = new TextView(this);
        title.setText("X 连续使用关注助手");
        title.setTextSize(24);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title, matchWrap());

        TextView info = new TextView(this);
        info.setText("输入 X 用户名或个人主页链接。每次点击执行后，会打开官方 X App，并保持本次授权直到成功点击一次“关注 / Follow”。\n\n成功后本次授权立即解除；下一次关注只需回到本应用再次点击执行。不会读取或保存你的 X 密码。");
        info.setTextSize(16);
        LinearLayout.LayoutParams infoLp = matchWrap();
        infoLp.topMargin = dp(14);
        root.addView(info, infoLp);

        EditText input = new EditText(this);
        input.setHint("例如：OpenAI 或 https://x.com/OpenAI");
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        LinearLayout.LayoutParams inputLp = matchWrap();
        inputLp.topMargin = dp(20);
        root.addView(input, inputLp);

        Button access = new Button(this);
        access.setText("1. 开启无障碍服务");
        access.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        LinearLayout.LayoutParams btnLp1 = matchWrap();
        btnLp1.topMargin = dp(16);
        root.addView(access, btnLp1);

        Button follow = new Button(this);
        follow.setText("2. 执行一次关注");
        follow.setOnClickListener(v -> {
            String raw = input.getText().toString().trim();
            String username = normalizeUsername(raw);
            if (username.isEmpty()) {
                Toast.makeText(this, "请输入用户名或 X 主页链接", Toast.LENGTH_SHORT).show();
                return;
            }

            // No short timeout: remain armed until one successful click.
            getSharedPreferences(PREFS, MODE_PRIVATE)
                    .edit()
                    .putLong(KEY_ARMED_UNTIL, Long.MAX_VALUE)
                    .apply();

            Uri uri = Uri.parse("https://x.com/" + username);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(intent);
            } catch (Exception e) {
                getSharedPreferences(PREFS, MODE_PRIVATE).edit().remove(KEY_ARMED_UNTIL).apply();
                Toast.makeText(this, "无法打开 X，请确认已安装 X App", Toast.LENGTH_LONG).show();
            }
        });
        LinearLayout.LayoutParams btnLp2 = matchWrap();
        btnLp2.topMargin = dp(8);
        root.addView(follow, btnLp2);

        Button cancel = new Button(this);
        cancel.setText("取消当前授权");
        cancel.setOnClickListener(v -> {
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().remove(KEY_ARMED_UNTIL).apply();
            Toast.makeText(this, "已取消当前授权", Toast.LENGTH_SHORT).show();
        });
        LinearLayout.LayoutParams cancelLp = matchWrap();
        cancelLp.topMargin = dp(8);
        root.addView(cancel, cancelLp);

        TextView note = new TextView(this);
        note.setText("提示：本次授权不会因 60 秒超时而失效。成功关注一次后会自动解除；需要下一次关注时，再回到本应用点“执行一次关注”。");
        note.setTextSize(14);
        LinearLayout.LayoutParams noteLp = matchWrap();
        noteLp.topMargin = dp(16);
        root.addView(note, noteLp);

        setContentView(root);
    }

    private String normalizeUsername(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        if (s.startsWith("@")) s = s.substring(1);
        try {
            if (s.startsWith("http://") || s.startsWith("https://")) {
                Uri u = Uri.parse(s);
                String path = u.getPath();
                if (path != null) {
                    String[] parts = path.split("/");
                    for (String p : parts) if (!p.isEmpty()) return clean(p);
                }
                return "";
            }
        } catch (Exception ignored) {}
        return clean(s);
    }

    private String clean(String s) {
        return s.replaceAll("[^A-Za-z0-9_]", "");
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }
}
