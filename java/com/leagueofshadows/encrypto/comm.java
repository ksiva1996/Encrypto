package com.leagueofshadows.encrypto;

import java.io.File;

/**
 * Created by siva on 5/20/2017.
 */
interface comm {
    void start(int type);
    void stop(int type,int result,File inFile,int id);
}
