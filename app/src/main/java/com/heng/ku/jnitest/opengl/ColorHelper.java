package com.heng.ku.jnitest.opengl;

/**
 * https://github.com/WangShuo1143368701/WSLiveDemo/blob/master/libWSLive/src/main/java/me/lake/librestreaming/core/ColorHelper.java
 */
public class ColorHelper {
    static {
        System.loadLibrary("native-color-helper");
    }

    static public native void NV21TOYUV420SP(byte[] src, byte[] dst, int YSize);

    static public native void NV21TOYUV420P(byte[] src, byte[] dst, int YSize);

    static public native void YUV420SPTOYUV420P(byte[] src, byte[] dst, int YSize);

    static public native void NV21TOARGB(byte[] src, int[] dst, int width, int height);

    static public native void FIXGLPIXEL(int[] src, int[] dst, int width, int height);
    public  static native void NV21TOYUV(byte[] src, byte[] dstY, byte[] dstU, byte[] dstV, int width, int height);
    //slow
    static public native void NV21Transform(byte[] src, byte[] dst, int srcwidth, int srcheight, int directionFlag);
}
