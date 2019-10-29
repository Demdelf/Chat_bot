package main;

import main.Connection;
import main.ConsoleHelper;
import main.Message;
import main.MessageType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<String, Connection>();

    public static void sendBroadcastMessage(Message message){
        for(Map.Entry<String, Connection> c : connectionMap.entrySet()){
            try {
                c.getValue().send(message);
            } catch (IOException e) {
                System.out.println("Error");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ConsoleHelper consoleHelper = new ConsoleHelper();
        System.out.println("Введите номер порта:");
        ServerSocket serverSocket = new ServerSocket(consoleHelper.readInt());
        //System.out.println(serverSocket.getLocalPort());
        //System.out.println( serverSocket.getInetAddress());
        System.out.println("Сервер запущен");
        while (true){
            try {
                new Handler(serverSocket.accept()).start();
            } catch (Exception e){
                serverSocket.close();
                break;
                //System.out.println(e);
            }

        }
    }

    private static class Handler extends Thread{
        Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException{

            while (true){

                connection.send(new Message(MessageType.NAME_REQUEST));
                Message message = connection.receive();
                String s = message.getData();
                if(message.getType().equals(MessageType.USER_NAME) && !s.isEmpty() &&
                        !connectionMap.containsKey(s)){

                    connectionMap.put(s, connection);
                    connection.send(new Message(MessageType.NAME_ACCEPTED));
                    return connection.receive().getData();
                } else continue;
            }

        }

        private void notifyUsers(Connection connection, String userName) throws IOException{
            for(Map.Entry<String, Connection> c : connectionMap.entrySet()){
                if (!userName.equals(c.getKey()))
                    connection.send(new Message(MessageType.USER_ADDED, c.getKey()));

            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{
            Message messageFrom;
            while (true){
                messageFrom = connection.receive();
                if (messageFrom.getType() == MessageType.TEXT){
                    sendBroadcastMessage(new Message(MessageType.TEXT,userName + ": " + messageFrom.getData()));
                } else new ConsoleHelper().writeMessage("Error");
            }
        }

        public void run(){
            System.out.println(socket.getRemoteSocketAddress());
            try {
                Connection connection = new Connection(socket);
                String userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));

            } catch (IOException e) {
                new ConsoleHelper().writeMessage("IOException");
            } catch (ClassNotFoundException e) {
                new ConsoleHelper().writeMessage("ClassNotFoundException");
            }
            new ConsoleHelper().writeMessage("соединение с удаленным адресом закрыто");

        }
    }
}
