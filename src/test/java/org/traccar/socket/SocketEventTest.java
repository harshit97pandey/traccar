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
    private static String position_hex = "000000000000011b080700000152b5541e98001ab5d9ae18da35d201b500f61100200004020100f0010242391a18001b000000000152b55416c8001ab5e0a718da387601b600ec1100180004020100f00102423901180013000000000152b55412e0001ab5e2dc18da39c701b600dd1100120004020100f001024238f3180014000000000152b5540ef8001ab5e42118da3b4601b700c71100120004020100f001024238e0180017000000000152b5540b10001ab5e45618da3d4e01b700b21100160004020100f001024238d218001b000000000152b553ebd0001ab5dc6018da518a01b700a81000130004020100f001024238c118000d000000000152b5" +
            "53e400001ab5dbff18da54b401b700b40f000b0004020100f0010242389c18000400000700000da0";

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
