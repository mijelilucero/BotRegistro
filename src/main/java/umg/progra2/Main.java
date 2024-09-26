package umg.progra2;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import umg.progra2.botTelegram.BotCuestionario;
import umg.progra2.botTelegram.BotPregunton;
import umg.progra2.botTelegram.BotRegistra;

public class Main {
    public static void main(String[] args) {

        try{
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            BotCuestionario bot = new BotCuestionario();

            botsApi.registerBot(bot);
            System.out.println("El bot está funcionando...");
        }
        catch(Exception ex){
            System.out.println("Error: "+ex.getMessage());
        }

    }
}