package com.monitor.main;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }


    public int getUnsignedByte (byte data){      //将data字节型数据转换为0~255 (0xFF 即BYTE)。
        return data&0x0FF;
    }

    public int getUnsignedByte (short data){      //将data字节型数据转换为0~65535 (0xFFFF 即 WORD)。
        return data&0x0FFFF;
    }

    public long getUnsignedIntt (int data){     //将int数据转换为0~4294967295 (0xFFFFFFFF即DWORD)。
        return data&0x0FFFFFFFFl;
    }



    @Test

    public void ddd (){

        System.out.println(0xaa);
        System.out.println(
              Integer.parseInt("aa",16)
        );


    }

    @Test
    public void testUnSigne(){
        int aa = 0xaa;
        System.out.println(aa);
        System.out.println(Integer.toBinaryString(aa));

        int bb = (aa & 255);
        System.out.println(bb);

    }

    @Test
    public void testSend()  {
        System.out.println("test");

        try {
            Socket socket = new Socket("10.10.10.1", 8080);
            System.out.println(socket);


            OutputStream outputStream = socket.getOutputStream();


            outputStream.write(0x55 & 0xff);
            outputStream.write(0x55& 0xff);
            outputStream.write(0x55& 0xff);

            outputStream.write(0x01& 0xff);
            outputStream.write(0x01& 0xff);
            outputStream.write(0x01& 0xff);
            outputStream.write(0x01& 0xff);

            outputStream.write(0xaa& 0xff);
            outputStream.write(0xaa& 0xff);
            outputStream.write(0xaa& 0xff);


            outputStream.flush();

            InputStream inputStream = socket.getInputStream();

            System.out.println("available" + inputStream.available());

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            System.out.println("begin reader");

            while ((line = bufferedReader.readLine()) != null){
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Test
    public void testConnect()  {
        System.out.println("test");

        try {
            Socket socket = new Socket("192.168.99.243", 8000);
            System.out.println(socket);


            OutputStream outputStream = socket.getOutputStream();

            outputStream.write(0x55);
            outputStream.write(0x55);
            outputStream.write(0x55);

            outputStream.write(0x01);
            outputStream.write(0x01);
            outputStream.write(0x01);
            outputStream.write(0x01);

            outputStream.write(0xaa);
            outputStream.write(0xaa);
            outputStream.write(0xaa);


            outputStream.flush();

            InputStream inputStream = socket.getInputStream();

            System.out.println("available" + inputStream.available());

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            System.out.println("begin reader");

            while ((line = bufferedReader.readLine()) != null){
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}