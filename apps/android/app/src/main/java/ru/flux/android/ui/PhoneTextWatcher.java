package ru.flux.android.ui;

import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.widget.EditText;

public class PhoneTextWatcher implements TextWatcher {

    private boolean updating = false;
    private String lastFormatted = "+7 (";

    /** Strips display formatting, returns e.g. "+79999999999". */
    public static String normalize(String displayed) {
        return displayed.replaceAll("[^+\\d]", "");
    }

    public static void setup(EditText editText) {
        editText.setText("+7 (");
        Selection.setSelection(editText.getText(), editText.getText().length());
        editText.addTextChangedListener(new PhoneTextWatcher());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        if (updating) return;
        updating = true;

        String raw = s.toString();
        String digits = raw.replaceAll("[^\\d]", "");
        if (digits.startsWith("7")) digits = digits.substring(1);
        if (digits.length() > 10) digits = digits.substring(0, 10);

        // If user deleted a separator (text shorter, digits unchanged), remove one digit too
        if (raw.length() < lastFormatted.length()) {
            String prevDigits = lastFormatted.replaceAll("[^\\d]", "");
            if (prevDigits.startsWith("7")) prevDigits = prevDigits.substring(1);
            if (digits.equals(prevDigits) && !digits.isEmpty()) {
                digits = digits.substring(0, digits.length() - 1);
            }
        }

        String formatted = format(digits);
        lastFormatted = formatted;
        s.replace(0, s.length(), formatted);
        Selection.setSelection(s, s.length());

        updating = false;
    }

    private static String format(String d) {
        int len = d.length();
        StringBuilder sb = new StringBuilder("+7");
        if (len == 0) return sb.toString();
        sb.append(" (").append(d, 0, Math.min(3, len));
        if (len < 3) return sb.toString();
        sb.append(") ");
        if (len > 3) sb.append(d, 3, Math.min(6, len));
        if (len < 6) return sb.toString();
        sb.append("-");
        if (len > 6) sb.append(d, 6, Math.min(8, len));
        if (len < 8) return sb.toString();
        sb.append("-");
        if (len > 8) sb.append(d, 8, len);
        return sb.toString();
    }
}