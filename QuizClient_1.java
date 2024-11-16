import java.io.*;
import java.net.*;

public class QuizClient {
    public static void main(String[] args) {
        String server_ip = "localhost";
        int port = 4321;

        BufferedReader in = null;
        BufferedReader stin = null;
        BufferedWriter out = null;
        Socket socket = null;

        try {
            try (BufferedReader Reader = new BufferedReader(new FileReader("server.dat"))) { 
                server_ip = Reader.readLine();
                port = Integer.parseInt(Reader.readLine());
            } catch (IOException e) {
                System.out.println("failed");
            }

            // Establish connection to server
            socket = new Socket(server_ip, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            stin = new BufferedReader(new InputStreamReader(System.in));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            String response;
            while ((response = in.readLine()) != null) {
                if (response.startsWith("QUESTION")) {
                    System.out.println(response.substring(9));  // question
                    System.out.print("Your Answer: ");
                    String answer = stin.readLine();
                    out.write("ANSWER " + answer + "\n");  // Send answer to server
                    out.flush();
                } else if (response.startsWith("FEEDBACK")) {
                    System.out.println(response.substring(9));  // feedback (Correct/Incorrect)
                } else if (response.startsWith("FINAL_SCORE")) {
                    System.out.println("Your Final Score: " + response.substring(12));  // final score
                    break;  // Exit loop 
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close
            try {
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.out.println("Disconnected.");
            }
        }
    }
}





