package com.example.waterlevelindicator;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.widget.TextView;

import com.john.waveview.WaveView;

public class CustomDialog {
    private Context      context;
    private MainActivity activity;
    private WaveView     progressView;
    private TextView     messageText;
    private Dialog       dialogBox;

    public CustomDialog(MainActivity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }

    public void showProgressDialog(String message) {
        if(this.dialogBox == null) {
            this.dialogBox = new Dialog(this.activity);
            this.dialogBox.setContentView(R.layout.custom_progress_dialog_layout);
            this.dialogBox.setCancelable(false);
            this.dialogBox.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            this.messageText = this.dialogBox.findViewById(R.id.progress_dialog_message);
            this.progressView = this.dialogBox.findViewById(R.id.progressView);
        }
        this.messageText.setText(message);
        this.dialogBox.show();
    }
    public boolean isDialogVisible() {
        if(this.dialogBox == null)
            return false;
        return dialogBox.isShowing();
    }
    public void hideDialogBox() {
        if(this.dialogBox != null) {
            dialogBox.dismiss();
            this.dialogBox = null;
        }
    }

}
