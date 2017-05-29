package com.leagueofshadows.encrypto;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;
import java.util.Random;
import java.util.StringTokenizer;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Parent extends AppCompatActivity {

    final private int  keySize = Util.keySize;
    final private int iterations = Util.numberOfIterations;
    private String Password;
    final private byte[] check = {21,22,23,24,25,26,27,28,29,30};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        Boolean set = sp.getBoolean("set",false);
        if(!set)
            setPassword();
        else
            checkPassword();
    }

    private void setPassword()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ENCRYPTO");
        builder.setMessage("This is your first time using the app please set the password to continue");
        View view = getLayoutInflater().inflate(R.layout.set_password,null);
        final EditText pass = (EditText)view.findViewById(R.id.pass);
        final EditText conPass = (EditText)view.findViewById(R.id.conPass);
        builder.setView(view);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = pass.getText().toString();
                String confirmPassword = conPass.getText().toString();
                if(password.equals(confirmPassword))
                {
                    try
                    {
                        SetPassword setPassword = new SetPassword(Parent.this,password);
                        setPassword.execute();
                    }
                    catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void checkPassword()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ENCRYPTO");
        builder.setMessage("Enter the password to continue");
        builder.setCancelable(false);
        View view = getLayoutInflater().inflate(R.layout.check_password,null);
        final EditText pass = (EditText)view.findViewById(R.id.pass);
        builder.setView(view);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = pass.getText().toString();
                try
                {
                    CheckPassword checkpassword = new CheckPassword(Parent.this,password);
                    Password = password;
                    checkpassword.execute();
                }
                catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                    e.printStackTrace();
                }
            }
        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    class SetPassword extends AsyncTask<String,String,Boolean>
    {
        private final Context context;
        private final String password;
        private final SecretKeyFactory factory;
        private final Cipher cipher;
        final ProgressDialog pd;

        SetPassword(Context context,String password) throws NoSuchAlgorithmException, NoSuchPaddingException {
            this.context=context;
            this.password = password;
            pd = new ProgressDialog(context);
            factory = SecretKeyFactory.getInstance(Util.factory);
            cipher = Cipher.getInstance(Util.cipher);
        }

        @Override
        protected void onPreExecute() {
            pd.setTitle("ENCRYPTO");
            pd.setMessage("Working...");
            pd.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean s) {
            if(s) {
                pd.dismiss();
                Toast.makeText(context, "setting password complete", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(context, MainActivity.class);
                i.putExtra("password",password);
                startActivity(i);
                finish();
            }
            else
                Toast.makeText(context, "please try again", Toast.LENGTH_SHORT).show();
            super.onPostExecute(s);
        }

        @Override
        protected Boolean doInBackground(String... p) {

            byte[] salt = new byte[8];
            Random rnd = new Random();
            rnd.nextBytes(salt);
            String saltString = getstring(salt);
            Log.e("saltString",saltString);
            SharedPreferences sp = getSharedPreferences("preferences",Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sp.edit();
            edit.putString("saltString",saltString);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt,iterations, keySize);
            SecretKey secretKey;
            try
            {
                secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                AlgorithmParameters params = cipher.getParameters();
                byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();

                String ivString = getstring(iv);
                edit.putString("ivString",ivString);
                Log.e("ivString",ivString);
                byte[] out  = useKeyOnData(check);
                String encrypted = getstring(out);
                edit.putString("encrypted",encrypted);
                edit.putBoolean("set",true);
                edit.apply();
                return true;
            }
            catch (InvalidKeySpecException | InvalidKeyException e)
            {
                e.printStackTrace();
                return false;
            } catch (InvalidParameterSpecException e) {
                e.printStackTrace();
                return false;
            }
        }
        byte[] useKeyOnData(byte[] inBytes)  {
            try
            {
                byte[] out = cipher.update(inBytes, 0,inBytes.length);
                byte[] finalout = cipher.doFinal();
                byte[] output = new byte[out.length+finalout.length];
                System.arraycopy(out, 0, output, 0, out.length);
                System.arraycopy(finalout, 0, output, out.length, finalout.length);
                return output;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return null;
            }
        }
    }

    private String getstring(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        int x;
        for(int i=0;i<bytes.length-1;i++)
        {
            x = bytes[i];
            builder.append(Integer.toString(x)).append("+");
        }
        x = bytes[bytes.length-1];
        builder.append(Integer.toString(x));
        return builder.toString();
    }

    private byte[] getbytes(String s)
    {
        StringTokenizer st = new StringTokenizer(s,"+");
        byte[] bytes = new byte[st.countTokens()];
        int i=0;
        while(st.hasMoreTokens())
        {
            int x= Integer.parseInt(st.nextToken());
            bytes[i]= (byte) x;
            i++;
        }
        return bytes;
    }

    class CheckPassword extends AsyncTask<String,String,Boolean>
    {

        private final Context context;
        private final String password;
        private final SecretKeyFactory factory;
        private final Cipher cipher;
        final ProgressDialog pd;

        CheckPassword(Context context,String password) throws NoSuchAlgorithmException, NoSuchPaddingException {
            this.context=context;
            this.password = password;
            pd = new ProgressDialog(context);
            factory = SecretKeyFactory.getInstance(Util.factory);
            cipher = Cipher.getInstance(Util.cipher);
        }

        @Override
        protected void onPreExecute() {
            pd.setTitle("ENCRYPTO");
            pd.setMessage("Checking...");
            pd.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean s) {
            if(s)
            {
                pd.dismiss();
                Toast.makeText(context, "Verification successful", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(context, MainActivity.class);
                i.putExtra("password",Password);
                startActivity(i);
                finish();
            }
            else {
                pd.dismiss();
                checkPassword();
                Toast.makeText(context, "Wrong password", Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(s);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            SharedPreferences sp = getSharedPreferences("preferences",Context.MODE_PRIVATE);
            String saltString = sp.getString("saltString",null);
            byte[] salt = getbytes(saltString);
            String ivString = sp.getString("ivString",null);
            byte[] iv = getbytes(ivString);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt,iterations,keySize);
            SecretKey secretKey;
            try
            {
                secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
                cipher.init(Cipher.DECRYPT_MODE, secretKey,new IvParameterSpec(iv));
                String encrypted = sp.getString("encrypted",null);
                byte[] inBytes = getbytes(encrypted);
                byte[] out = useKeyOnData(inBytes);
                return Arrays.equals(out,check);
            }
            catch (InvalidKeySpecException | InvalidKeyException e)
            {
                e.printStackTrace();
                return false;
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
                return false;
            }
        }

        private byte[] useKeyOnData(byte[] inBytes) {
            try
            {
                byte[] out = cipher.update(inBytes, 0,inBytes.length);
                byte[] finalout = cipher.doFinal();
                byte[] output = new byte[out.length+finalout.length];
                System.arraycopy(out, 0, output, 0, out.length);
                System.arraycopy(finalout, 0, output, out.length, finalout.length);
                return output;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return null;
            }
        }
    }
}
