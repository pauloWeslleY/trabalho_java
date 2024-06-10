import java.io.*;
import java.net.*;

public class PedraPapelTesouraClient {
  private static final int SERVER_PORT = 12345;

  public static void main(String[] args) throws IOException {
    BufferedReader userIP = new BufferedReader(new InputStreamReader(System.in));
    System.out.println("Digite o IP:");
    String IP = userIP.readLine();

    try (Socket socket = new Socket(IP, SERVER_PORT);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

      String serverMessage;
      while ((serverMessage = in.readLine()) != null) {
        System.out.println("Servidor: " + serverMessage);
        if (serverMessage.contains("Sair para terminar")) {
          String userChoice = userInput.readLine();
          out.println(userChoice);
          if ("Sair".equalsIgnoreCase(userChoice)) {
            break;
          }
        } else if (serverMessage.contains("CPU")) {
          String userChoice = userInput.readLine();
          out.println(userChoice);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
