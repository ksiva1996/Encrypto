package com.leagueofshadows.encrypto;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


public class DecryptMuitiple  extends AsyncTask<String,Boolean,Boolean> {


    private ArrayList<FileItem> fileItems;
    private String Password;
    private final SecretKeyFactory factory;
    private final Cipher cipher;
    private comm comm;
    private Db db;

    DecryptMuitiple(Context context,ArrayList<FileItem> fileItems, String Password) throws NoSuchAlgorithmException, NoSuchPaddingException {
        comm = (comm) context;
        this.fileItems = fileItems;
        this.Password = Password;
        factory = SecretKeyFactory.getInstance(Util.factory);
        cipher = Cipher.getInstance(Util.cipher);
        db = new Db(context);
    }

    @Override
    protected void onPreExecute() {
        comm.start(2);
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean s) {

        if(s)
        {
            comm.stop(3,1,null,-1);
        }
        else
        {
            comm.stop(3,1,null,-1);
        }
        super.onPostExecute(s);
    }

    @Override
    protected Boolean doInBackground(String... p) {

        try
        {
            for(int i=0;i<fileItems.size();i++) {
                FileItem f = fileItems.get(i);
                File inFile = new File(f.getNewpath());
                File outFile = new File(f.getOriginalpath());
                FileInputStream inputFile = new FileInputStream(inFile);
                FileOutputStream outputFile = new FileOutputStream(outFile);

                byte[] salt = new byte[8];
                int x = inputFile.read(salt);

                byte[] iv = new byte[16];
                x = inputFile.read(iv);

                SecretKey secretkey = makeKey(Password, salt);
                cipher.init(Cipher.DECRYPT_MODE, secretkey, new IvParameterSpec(iv));
                useKeyOnData(inputFile, outputFile);
                db.deleteFile(f.getId());
                inFile.delete();
                inputFile.close();
                outputFile.flush();
                outputFile.close();

            }
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private SecretKey makeKey(String password, byte[] salt) throws InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt,Util.numberOfIterations, Util.keySize);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    private void useKeyOnData(FileInputStream inFile, FileOutputStream outFile)  {
        try {
            byte[] input = new byte[4096];
            int bytesRead;
            while ((bytesRead = inFile.read(input)) != -1) {
                byte[] output = cipher.update(input, 0, bytesRead);
                if (output != null) {
                    outFile.write(output);
                }
            }
            byte[] output = cipher.doFinal();
            if (output != null) {
                outFile.write(output);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
