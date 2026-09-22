package dk.hjlab.minvaegt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int GREEN = Color.rgb(46, 125, 50);
    private static final int PALE = Color.rgb(232, 243, 231);
    private static final int BG = Color.rgb(247, 249, 246);
    private static final String PREFS = "min_vaegt";
    private final List<WeightEntry> weights = new ArrayList<>();
    private SharedPreferences prefs;
    private FrameLayout content;
    private Profile profile;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        profile = loadProfile();
        loadWeights();
        buildShell();
        showDashboard();
    }

    private void buildShell() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        LinearLayout header = row();
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(18), dp(14), dp(18), dp(14));
        header.setBackgroundColor(Color.rgb(244, 248, 242));
        LinearLayout titles = column();
        titles.addView(text("MIN VÆGT", 22, GREEN, true));
        titles.addView(text("Dit rolige overblik", 13, Color.DKGRAY, false));
        header.addView(titles, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        header.addView(text("v1.1.0", 12, Color.GRAY, false));
        root.addView(header);

        content = new FrameLayout(this);
        root.addView(content, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        LinearLayout nav = row();
        nav.setPadding(dp(6), dp(5), dp(6), dp(5));
        nav.setBackgroundColor(Color.WHITE);
        nav.addView(navButton("Overblik", v -> showDashboard()), weightParams());
        nav.addView(navButton("+ Ny vejning", v -> showAddWeight()), weightParams());
        nav.addView(navButton("Profil", v -> showProfile()), weightParams());
        root.addView(nav);
        setContentView(root);
    }

    private void showDashboard() {
        content.removeAllViews();
        ScrollView scroll = new ScrollView(this);
        LinearLayout box = column();
        box.setPadding(dp(16), dp(12), dp(16), dp(18));
        box.addView(text(profile.name.isEmpty() ? "Dit vægtforløb" : "Hej " + profile.name, 26, Color.BLACK, true));
        box.addView(text("Startdato: " + prettyDate(profile.startDate), 14, Color.GRAY, false));

        double current = weights.isEmpty() ? profile.startWeight : weights.get(weights.size() - 1).weight;
        LinearLayout summary = card(PALE);
        summary.setPadding(dp(20), dp(18), dp(20), dp(18));
        summary.addView(text("Nuværende vægt", 14, Color.DKGRAY, false));
        summary.addView(text(decimal(current) + " kg", 38, GREEN, true));
        LinearLayout stats = row();
        stats.addView(stat("Tabt", decimal(profile.startWeight - current) + " kg"), weightParams());
        stats.addView(stat("Til mål", decimal(Math.max(0, current - profile.goalWeight)) + " kg"), weightParams());
        double metres = profile.heightCm / 100.0;
        stats.addView(stat("BMI", decimal(metres > 0 ? current / (metres * metres) : 0)), weightParams());
        summary.addView(stats);
        box.addView(summary, marginParams(0, 14));

        LinearLayout graphCard = card(Color.WHITE);
        graphCard.addView(text("Udvikling", 19, Color.BLACK, true));
        graphCard.addView(new WeightChart(this, weights, profile.startWeight),
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(160)));
        graphCard.addView(text("Ønsket vægt: " + decimal(profile.goalWeight) + " kg", 15, GREEN, true));
        box.addView(graphCard, marginParams(0, 12));

        box.addView(text("Historik", 21, Color.BLACK, true), marginParams(0, 10));
        if (weights.isEmpty()) {
            box.addView(text("Ingen vejninger endnu. Tryk på “Ny vejning” nederst.", 15, Color.GRAY, false));
        } else {
            List<WeightEntry> newest = new ArrayList<>(weights);
            Collections.reverse(newest);
            for (WeightEntry entry : newest) {
                LinearLayout line = row();
                line.setGravity(Gravity.CENTER_VERTICAL);
                line.setPadding(dp(14), dp(10), dp(10), dp(10));
                line.setBackground(roundRect(Color.WHITE, 14));
                LinearLayout left = column();
                left.addView(text(decimal(entry.weight) + " kg", 19, Color.BLACK, true));
                left.addView(text(formatTime(entry.timestamp), 13, Color.GRAY, false));
                line.addView(left, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                Button delete = new Button(this);
                delete.setText("Slet");
                delete.setOnClickListener(v -> confirmDelete(entry));
                line.addView(delete);
                box.addView(line, marginParams(0, 7));
            }
        }
        scroll.addView(box);
        content.addView(scroll);
    }

    private void showAddWeight() {
        content.removeAllViews();
        LinearLayout box = column();
        box.setPadding(dp(20), dp(18), dp(20), dp(18));
        box.addView(text("Ny vejning", 27, Color.BLACK, true));
        box.addView(text("Dato og klokkeslæt gemmes automatisk.", 14, Color.GRAY, false));

        EditText weight = input("Din vægt i kg");
        double latest = weights.isEmpty() ? profile.startWeight : weights.get(weights.size() - 1).weight;
        weight.setText(decimal(latest));
        weight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        box.addView(weight, marginParams(0, 18));

        Button save = primaryButton("Gem vejning");
        save.setOnClickListener(v -> {
            Double value = parse(weight.getText().toString());
            if (value == null || value < 30 || value > 300) {
                weight.setError("Skriv en vægt mellem 30 og 300 kg");
                return;
            }
            weights.add(new WeightEntry(value, System.currentTimeMillis()));
            weights.sort(Comparator.comparingLong(a -> a.timestamp));
            saveWeights();
            Toast.makeText(this, "Vejningen er gemt", Toast.LENGTH_SHORT).show();
            showDashboard();
        });
        box.addView(save);
        content.addView(box);
    }

    private void showProfile() {
        content.removeAllViews();
        ScrollView scroll = new ScrollView(this);
        LinearLayout box = column();
        box.setPadding(dp(20), dp(16), dp(20), dp(20));
        box.addView(text("Din profil", 27, Color.BLACK, true));
        box.addView(text("Udfyld én ønsket idealvægt.", 14, Color.GRAY, false));

        EditText name = input("Navn"); name.setText(profile.name);
        EditText height = numberInput("Højde i cm", profile.heightCm);
        EditText start = numberInput("Startvægt i kg", profile.startWeight);
        EditText goal = numberInput("Ønsket idealvægt i kg", profile.goalWeight);
        EditText date = input("Startdato (åååå-mm-dd)"); date.setText(profile.startDate);
        box.addView(name, marginParams(0, 10));
        box.addView(height, marginParams(0, 8));
        box.addView(start, marginParams(0, 8));
        box.addView(goal, marginParams(0, 8));
        box.addView(date, marginParams(0, 8));

        Button save = primaryButton("Gem profil");
        save.setOnClickListener(v -> {
            Double h = parse(height.getText().toString());
            Double s = parse(start.getText().toString());
            Double g = parse(goal.getText().toString());
            boolean validDate;
            try { LocalDate.parse(date.getText().toString()); validDate = true; }
            catch (Exception ex) { validDate = false; }
            if (h == null || h < 100 || h > 230 || s == null || s < 30 || s > 300 ||
                    g == null || g < 30 || g > 300 || !validDate) {
                Toast.makeText(this, "Kontrollér højde, vægte og dato", Toast.LENGTH_LONG).show();
                return;
            }
            profile = new Profile(name.getText().toString().trim(), h, s, g, date.getText().toString());
            saveProfile();
            Toast.makeText(this, "Profilen er gemt", Toast.LENGTH_SHORT).show();
            showDashboard();
        });
        box.addView(save, marginParams(0, 10));
        box.addView(text("Oplysningerne gemmes kun lokalt på denne telefon.", 13, Color.GRAY, false));
        scroll.addView(box);
        content.addView(scroll);
    }

    private void confirmDelete(WeightEntry entry) {
        new AlertDialog.Builder(this)
                .setTitle("Slet vejning?")
                .setMessage(decimal(entry.weight) + " kg – " + formatTime(entry.timestamp))
                .setNegativeButton("Annuller", null)
                .setPositiveButton("Slet", (d, w) -> {
                    weights.remove(entry);
                    saveWeights();
                    showDashboard();
                }).show();
    }

    private Profile loadProfile() {
        return new Profile(
                prefs.getString("name", ""),
                Double.longBitsToDouble(prefs.getLong("height", Double.doubleToLongBits(170))),
                Double.longBitsToDouble(prefs.getLong("startWeight", Double.doubleToLongBits(80))),
                Double.longBitsToDouble(prefs.getLong("goalWeight", Double.doubleToLongBits(75))),
                prefs.getString("startDate", LocalDate.now().toString())
        );
    }

    private void saveProfile() {
        prefs.edit()
                .putString("name", profile.name)
                .putLong("height", Double.doubleToLongBits(profile.heightCm))
                .putLong("startWeight", Double.doubleToLongBits(profile.startWeight))
                .putLong("goalWeight", Double.doubleToLongBits(profile.goalWeight))
                .putString("startDate", profile.startDate).apply();
    }

    private void loadWeights() {
        weights.clear();
        try {
            JSONArray array = new JSONArray(prefs.getString("weights", "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                weights.add(new WeightEntry(item.getDouble("weight"), item.getLong("timestamp")));
            }
            weights.sort(Comparator.comparingLong(a -> a.timestamp));
        } catch (Exception ignored) {}
    }

    private void saveWeights() {
        JSONArray array = new JSONArray();
        try {
            for (WeightEntry entry : weights) {
                array.put(new JSONObject().put("weight", entry.weight).put("timestamp", entry.timestamp));
            }
        } catch (Exception ignored) {}
        prefs.edit().putString("weights", array.toString()).apply();
    }

    private LinearLayout stat(String title, String value) {
        LinearLayout box = column();
        box.setPadding(dp(8), dp(8), dp(8), dp(8));
        box.setBackground(roundRect(Color.WHITE, 12));
        box.addView(text(title, 12, Color.GRAY, false));
        box.addView(text(value, 16, Color.BLACK, true));
        return box;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private Button navButton(String label, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(12);
        b.setOnClickListener(listener);
        return b;
    }

    private Button primaryButton(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(Color.WHITE);
        b.setTextSize(17);
        b.setBackground(roundRect(GREEN, 14));
        b.setMinHeight(dp(54));
        return b;
    }

    private EditText input(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextSize(17);
        e.setSingleLine(true);
        e.setPadding(dp(12), dp(12), dp(12), dp(12));
        e.setBackground(roundStroke(Color.WHITE, Color.LTGRAY, 12));
        return e;
    }

    private EditText numberInput(String hint, double value) {
        EditText e = input(hint);
        e.setText(decimal(value));
        e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        return e;
    }

    private LinearLayout card(int color) {
        LinearLayout box = column();
        box.setPadding(dp(16), dp(16), dp(16), dp(16));
        box.setBackground(roundRect(color, 18));
        return box;
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private LinearLayout row() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        return l;
    }

    private LinearLayout.LayoutParams weightParams() {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
    }

    private LinearLayout.LayoutParams marginParams(int top, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.topMargin = dp(top);
        p.bottomMargin = dp(bottom);
        return p;
    }

    private GradientDrawable roundRect(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private GradientDrawable roundStroke(int color, int stroke, int radius) {
        GradientDrawable d = roundRect(color, radius);
        d.setStroke(dp(1), stroke);
        return d;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static String decimal(double value) {
        return String.format(new Locale("da", "DK"), "%.1f", value);
    }

    private static Double parse(String value) {
        try { return Double.parseDouble(value.replace(",", ".").trim()); }
        catch (Exception ex) { return null; }
    }

    private static String prettyDate(String raw) {
        try {
            return LocalDate.parse(raw).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (Exception ex) { return raw; }
    }

    private static String formatTime(long millis) {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy 'kl.' HH:mm"));
    }

    private static class Profile {
        final String name, startDate;
        final double heightCm, startWeight, goalWeight;
        Profile(String name, double heightCm, double startWeight, double goalWeight, String startDate) {
            this.name = name; this.heightCm = heightCm; this.startWeight = startWeight;
            this.goalWeight = goalWeight; this.startDate = startDate;
        }
    }

    private static class WeightEntry {
        final double weight;
        final long timestamp;
        WeightEntry(double weight, long timestamp) {
            this.weight = weight; this.timestamp = timestamp;
        }
    }

    private static class WeightChart extends View {
        private final List<WeightEntry> values = new ArrayList<>();
        private final double fallback;
        private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);

        WeightChart(Context context, List<WeightEntry> source, double fallback) {
            super(context);
            values.addAll(source);
            this.fallback = fallback;
            line.setColor(GREEN); line.setStrokeWidth(7); line.setStyle(Paint.Style.STROKE);
            line.setStrokeCap(Paint.Cap.ROUND);
            dot.setColor(GREEN);
            setBackgroundColor(Color.WHITE);
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            List<Double> weights = new ArrayList<>();
            for (WeightEntry e : values) weights.add(e.weight);
            if (weights.isEmpty()) weights.add(fallback);
            double low = Collections.min(weights);
            double high = Collections.max(weights);
            double range = Math.max(1, high - low);
            float left = 12, right = getWidth() - 12, top = 18, bottom = getHeight() - 18;
            Path path = new Path();
            for (int i = 0; i < weights.size(); i++) {
                float x = weights.size() == 1 ? getWidth() / 2f :
                        left + i * (right - left) / (weights.size() - 1);
                float y = (float)(bottom - ((weights.get(i) - low) / range) * (bottom - top));
                if (i == 0) path.moveTo(x, y); else path.lineTo(x, y);
            }
            canvas.drawPath(path, line);
            for (int i = 0; i < weights.size(); i++) {
                float x = weights.size() == 1 ? getWidth() / 2f :
                        left + i * (right - left) / (weights.size() - 1);
                float y = (float)(bottom - ((weights.get(i) - low) / range) * (bottom - top));
                canvas.drawCircle(x, y, 9, dot);
            }
        }
    }
}
