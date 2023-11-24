import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
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
    String apiUrl = Config.API_URL;
    String authToken = Config.AUTHORIZATION_HEADER;
    String playerId = "";
    String playerName = "";
    int score;
    int wins;
    int top3;
    int top5;
    int top10;
    int kills;
    int killsPerMin;
    int killsPerMatch;
    int deaths;
    int kd;
    int matches;
    int minutesPlayed;
    int playersOutlived;
    int battlePassLevel;

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
        JsonObject allObject = statsObject.getJsonObject(deviceType);
        // overall
        JsonObject gameTypeObject = allObject.getJsonObject(gameType);
        score = gameTypeObject.getInt("score");
        wins = gameTypeObject.getInt("wins");
        top3 = gameTypeObject.getInt("top3");
        top5 = gameTypeObject.getInt("top5");
        top10 = gameTypeObject.getInt("top10");
        kills = gameTypeObject.getInt("kills");
        killsPerMin = gameTypeObject.getInt("killsPerMin");
        killsPerMatch = gameTypeObject.getInt("killsPerMatch");
        deaths = gameTypeObject.getInt("deaths");
        kd = gameTypeObject.getInt("kd");
        matches = gameTypeObject.getInt("matches");
        minutesPlayed = gameTypeObject.getInt("minutesPlayed");
        playersOutlived = gameTypeObject.getInt("playersOutlived");
        showStats();
    }

    public String gameTypeSelect() {
        int resGame = 0;
        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("=================");
            System.out.println("Tipo de partida:");
            System.out.println("1- Todos");
            System.out.println("2- Solo");
            System.out.println("3- Duo");
            System.out.println("4- Squad");
            resGame = sc.nextInt();
        }
        switch (resGame) {
            case 1:
                return "overall";
            case 2:

                return "solo";
            case 3:

                return "duo";
            case 4:

                return "squad";
        }
        return "";
    }

    public void getGeneralStats(JsonObject obj) {
        try (Scanner sc = new Scanner(System.in)) {
            int resDevice = 0;
            System.out.println("=================");
            System.out.println("Tipo de Dispositivo:");
            System.out.println("1- Todos");
            System.out.println("2- Teclado Y Raton");
            System.out.println("3- Mando");
            System.out.println("4- Movil-Tablet");
            resDevice = sc.nextInt();
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
    }

    public void showStats() {
        System.out.println("Player ID: " + playerId);
        System.out.println("Player Name: " + playerName);
        System.out.println("BattlePass Level: " + battlePassLevel);
        System.out.println("Score: " + score);
        System.out.println("Wins: " + wins);
        System.out.println("Top 3: " + top3);
        System.out.println("Top 5: " + top5);
        System.out.println("Top 10: " + top10);
        System.out.println("Kills: " + kills);
        System.out.println("Kills Per Min: " + killsPerMin);
        System.out.println("Kills Per Match: " + killsPerMatch);
        System.out.println("Deaths: " + deaths);
        System.out.println("KD: " + kd);
        System.out.println("Matches: " + matches);
        System.out.println("Minutes Played: " + minutesPlayed);
        System.out.println("Players Outlived: " + playersOutlived);
    }

    public static void main(String[] args) {

        App a = new App();
        try {
            JsonObject obj = a.leerHttps(a.apiUrl).asJsonObject();
            File f = new File(System.getProperty("user.home") + "\\Desktop\\fortnite.json");
            a.getGeneralStats(obj);
            a.escribeJSON(obj, f);
        } catch (IOException e) {
            System.err.println("Error al realizar la solicitud a la API de Fortnite: " +
            e.getMessage());
            e.printStackTrace();
        }
        // JsonObject json = a.leeJSON(f.getAbsolutePath()).asJsonObject();
        // a.getGeneralStats(json);
        // a.showStats();
    }
}
//TODO comprobar errores en solo y duo, no tiene algunos top
