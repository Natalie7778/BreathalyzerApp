package com.dfrobot.angelo.blunobasicdemo;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class datashare_consent extends DialogFragment {

    private CheckBox dataShare;
    private Button btnCancel, btnSave;
    private OnConsentListener listener;
    private boolean currentConsent;
    public static datashare_consent newInstance() {
        return new datashare_consent();
    }
    public interface OnConsentListener {
        void onConsentChanged(boolean consentGiven);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.datashare_consent_dialogfragment, container, false);

        dataShare = view.findViewById(R.id.cb_consent);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnSave = view.findViewById(R.id.btn_save);

        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        currentConsent = prefs.getBoolean("dataShareConsent", false); // default is false
        dataShare.setChecked(currentConsent);

        btnSave.setOnClickListener(v -> {
            // Save to SharedPreferences
            boolean isChecked = dataShare.isChecked();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("dataShareConsent", isChecked);
            editor.apply();
            if (listener != null) {
                listener.onConsentChanged(dataShare.isChecked());
            }

            // Close the dialog
            dismiss();
        });
        btnCancel.setOnClickListener(v -> dismiss());

        return view;
    }
}

