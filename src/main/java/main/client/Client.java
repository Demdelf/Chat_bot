package main.client;



import main.Connection;
import main.ConsoleHelper;
import main.Message;
import main.MessageType;

import java.io.IOException;
import java.net.Socket;

import static main.MessageType.NAME_REQUEST;


public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;

    protected String getServerAddress(){
        new ConsoleHelper().writeMessage("ввод адреса сервера");
        return new ConsoleHelper().readString();
    }

    protected int getServerPort() throws IOException {
        new ConsoleHelper().writeMessage("ввод порта сервера");
        return new ConsoleHelper().readInt();
    }
    protected String getUserName(){
        new ConsoleHelper().writeMessage("ввод имя пользователя");
        return new ConsoleHelper().readString();
    }
    protected boolean shouldSendTextFromConsole(){
        return true;
    }
    protected SocketThread getSocketThread(){
        return new SocketThread();
    }
    protected void sendTextMessage(String text){
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            new ConsoleHelper().writeMessage("IOException");
            clientConnected = false;
        }
    }

    public class SocketThread extends Thread{
        protected void processIncomingMessage(String message){
            new ConsoleHelper().writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName){
            new ConsoleHelper().writeMessage(userName + "joined to chat");
        }
        protected void informAboutDeletingNewUser(String userName){
            new ConsoleHelper().writeMessage(userName + "left chat");
        }
        protected void notifyConnectionStatusChanged(boolean clientConnected){
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this){
                Client.this.notify();
            }
        }
        protected void clientHandshake() throws IOException, ClassNotFoundException{
            while (true){
                Message message = connection.receive();
                if (message.getType()==NAME_REQUEST) {
                    String userName = getUserName();
                    connection.send(new Message(MessageType.USER_NAME, userName));
                } else if(message.getType()==MessageType.NAME_ACCEPTED){
                    notifyConnectionStatusChanged(true);
                    break;
                }
                else{
                    throw new IOException("Unexpected main.MessageType");
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException{
            while (true){
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT ){
                    processIncomingMessage(message.getData());
                }
                else if (message.getType() == MessageType.USER_ADDED) {
                    informAboutAddingNewUser(message.getData());
                }
                else if (message.getType() == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(message.getData());
                }
                else {
                    throw new IOException("Unexpected main.MessageType");
                }
            }


        }
        public void run(){
            try {
                connection = new Connection(new Socket(getServerAddress(), getServerPort()));
                clientHandshake();
                clientMainLoop();
            } catch (IOException e) {
                notifyConnectionStatusChanged(false);
            } catch (ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }

        }

    }

    public void run() throws InterruptedException {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        synchronized (this){
            try{
                this.wait();
                this.notify();
            } catch (Exception e){
                new ConsoleHelper().writeMessage("Exception");

            }
        }


        if (clientConnected == true) new ConsoleHelper().
                writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
        else {
            new ConsoleHelper().
                    writeMessage("Произошла ошибка во время работы клиента.");
        }
        while (clientConnected == true){
            String consoleMessage = new ConsoleHelper().readString();
            if (consoleMessage.equals("exit")) break;
            else {
                if (shouldSendTextFromConsole() == true) sendTextMessage(consoleMessage);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Client client = new Client();
        client.run();
    }
}
