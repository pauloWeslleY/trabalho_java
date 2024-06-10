import java.io.*;
import java.net.*;
import java.util.Random;

public class PedraPapelTesouraServer {
  private static final int PORT = 12345;
  private static final String[] OPTIONS = { "Pedra", "Papel", "Tesoura" };
  private static final String SAIR = "Sair";

  public static void main(String[] args) {
    System.out.println("Servidor iniciado...");

    try (ServerSocket serverSocket = new ServerSocket(PORT)) {
      while (true) {
        Socket player1Socket = serverSocket.accept();
        BufferedReader player1In = new BufferedReader(new InputStreamReader(player1Socket.getInputStream()));
        PrintWriter player1Out = new PrintWriter(player1Socket.getOutputStream(), true);

        player1Out.println("Esperando um oponente... Digite 'CPU' para jogar contra a CPU.");

        String opponentType = player1In.readLine().trim();
        if (opponentType.equalsIgnoreCase("CPU")) {
          new Thread(new GameHandler(player1Socket, null)).start();
        } else {
          Socket player2Socket = serverSocket.accept();
          new Thread(new GameHandler(player1Socket, player2Socket)).start();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static class GameHandler implements Runnable {
    private final Socket player1Socket;
    private final Socket player2Socket;
    private final BufferedReader player1In;
    private final PrintWriter player1Out;
    private final BufferedReader player2In;
    private final PrintWriter player2Out;
    private final Random rand = new Random();

    public GameHandler(Socket player1Socket, Socket player2Socket) throws IOException {
      this.player1Socket = player1Socket;
      this.player2Socket = player2Socket;
      this.player1In = new BufferedReader(new InputStreamReader(player1Socket.getInputStream()));
      this.player1Out = new PrintWriter(player1Socket.getOutputStream(), true);

      if (player2Socket != null) {
        this.player2In = new BufferedReader(new InputStreamReader(player2Socket.getInputStream()));
        this.player2Out = new PrintWriter(player2Socket.getOutputStream(), true);
      } else {
        this.player2In = null;
        this.player2Out = null;
      }
    }

    @Override
    public void run() {
      try {
        int player1Wins = 0, player1Losses = 0, player1Draws = 0;
        int player2Wins = 0, player2Losses = 0, player2Draws = 0;

        while (true) {
          player1Out.println("Escolha: Pedra, Papel ou Tesoura (ou Sair para terminar):");
          String player1Choice = player1In.readLine().trim();

          if (SAIR.equalsIgnoreCase(player1Choice)) {
            printResults(player1Wins, player1Losses, player1Draws, player2Wins, player2Losses, player2Draws);
            break;
          }

          if (!isValidChoice(player1Choice)) {
            player1Out.println("Escolha invalida. Tente novamente.");
            continue;
          }

          String player2Choice;
          if (player2Socket == null) { // Jogar contra a CPU
            player2Choice = OPTIONS[rand.nextInt(OPTIONS.length)];
            player1Out.println("CPU escolheu: " + player2Choice);
          } else { // Jogar contra outro jogador
            player2Out.println("Escolha: Pedra, Papel ou Tesoura (ou Sair para terminar):");
            player2Choice = player2In.readLine().trim();

            if (SAIR.equalsIgnoreCase(player2Choice)) {
              player1Out.println("O oponente saiu do jogo.");
              player2Out.println("Voce saiu do jogo.");
              break;
            }

            if (!isValidChoice(player2Choice)) {
              player1Out.println("Escolha invalida do oponente. Tente novamente.");
              player2Out.println("Escolha invalida. Tente novamente.");
              continue;
            }

            player1Out.println("Oponente escolheu: " + player2Choice);
            player2Out.println("Voce escolheu: " + player2Choice);
          }

          String result = determineWinner(player1Choice, player2Choice);

          switch (result) {
            case "Vitória":
              player1Wins++;
              player2Losses++;
              player1Out.println("Voce ganhou!");
              if (player2Out != null)
                player2Out.println("Voce perdeu!");
              break;
            case "Derrota":
              player1Losses++;
              player2Wins++;
              player1Out.println("Voce perdeu!");
              if (player2Out != null)
                player2Out.println("Voce ganhou!");
              break;
            case "Empate":
              player1Draws++;
              player2Draws++;
              player1Out.println("Empate!");
              if (player2Out != null)
                player2Out.println("Empate!");
              break;
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        try {
          player1Socket.close();
          if (player2Socket != null)
            player2Socket.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    private boolean isValidChoice(String choice) {
      for (String option : OPTIONS) {
        if (option.equalsIgnoreCase(choice)) {
          return true;
        }
      }
      return false;
    }

    private String determineWinner(String player1Choice, String player2Choice) {
      if (player1Choice.equalsIgnoreCase(player2Choice)) {
        return "Empate";
      }

      switch (player1Choice.toLowerCase()) {
        case "pedra":
          return player2Choice.equalsIgnoreCase("Tesoura") ? "Vitória" : "Derrota";
        case "papel":
          return player2Choice.equalsIgnoreCase("Pedra") ? "Vitória" : "Derrota";
        case "tesoura":
          return player2Choice.equalsIgnoreCase("Papel") ? "Vitória" : "Derrota";
        default:
          return "Escolha invalida";
      }
    }

    private void printResults(int player1Wins, int player1Losses, int player1Draws, int player2Wins, int player2Losses,
        int player2Draws) {
      player1Out.println("Vitórias: " + player1Wins + ", Derrotas: " + player1Losses + ", Empates: " + player1Draws);
      if (player2Out != null) {
        player2Out.println("Vitórias: " + player2Wins + ", Derrotas: " + player2Losses + ", Empates: " + player2Draws);
      }
    }
  }
}
