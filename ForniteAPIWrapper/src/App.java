import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.InputMismatchException;
import java.util.Scanner;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonWriter;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class App {
    public static String nombrePlayerUrl = "";
    // String apiUrl = Config.API_URL;
    String authToken = Config.AUTHORIZATION_HEADER;
    String playerId = "";
    String playerName = "";
    String lastModified = "";
    int score;
    int wins;
    int top3;
    int top5;
    int kills;
    Double killsPerMin;
    Double killsPerMatch;
    int deaths;
    Double kd;
    int matches;
    Double winRate;
    int minutesPlayed;
    int playersOutlived;
    int battlePassLevel;
    public boolean error;
    boolean errorDevice;
    boolean errorGame;
    boolean excp;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public void asciiArt() {
        System.out.println(
                "    ______                 _ __          ___    ____  ____   _       __                                \r\n"
                        + //
                        "   / ____/___  _________  (_) /____     /   |  / __ \\/  _/  | |     / /________ _____  ____  ___  _____\r\n"
                        + //
                        "  / /_  / __ \\/ ___/ __ \\/ / __/ _ \\   / /| | / /_/ // /    | | /| / / ___/ __ `/ __ \\/ __ \\/ _ \\/ ___/\r\n"
                        + //
                        " / __/ / /_/ / /  / / / / / /_/  __/  / ___ |/ ____// /     | |/ |/ / /  / /_/ / /_/ / /_/ /  __/ /    \r\n"
                        + //
                        "/_/    \\____/_/  /_/ /_/_/\\__/\\___/  /_/  |_/_/   /___/     |__/|__/_/   \\__,_/ .___/ .___/\\___/_/     \r\n"
                        + //
                        "                                                                             /_/   /_/                 \r\n"
                        + //
                        "\r\n" + //
                        "");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.print(ANSI_PURPLE + "[+] Player Name: " + ANSI_RESET + "\n\n");
            nombrePlayerUrl = reader.readLine();
        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
        }

    }

    public JsonValue leerHttps(String direccion) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(direccion)
                .addHeader("Authorization", authToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }

            try (JsonReader reader = Json.createReader(response.body().byteStream())) {
                return reader.read();
            }
        }
    }

    public void escribeJSON(JsonValue json, File f) throws IOException {
        System.out.println("Guardando tipo: " + json.getValueType());
        try (PrintWriter pw = new PrintWriter(f);
                JsonWriter writer = Json.createWriter(pw)) {

            if (json.getValueType() == JsonValue.ValueType.OBJECT) {
                writer.writeObject(json.asJsonObject());
            } else if (json.getValueType() == JsonValue.ValueType.ARRAY) {
                writer.writeArray(json.asJsonArray());
            } else {
                System.out.println("No se soporta la escritura");
            }
        }
    }

    public JsonValue leeJSON(String ruta) {
        try {
            if (ruta.toLowerCase().startsWith("http://")) {
                return leerHttp(ruta);
            } else if (ruta.toLowerCase().startsWith("https://")) {
                return leerHttps(ruta);
            } else {
                return leerFichero(ruta);
            }
        } catch (IOException e) {
            System.out.println("Error procesando documento Json " +
                    e.getLocalizedMessage());
            return null;
        }
    }

    public JsonValue leerFichero(String ruta) throws FileNotFoundException {
        try (JsonReader reader = Json.createReader(new FileReader(ruta))) {
            return reader.read();
            /*
             * JsonStructure jsonSt = reader.read();
             * System.out.println(jsonSt.getValueType());
             * JsonObject jsonObj = reader.readObject();
             * System.out.println(jsonObj.getValueType());
             * JsonArray jsonArr = reade r.readArray();
             * System.out.println(jsonArr.getValueType());
             */
        }
    }

    public JsonValue leerHttp(String direccion) throws IOException {
        URL url = new URL(direccion);
        try (InputStream is = url.openStream();
                JsonReader reader = Json.createReader(is)) {
            return reader.read();
        }
    }

    public void getStats(JsonObject obj, String deviceType, String gameType) {
        try {

            // data
            JsonObject dataObject = obj.getJsonObject("data");
            // Player
            JsonObject accountObject = dataObject.getJsonObject("account");
            playerId = accountObject.getString("id");
            playerName = accountObject.getString("name");
            // BattlePass
            JsonObject battlePassObject = dataObject.getJsonObject("battlePass");
            battlePassLevel = battlePassObject.getInt("level");
            // stats
            JsonObject statsObject = dataObject.getJsonObject("stats");
            // all
            errorDevice = true;
            JsonObject allObject = statsObject != null ? statsObject.getJsonObject(deviceType) : null;
            // overall
            errorDevice = false;
            errorGame = true;
            JsonObject gameTypeObject = allObject != null ? allObject.getJsonObject(gameType) : null;
            if (!error) {
                score = gameTypeObject.containsKey("score") ? gameTypeObject.getInt("score") : 0;
                wins = gameTypeObject.containsKey("wins") ? gameTypeObject.getInt("wins") : 0;
                top3 = gameTypeObject.containsKey("top3") ? gameTypeObject.getInt("top3") : 0;
                top5 = gameTypeObject.containsKey("top5") ? gameTypeObject.getInt("top5") : 0;
                kills = gameTypeObject.containsKey("kills") ? gameTypeObject.getInt("kills") : 0;
                killsPerMin = gameTypeObject.containsKey("killsPerMin")
                        ? gameTypeObject.getJsonNumber("killsPerMin").doubleValue()
                        : 0;
                killsPerMatch = gameTypeObject.containsKey("killsPerMatch")
                        ? gameTypeObject.getJsonNumber("killsPerMatch").doubleValue()
                        : 0;
                deaths = gameTypeObject.containsKey("deaths") ? gameTypeObject.getInt("deaths") : 0;
                kd = gameTypeObject.containsKey("kd") ? gameTypeObject.getJsonNumber("kd").doubleValue() : 0;
                matches = gameTypeObject.containsKey("matches") ? gameTypeObject.getInt("matches") : 0;
                winRate = gameTypeObject.containsKey("winRate") ? gameTypeObject.getJsonNumber("winRate").doubleValue()
                        : 0;
                minutesPlayed = gameTypeObject.containsKey("minutesPlayed") ? gameTypeObject.getInt("minutesPlayed")
                        : 0;
                playersOutlived = gameTypeObject.containsKey("playersOutlived")
                        ? gameTypeObject.getInt("playersOutlived")
                        : 0;
                lastModified = gameTypeObject.containsKey("lastModified") ? gameTypeObject.getString("lastModified")
                        : "";
                // showStats();
            }
        } catch (ClassCastException e) {
            if (errorDevice) {
                System.out.println(ANSI_RED + "\n[!] You have not played any games" + ANSI_RESET);
                System.exit(1);
            } else if (errorGame) {
                System.out.println(ANSI_RED + "\n[!] You have not played any games in this game mode" + ANSI_RESET);
                System.exit(1);
            }
        }
    }

    public String gameTypeSelect() {
        int resGame = 0;
        do {
            try {
                Scanner sc2 = new Scanner(System.in);
                System.out.println("========================================");
                System.out.println(ANSI_PURPLE + "[+] Tipo de Partida:" + ANSI_RESET + "\n");
                System.out.println(ANSI_BLUE + "1" + ANSI_RESET + " Todos [Todos los modos de Juego]");
                System.out.println(ANSI_BLUE + "2" + ANSI_RESET + " Solo ");
                System.out.println(ANSI_BLUE + "3" + ANSI_RESET + " Duo");
                System.out.println(ANSI_BLUE + "4" + ANSI_RESET + " Squad");
                System.out.println(ANSI_BLUE + "5" + ANSI_RESET + " Ltm");
                resGame = sc2.nextInt();
            } catch (InputMismatchException e) {
                excp = true;
                System.out.println(ANSI_RED + "[!] Intrroduce un valor válido" + ANSI_RESET);
            }
        } while (resGame > 5 || resGame < 0 || excp);

        switch (resGame) {
            case 1:
                return "overall";
            case 2:
                return "solo";
            case 3:
                return "duo";
            case 4:
                return "squad";
            case 5:
                return "ltm";
        }
        return "";
    }

    public void getGeneralStats(JsonObject obj) {
        int resDevice = 0;
        do {
            try {
                Scanner sc = new Scanner(System.in);
                System.out.println(ANSI_PURPLE + "\n[+] Escoge el tipo de dispositivo" + ANSI_RESET + "\n");
                System.out.println(ANSI_BLUE + "1" + ANSI_RESET + " Todos [Todos los dispositivos]");
                System.out.println(ANSI_BLUE + "2" + ANSI_RESET + " Teclado Y Raton [PC]");
                System.out.println(ANSI_BLUE + "3" + ANSI_RESET + " Mando [Consola]");
                System.out.println(ANSI_BLUE + "4" + ANSI_RESET + " Movil/Tablet [Dispositivo Portatil]");
                resDevice = sc.nextInt();
            } catch (InputMismatchException e) {
                excp = true;
                System.out.println(ANSI_RED + "[!] Intrroduce un valor válido" + ANSI_RESET);
            }
        } while (resDevice > 4 || resDevice < 0 || excp);

        switch (resDevice) {
            case 1:
                getStats(obj, "all", gameTypeSelect());
                break;
            case 2:
                getStats(obj, "keyboardMouse", gameTypeSelect());
                break;
            case 3:
                getStats(obj, "gamepad", gameTypeSelect());
                break;
            case 4:
                getStats(obj, "touch", gameTypeSelect());
                break;
            default:
                break;
        }
    }

    public void showStats() {
        if (!error) {
            System.out.println(ANSI_BLUE + "[+] Player ID: " + ANSI_RESET + playerId + "\n");
            System.out.println(ANSI_BLUE + "[+] Player Name: " + ANSI_RESET + playerName + "\n");
            System.out.println(ANSI_BLUE + "[+] BattlePass Level: " + ANSI_RESET + battlePassLevel + "\n");
            if (score > 0) {
                System.out.println(ANSI_BLUE + "[+] Score: " + ANSI_RESET + score + "\n");
            }
            if (wins > 0) {
                System.out.println(ANSI_BLUE + "[+] Wins: " + ANSI_RESET + wins + "\n");
            }
            if (top3 > 0) {
                System.out.println(ANSI_BLUE + "[+] Top 3: " + ANSI_RESET + top3 + "\n");
            }
            if (top5 > 0) {
                System.out.println(ANSI_BLUE + "[+] Top 5: " + ANSI_RESET + top5 + "\n");
            }
            System.out.println(ANSI_BLUE + "[+] Kills: " + ANSI_RESET + kills + "\n");
            System.out.println(ANSI_BLUE + "[+] Kills Per Min: " + ANSI_RESET + killsPerMin + "\n");
            System.out.println(ANSI_BLUE + "[+] Kills Per Match: " + ANSI_RESET + killsPerMatch + "\n");
            System.out.println(ANSI_BLUE + "[+] Deaths: " + ANSI_RESET + deaths + "\n");
            System.out.println(ANSI_BLUE + "[+] KD: " + ANSI_RESET + kd + "\n");
            System.out.println(ANSI_BLUE + "[+] Matches: " + ANSI_RESET + matches + "\n");
            System.out.println(ANSI_BLUE + "[+] WinRate: " + ANSI_RESET + winRate + "\n");
            System.out.println(ANSI_BLUE + "[+] Minutes Played: " + ANSI_RESET + minutesPlayed + "\n");
            System.out.println(ANSI_BLUE + "[+] Players Outlived: " + ANSI_RESET + playersOutlived + "\n");
            if (lastModified != null && !lastModified.isEmpty()) {
                LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.parse(lastModified), ZoneId.systemDefault());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyy HH:mm:ss");
                System.out.println(ANSI_BLUE + "[+] Last Modified: " + ANSI_RESET + dateTime.format(formatter) + "\n");
            }
        }
    }

    private void runApp() throws IOException {
        asciiArt();
        JsonObject obj = leerHttps(Config.getApiUrl(nombrePlayerUrl)).asJsonObject();
        getGeneralStats(obj);
    }
    public static void main(String[] args) {

        App a = new App();
        try {
            // File f = new File(System.getProperty("user.home") + "\\Desktop\\fortnite.json");
            a.runApp();
            // a.escribeJSON(obj, f);
        } catch (IOException e) {
            a.error = true;
            System.err.println(ANSI_RED
                    + "[!] Error when making the request to the Fortnite API, make sure you have correctly entered the player name and TOKEN"
                    + ANSI_RESET);
        }
        a.showStats();
    }
}
