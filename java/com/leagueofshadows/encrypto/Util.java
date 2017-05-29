package com.leagueofshadows.encrypto;


import android.os.Environment;

class Util
{
    static final int keySize = 128;
    static final int numberOfIterations = 1000;
    static final String factory = "PBKDF2WithHmacSHA1";
    static final String cipher = "AES/CBC/PKCS5Padding";
    static final String path = Environment.getExternalStorageDirectory().getPath()+"/Encrypto/";
    static final String sdcard = Environment.getExternalStorageDirectory().getPath();
    static final String output = Environment.getExternalStorageDirectory().getPath()+"/Encrypto/Outputs/";
}
