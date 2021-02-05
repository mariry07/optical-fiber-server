package com.optical.common;

import org.springframework.util.StringUtils;

import java.io.*;

/**
 * Created by mary on 2021/1/5.
 */
public class EncodeUtil {

    File f = new File("d:\\1");

    public byte[] writeInByte() throws IOException {
        FileInputStream input = new FileInputStream("d:\\1");
        byte[] buffer = new byte[100];
        int len = input.read(buffer);
        input.close();
        return buffer;
    }


    public String writeInFile() throws IOException{
        String str = "";
        String count = "";
        try {
            // 使用字符流对文件进行读取
            BufferedReader bf = new BufferedReader(new FileReader(f));
            while (true) {
                if ((count = bf.readLine()) != null) {
                    str += count;
                } else {
                    break;
                }
            }
            bf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return str;
    }


    public static String toBinaryString(byte[] var0) {
        String var1 = "";

        for(int var2 = 0; var2 < var0.length; ++var2) {
            byte var3 = var0[var2];

            for(int var4 = 0; var4 < 8; ++var4) {
                int var5 = var3 >>> var4 & 1;
                var1 = var1 + var5;
            }

            if (var2 != var0.length - 1) {
                var1 = var1 + " ";
            }
        }

        return var1;
    }

    public String binary2Decimal(byte[] bytArr) {
        StringBuilder sb = new StringBuilder();

        return sb.toString();
    }

    public static String binary2Hex(byte[] bytArr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytArr.length; i++) {
            int v = bytArr[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                sb.append(0);
            }
            sb.append(hv);
        }
        return sb.toString();
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (StringUtils.isEmpty(hexString)) {
            return null;
        }
        hexString = hexString.toLowerCase();
        final byte[] byteArray = new byte[hexString.length() >> 1];
        int index = 0;
        for (int i = 0; i < hexString.length(); i++) {
            if (index  > hexString.length() - 1) {
                return byteArray;
            }
            byte highDit = (byte) (Character.digit(hexString.charAt(index), 16) & 0xFF);
            byte lowDit = (byte) (Character.digit(hexString.charAt(index + 1), 16) & 0xFF);
            byteArray[i] = (byte) (highDit << 4 | lowDit);
            index += 2;
        }
        return byteArray;
    }

//        public static byte hexStrToByte(String hexbytein){
//            return (byte)Integer.parseInt(hexbytein,16);
//        }

    public static void main(String[] args) throws IOException{

        System.out.println(" here started test main!");


        EncodeUtil test=new EncodeUtil();
        //将字符串输入到文件中
        //test.getReader();
        //读取相对应的字符串
        String str=test.writeInFile();
        //将文件中内容在控制台输出
        System.out.println("文件内容为："+str);

        String byteString = test.toBinaryString(test.writeInByte());
        System.out.println("byteString 打印出来： " + byteString);

        String hexString = test.binary2Hex(test.writeInByte());
        System.out.println("16进制 打印出来： " + hexString);


        String encodeFormat = "utf8";

        String s = new String(test.writeInByte(), encodeFormat);

        System.out.println("编码格式：" + encodeFormat + ", 结果:"+s) ;

    }



}
