package com.example.paarasbhandari.bitcoinrate;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.lang.*;

import java.net.*;
import java.io.*;

import java.security.*;

import java.util.ArrayList;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    private double price = 0.0;
    private String cur;
    private TextView text;
    private Button button;
    private String tempStr;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = findViewById(R.id.textView);
        text.setText("");


        spinner = findViewById(R.id.spinner);
        String[] items = new String[]{"USD","GBP","EUR","JPY"};
        List<String> list = new ArrayList<String>();
        list.add("USD");
        list.add("GBP");
        list.add("JPY");
        list.add("INR");
        list.add("EUR");
        list.add("CAD");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item ,items);
        spinner.setAdapter(adapter);

        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cur = spinner.getSelectedItem().toString();
                Thread thread = new Thread(new BitcoinRate());
                thread.start();
                try{
                    thread.join();
                }catch(Exception e){

                }
                text = findViewById(R.id.textView);
                text.setText("Price of 1 Bitcoin is "+price+" "+cur);
                //text.setText(temp);
            }
        });


    }

    private class BitcoinRate implements Runnable{
        public void run(){
            try{
                String secretKey = "NDMwZjMzZjI1YWJkNDY3NDgzZjM3MDM1MDNkMDk2ZTlkMDJjOGJiOTE4YTE0NjhlODQ3ODFmOGZlZmEyNDJmNw";
                String publicKey = "YmFkNTNjOTJiYzIyNDBkNzk2MGY0OTdiNWU4OTg4MGY";
                String signature = getSignature(secretKey, publicKey);

                String url = "https://apiv2.bitcoinaverage.com/convert/global?from=BTC&to="+cur+"&amount=1";
                URL urlObj = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("X-Signature", signature);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = bufferedReader.readLine()) != null) {
                    response.append(inputLine);
                }
                bufferedReader.close();
                String temp;
                double val = 0;
                for(int i=0;i<response.length();i++) {
                    if(response.substring(i).startsWith("price")) {
                        temp = response.substring(i+8);
                        temp = temp.substring(0, temp.indexOf(",")==-1?temp.indexOf("}"):temp.indexOf(","));
                        val = Double.parseDouble(temp);
                        break;
                    }
                }
                price = val;
            }catch(Exception e){
                tempStr += e.getClass().getCanonicalName();
            }
        }

        public String getSignature(String secretKey, String publicKey) throws NoSuchAlgorithmException, InvalidKeyException {

            long timestamp = System.currentTimeMillis() / 1000L;
            String payload = timestamp + "." + publicKey;

            Mac sha256_Mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            sha256_Mac.init(secretKeySpec);
            String hashHex = "";
            try{
            hashHex = byteArrayToHexString(sha256_Mac.doFinal(payload.getBytes()));
            }catch(Exception e){

            }
            String signature = payload + "." + hashHex;
            return signature;
        }

        public String byteArrayToHexString(final byte[] bytes) {
            StringBuilder sb = new StringBuilder();
            for(byte b : bytes){
                sb.append(String.format("%02x", b&0xff));
            }
            return sb.toString();
        }

    }



}
