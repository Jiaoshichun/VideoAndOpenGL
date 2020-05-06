package com.heng.ku.lib;

public class Test {
    public static void main(String[] args){
        System.out.println("-1<<31 :"+(-1<<31));
        System.out.println("-1<<31 :"+((-1<<31) | 0));
        System.out.println("-1>>1 :"+(-1>>1));
        System.out.println("-1>>>1 :"+(-1>>>1));
    }
}
