package dk.hjlab.minvaegt;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("MIN VÆGT TEST");
        title.setTextSize(28);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);

        TextView version = new TextView(this);
        version.setText("Version 3.0.1");
        version.setTextSize(18);
        version.setTextColor(Color.DKGRAY);
        version.setGravity(Gravity.CENTER);

        TextView status = new TextView(this);
        status.setText("Installationen virker.");
        status.setTextSize(16);
        status.setTextColor(Color.DKGRAY);
        status.setGravity(Gravity.CENTER);
        status.setPadding(40, 30, 40, 0);

        root.addView(title);
        root.addView(version);
        root.addView(status);

        setContentView(root);
    }
}
