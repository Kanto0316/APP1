package com.netk.mvolacash.admin;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.netk.mvolacash.admin.activation.ActivationRequest;

public class MainActivity extends AppCompatActivity {
    private TextInputLayout requestInputLayout;
    private TextInputEditText requestInput;
    private TextInputEditText activationOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestInputLayout = findViewById(R.id.requestInputLayout);
        requestInput = findViewById(R.id.requestInput);
        activationOutput = findViewById(R.id.activationOutput);
        MaterialButton pasteButton = findViewById(R.id.pasteButton);
        MaterialButton generateButton = findViewById(R.id.generateButton);
        MaterialButton copyButton = findViewById(R.id.copyButton);

        pasteButton.setOnClickListener(view -> pasteRequestCode());
        generateButton.setOnClickListener(view -> generateActivation());
        copyButton.setOnClickListener(view -> copyActivationCode());
    }

    private void pasteRequestCode() {
        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard.hasPrimaryClip() && clipboard.getPrimaryClip() != null) {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            CharSequence text = item.coerceToText(this);
            requestInput.setText(text == null ? "" : text.toString());
        } else {
            Toast.makeText(this, R.string.clipboard_empty, Toast.LENGTH_SHORT).show();
        }
    }

    private void generateActivation() {
        ActivationRequest request = new ActivationRequest(textOf(requestInput));
        requestInput.setText(request.getRequestCode());

        if (request.isEmpty()) {
            requestInputLayout.setError(getString(R.string.request_required));
            activationOutput.setText("");
            return;
        }

        requestInputLayout.setError(null);
        activationOutput.setText(R.string.activation_not_configured);
    }

    private void copyActivationCode() {
        String activationCode = textOf(activationOutput);
        if (activationCode.isEmpty()) {
            Toast.makeText(this, R.string.nothing_to_copy, Toast.LENGTH_SHORT).show();
            return;
        }

        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText(
                getString(R.string.activation_code), activationCode));
        Toast.makeText(this, R.string.activation_copied, Toast.LENGTH_SHORT).show();
    }

    private static String textOf(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString();
    }
}
