package hu.esamu.basic.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import hu.esamu.basic.server.proto.*;
import java.io.File;
import org.apache.commons.io.FileUtils;

/**
 * This is a simple TPC server
 *
 * @author Jakab Ádám
 *
 */
class Server {

    private Socket socket = null;
    private ServerSocket server = null;
    private static List<Socket> onlinePlayers = new ArrayList<>();

    /**
     * Here will be binded the port, and listen for new clients
     *
     * @param port This will be the port of the server
     *
     */
    Server(int port) {
        try {
            System.out.println("Binding to port " + port + "...");
            InetAddress addr = InetAddress.getByName("192.168.0.102");
            server = new ServerSocket(port, 50, addr);
            //server = new ServerSocket(port);
            System.out.println("Server started: " + server);
            while (true) {
                try {
                    System.out.println("Waiting for players...");
                    socket = server.accept();
                    HandleClients hc = new HandleClients(socket);
                    hc.start();
                    onlinePlayers.add(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This class will handle the connected clients. Every one ot them will get
     * its own thread
     *
     */
    private class HandleClients extends Thread {

        private ObjectOutputStream out = null;
        private ObjectInputStream in = null;
        private Socket socket = null;

        HandleClients(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            Object inputObject;
            try {
                System.out.println("Client accepted: " + socket);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                System.out.println("Communication started!");

                while ((inputObject = in.readObject()) != null) {
                    parseAndSendMessage(inputObject);
                }

                if (socket != null) {
                    socket.close();
                }
                if (in != null) {
                    in.close();
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

        }

        /**
         * This method will parse the input messages.
         *
         * @param inputObject This is the object which we get from the clients
         * @exception IOException
         *
         */
        private void parseAndSendMessage(Object inputObject) throws IOException {
            ProtoPicture.Picture protoPicture = ProtoPicture.Picture.parseFrom((byte[]) inputObject);

            byte[] outputFileArray = protoPicture.getImage().toByteArray();
            File theDir = new File(System.getProperty("user.home"), "prototest");
            theDir.mkdir();
            File outputImage = new File(theDir, "output.jpg");
            FileUtils.writeByteArrayToFile(outputImage, outputFileArray);
            
            System.out.println("latitude " + protoPicture.getLatitude());
            System.out.println("longitude " + protoPicture.getLongitude());

            /*
            if(inputObject instanceof String){
                System.out.println(inputObject + " | FROM: " + socket);
                out.writeObject("Server got your message! Your message: " + inputObject);
                out.flush();
            }
             */
        }

        /**
         * This utility class will redirect the headers of the messages. Only
         * usable when two clients communicate trough the server.
         *
         */
        class AppendingObjectOutputStream extends ObjectOutputStream {

            AppendingObjectOutputStream(OutputStream out) throws IOException {
                super(out);
            }

            @Override
            protected void writeStreamHeader() throws IOException {
                reset();
            }

        }

    }

}
