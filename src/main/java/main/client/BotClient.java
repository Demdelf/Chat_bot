package main.client;

import main.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BotClient extends Client {
    @Override
    protected String getUserName() {
        return "date_bot_" + (int) (Math.random()*100);
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    public static void main(String[] args) throws InterruptedException {
        new BotClient().run();
    }

    public class BotSocketThread extends SocketThread{

        @Override
        protected void processIncomingMessage(String message) {
            new ConsoleHelper().writeMessage(message);

            if (!message.contains(": ")) return;
            String[] nameText = message.split(":");
            SimpleDateFormat simpleDateFormat;
            Calendar calendar = Calendar.getInstance();
            switch (nameText[1].trim()) {
                case "дата":
                    simpleDateFormat = new SimpleDateFormat("d.MM.YYYY");
                    sendTextMessage(String.format("Информация для %s: %s", nameText[0],
                            simpleDateFormat.format(calendar.getTime())));
                    break;
                case "день":
                    simpleDateFormat = new SimpleDateFormat("d");
                    sendTextMessage(String.format("Информация для %s: %s", nameText[0],
                            simpleDateFormat.format(calendar.getTime())));
                    break;
                case "месяц":
                    simpleDateFormat = new SimpleDateFormat("MMMM");
                    sendTextMessage(String.format("Информация для %s: %s", nameText[0],
                            simpleDateFormat.format(calendar.getTime())));
                    break;
                case "год":
                    simpleDateFormat = new SimpleDateFormat("YYYY");
                    sendTextMessage(String.format("Информация для %s: %s", nameText[0],
                            simpleDateFormat.format(calendar.getTime())));
                    break;
                case "время":
                    simpleDateFormat = new SimpleDateFormat("H:mm:ss");
                    sendTextMessage(String.format("Информация для %s: %s", nameText[0],
                            simpleDateFormat.format(calendar.getTime())));
                    break;
                case "час":
                    simpleDateFormat = new SimpleDateFormat("H");
                    sendTextMessage(String.format("Информация для %s: %s", nameText[0],
                            simpleDateFormat.format(calendar.getTime())));
                    break;
                case "минуты":
                    simpleDateFormat = new SimpleDateFormat("m");
                    sendTextMessage(String.format("Информация для %s: %s", nameText[0],
                            simpleDateFormat.format(calendar.getTime())));
                    break;
                case "секунды":
                    simpleDateFormat = new SimpleDateFormat("s");
                    sendTextMessage(String.format("Информация для %s: %s", nameText[0],
                            simpleDateFormat.format(calendar.getTime())));
                    break;
                /*default:
                    simpleDateFormat = null;
                    break;*/
            }
        }

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }
    }
}
