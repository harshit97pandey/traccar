package org.traccar.socket;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by niko on 2/13/16.
 */

public class SocketEventTest {
    private static String id_hex = "000f333536313733303631363837333032";
    private static String position_hex = "000000000000002b080100000152ba5eb538001ab5e1cb18da520401bf00001000000004020100f000024231d818000000000100000558";

    public static void main(String[] args) throws IOException, InterruptedException {

        String host = "localhost";
        int port = 5027;

        Socket client = new Socket(host, port);

        OutputStream out = client.getOutputStream();
        InputStream inputStream = client.getInputStream();

        //Write Device identifier
        out.write(DatatypeConverter.parseHexBinary(id_hex));

        //Write position
        out.write(DatatypeConverter.parseHexBinary(position_hex));

        out.flush();

        byte[] b = new byte[1024];
        inputStream.read(b);

        client.close();
        System.out.println("Done");
    }
}
