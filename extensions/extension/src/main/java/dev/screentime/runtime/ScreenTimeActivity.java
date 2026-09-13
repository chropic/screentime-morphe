package dev.screentime.runtime;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

/** Dedicated-process controls for the explicit initial policy required before activation. */
public final class ScreenTimeActivity extends Activity {
    private static final String BUDGET_HINT = "Budget (hh:mm:ss)";
    private static final String RESET_HINT = "Daily reset (HH:mm)";
    private EditText budget;
    private EditText reset;
    private RadioGroup mode;
    private TextView status;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 48, 48, 48);
        TextView title = new TextView(this); title.setText("Screen time"); layout.addView(title);
        budget = new EditText(this); budget.setHint(BUDGET_HINT); layout.addView(budget);
        reset = new EditText(this); reset.setHint(RESET_HINT); layout.addView(reset);
        mode = new RadioGroup(this);
        RadioButton warning = new RadioButton(this); warning.setId(View.generateViewId()); warning.setText("Warning mode"); mode.addView(warning);
        RadioButton blocking = new RadioButton(this); blocking.setId(View.generateViewId()); blocking.setText("Blocking mode"); mode.addView(blocking);
        warning.setChecked(true); layout.addView(mode);
        Button save = new Button(this); save.setText("Save policy"); save.setOnClickListener(view -> savePolicy()); layout.addView(save);
        status = new TextView(this); layout.addView(status);
        setContentView(layout);
        refreshState();
    }

    private void savePolicy() {
        try {
            Bundle values = new Bundle();
            values.putLong(StateProvider.KEY_BUDGET_MILLIS, parseBudget(budget.getText().toString()));
            values.putLong(StateProvider.KEY_RESET_SECONDS, parseReset(reset.getText().toString()));
            RadioButton selected = findViewById(mode.getCheckedRadioButtonId());
            values.putString(StateProvider.KEY_MODE, selected.getText().toString().startsWith("Blocking") ? "BLOCKING" : "WARNING");
            Bundle current = call(StateProvider.OP_READ_STATE, null);
            call(current != null && current.getBoolean("configured") ? StateProvider.OP_REQUEST_POLICY_CHANGE : StateProvider.OP_SET_INITIAL_POLICY, values);
            refreshState();
        } catch (RuntimeException exception) { status.setText("Cannot save: " + exception.getMessage()); }
    }

    private void refreshState() {
        Bundle state = call(StateProvider.OP_READ_STATE, null);
        if (state == null || !state.getBoolean("configured")) { status.setText("Policy is inactive until all values are set."); return; }
        String pending = state.containsKey("pending_activation_millis") ? " Pending change activates at " + state.getLong("pending_activation_millis") + "." : "";
        status.setText("Current decision: " + state.getString("decision") + ". Used: " + state.getLong("consumed_millis") + " ms." + pending);
    }

    private Bundle call(String operation, Bundle values) {
        Uri uri = Uri.parse("content://" + getPackageName() + ".screentime.internal");
        return getContentResolver().call(uri, operation, null, values);
    }

    private static long parseBudget(String text) {
        String[] parts = text.trim().split(":", -1);
        if (parts.length != 3) throw new IllegalArgumentException(BUDGET_HINT);
        long hours = number(parts[0], 0, 23, BUDGET_HINT), minutes = number(parts[1], 0, 59, BUDGET_HINT), seconds = number(parts[2], 0, 59, BUDGET_HINT);
        long result = ((hours * 60 + minutes) * 60 + seconds) * 1000L;
        if (result == 0) throw new IllegalArgumentException("Budget must be positive");
        return result;
    }

    private static long parseReset(String text) {
        String[] parts = text.trim().split(":", -1);
        if (parts.length != 2) throw new IllegalArgumentException(RESET_HINT);
        return number(parts[0], 0, 23, RESET_HINT) * 3600 + number(parts[1], 0, 59, RESET_HINT) * 60;
    }

    private static long number(String value, long minimum, long maximum, String hint) {
        try { long parsed = Long.parseLong(value); if (parsed < minimum || parsed > maximum) throw new NumberFormatException(); return parsed; }
        catch (NumberFormatException exception) { throw new IllegalArgumentException("Invalid " + hint); }
    }
}
