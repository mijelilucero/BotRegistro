package umg.progra2.botTelegram;


import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import umg.progra2.model.Respuesta;
import umg.progra2.model.User;
import umg.progra2.service.RespuestaService;
import umg.progra2.service.UserService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BotCuestionario extends TelegramLongPollingBot {

    private Map<Long, String> estadoConversacion = new HashMap<>();
    User usuarioConectado = null;
    UserService userService = new UserService();
    RespuestaService respuestaService = new RespuestaService();

    private final Map<Long, Integer> indicePregunta = new HashMap<>();
    private final Map<Long, String> seccionActiva = new HashMap<>();
    private final Map<String, String[]> preguntas = new HashMap<>();

    @Override
    public String getBotUsername() {
        return "@MiloMind_bot";
    }

    @Override
    public String getBotToken() {
        return "7400553095:AAFQjfYLT-k9jsl2t-lU3OQrYABu9VA4DSU";
    }

    public BotCuestionario() {
        // Inicializa los cuestionarios con las preguntas.
        preguntas.put("SECTION_1", new String[]{"🤦‍♂️1.1- Estas aburrido?", "😂😂 1.2- Te bañaste hoy?", "🤡🤡 Pregunta 1.3-Esta es una broma!"});
        preguntas.put("SECTION_2", new String[]{"2.1- Cuál es tu superpoder secreto?", "2.2- Si fueras un animal, cuál serías?", "2.3- Cuál es tu snack favorito de la infancia?"});
        preguntas.put("SECTION_3", new String[]{"3.1- Café o té?", "3.2- Playa o montaña?", "3.3- Perros o gatos?"});
        preguntas.put("SECTION_4", new String[]{"4.1- Cuál es tu nombre completo?", "4.2.- Cuántos años tienes?", "4.3- Cuál es tu fecha de nacimiento?"});
    }



    @Override
    public void onUpdateReceived(Update update) {

        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String userFirstName = update.getMessage().getFrom().getFirstName();
                String userLastName = update.getMessage().getFrom().getLastName();
                String nickName = update.getMessage().getFrom().getUserName();
                long chat_id = update.getMessage().getChatId();
                String mensaje_Texto = update.getMessage().getText();

                String state = estadoConversacion.getOrDefault(chat_id, "");
                usuarioConectado = userService.getUserByTelegramId(chat_id);

                if (usuarioConectado == null && state.isEmpty()) {
                    sendText(chat_id, "Hola " + formatUserInfo(userFirstName, userLastName, nickName) + ", no tienes un usuario registrado en el sistema. Por favor ingresa tu correo electrónico:");
                    estadoConversacion.put(chat_id, "ESPERANDO_CORREO");
                    return;
                }

                if (state.equals("ESPERANDO_CORREO")) {
                    processEmailInput(chat_id, mensaje_Texto);
                    return;
                }

                if(usuarioConectado != null && state.isEmpty() && !estadoConversacion.containsKey(chat_id)){
                    sendText(chat_id, "Hola " + formatUserInfo(userFirstName, userLastName, nickName) + ", envía /menu para iniciar el cuestionario.");
                    estadoConversacion.put(chat_id, "ESPERANDO_MENU");
                    return;
                }

                if (mensaje_Texto.equals("/menu")) {
                    sendMenu(chat_id);
                    estadoConversacion.put(chat_id, "MENU_ENVIADO");
                } else if (seccionActiva.containsKey(chat_id)) {
                    manejaCuestionario(chat_id, mensaje_Texto);
                }
            } else if (update.hasCallbackQuery()) {
                String callbackData = update.getCallbackQuery().getData();
                long chatId = update.getCallbackQuery().getMessage().getChatId();
                inicioCuestionario(chatId, callbackData);
            } else {
                System.out.println("Tipo de actualización no soportado: " + update);
            }
        } catch (Exception e) {
            long chat_id = update.getMessage().getChatId();
            sendText(chat_id, "Ocurrió un error al procesar tu mensaje. Por favor intenta de nuevo.");
        }
    }


    private String formatUserInfo(String firstName, String lastName, String userName) {
        return firstName + " " + lastName + " (" + userName + ")";
    }


    private void processEmailInput(long chat_id, String email) {
        sendText(chat_id, "Recibo su Correo: " + email);
        estadoConversacion.remove(chat_id);

        try{
            usuarioConectado = userService.getUserByEmail(email);
        } catch (Exception e) {
            System.err.println("Error al obtener el usuario por correo: " + e.getMessage());
            e.printStackTrace();
        }

        if (usuarioConectado == null) {
            sendText(chat_id, "El correo no se encuentra registrado en el sistema, por favor contacte al administrador o envie otro mensaje para intentarlo de nuevo.");
        } else {
            usuarioConectado.setTelegramid(chat_id);

            try {
                userService.updateUser(usuarioConectado);
            } catch (Exception e) {
                System.err.println("Error al actualizar el usuario: " + e.getMessage());
                e.printStackTrace();
            }

            sendText(chat_id, "Usuario actualizado con éxito!");
            sendText(chat_id, "Por favor, envíe un mensaje nuevamente para continuar.");
        }
    }


    private void sendMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Selecciona una sección:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(crearFilaBoton("Sección 1", "SECTION_1"));
        rows.add(crearFilaBoton("Sección 2", "SECTION_2"));
        rows.add(crearFilaBoton("Sección 3", "SECTION_3"));
        rows.add(crearFilaBoton("Sección 4", "SECTION_4"));

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private List<InlineKeyboardButton> crearFilaBoton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        return row;
    }


    private void inicioCuestionario(long chatId, String section) {
        seccionActiva.put(chatId, section);
        indicePregunta.put(chatId, 0);
        enviarPregunta(chatId);
    }


    private void enviarPregunta(long chatId) {
        String seccion = seccionActiva.get(chatId);
        int index = indicePregunta.get(chatId);
        String[] questions = preguntas.get(seccion);

        if (index < questions.length) {
            sendText(chatId, questions[index]);
        } else {
            sendText(chatId, "¡Has completado el cuestionario!");
            seccionActiva.remove(chatId);
            indicePregunta.remove(chatId);
        }
    }


    private void manejaCuestionario(long chatId, String response) {
        String section = seccionActiva.get(chatId);
        int index = indicePregunta.get(chatId);
        Timestamp fechaActual = new Timestamp(System.currentTimeMillis());

        if(section.equals("SECTION_4") && index==1){
            try {
                int edad = Integer.parseInt(response);

                if(edad < 0 || edad > 100){
                    indicePregunta.put(chatId, 1);
                    sendText(chatId, "Ingrese una edad válida.");

                    enviarPregunta(chatId);
                    return;
                }else{
                    guardarRespuesta(section,chatId, index, response, fechaActual);
                    sendText(chatId, "Tu respuesta fue: " + response);
                    indicePregunta.put(chatId, index + 1);

                    enviarPregunta(chatId);
                    return;
                }
            } catch (NumberFormatException e) {
                sendText(chatId, "Ingrese un dato numérico para la edad.");

                enviarPregunta(chatId);
                return;
            }
        }else{
            guardarRespuesta(section,chatId, index, response, fechaActual);
            sendText(chatId, "Tu respuesta fue: " + response);
            indicePregunta.put(chatId, index + 1);

            enviarPregunta(chatId);
        }
    }


    public void guardarRespuesta(String seccion, Long telegramId, int preguntaId, String respuestaTexto, Timestamp fechaRespuesta){
        try {
            Respuesta respuesta = new Respuesta();

            respuesta.setSeccion(seccion);
            respuesta.setTelegramId(telegramId);
            respuesta.setPreguntaId(preguntaId);
            respuesta.setRespuestaTexto(respuestaTexto);
            respuesta.setFechaRespuesta(fechaRespuesta);

            respuestaService.createRespuesta(respuesta);
        }catch (Exception e){
            System.err.println("Error al guardar la respuesta: " + e.getMessage());
            e.printStackTrace();

            sendText(telegramId, "Ha ocurrido un error al intentar guardar su respuesta.");
        }
    }


    public void sendText(Long who, String what){
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString())
                .text(what).build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
