package com.leagueofshadows.encrypto;

class FileItem {
    private String name;
    private String originalpath;
    private String size;
    private String newpath;
    private int id;

    FileItem(int id,String name,String originalpath,String newpath,String size)
    {
        this.id=id;
        this.name=name;
        this.newpath=newpath;
        this.originalpath=originalpath;
        this.size=size;
    }


    String getName() {
        return name;
    }

    String getOriginalpath() {
        return originalpath;
    }

    String getSize() {
        return size;
    }

    String getNewpath() {
        return newpath;
    }

    int getId() {
        return id;
    }
}
