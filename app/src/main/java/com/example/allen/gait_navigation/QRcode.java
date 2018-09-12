package com.example.allen.gait_navigation;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

public class QRcode extends AppCompatActivity {
    int RESULT_OK=1;
    boolean qr=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        IntentIntegrator integrator=new IntentIntegrator(QRcode.this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Camera Scan");
        integrator.setCameraId(0);
        integrator.initiateScan();

    }
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        IntentResult result=IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if (result!=null)
            alert(result.getContents());
        else
            alert("Scan cancelado");
    }
    private void alert(String msg)
    {
        //Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
        Intent intent=getIntent();
        Bundle bundle=new Bundle();
        String[] place=msg.split(" ");
        bundle.putStringArray("location",place);
        bundle.putBoolean("QR_boolean",qr);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        QRcode.this.finish();
    }

}
