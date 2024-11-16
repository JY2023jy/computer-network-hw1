import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*; //multi thread

public class QuizServer {
    private static final int PORT = 4321;
    private static final List<String[]> QUESTIONS = Arrays.asList(      //java.util함수 사용
        new String[]{"1 + 1 = ?", "2"},
        new String[]{"4 / 2 = ?", "2"},
        new String[]{"1 x 2 = ?", "2"}
    );
    private static final int THREAD_POOL_SIZE = 5; // Maximum 5 clients

    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE); // thread pool 생성
        try (ServerSocket listener = new ServerSocket(PORT)) {  
            System.out.println("Quiz Server connect on  " + PORT);
            while (true) {
                Socket clientSocket = listener.accept();
                System.out.println("new connection's established");
                pool.execute(new ClientHandler(clientSocket)); // 클라이언트 처리 실행
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            pool.shutdown(); // ThreadPool 종료
        }
    }

    private static class ClientHandler implements Runnable { //Thread
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
            ) {
                int score = 0;
                for (String[] qa : QUESTIONS) {
                    out.write("QUESTION " + qa[0] + "\n");
                    out.flush();

                    String clientAnswer = in.readLine().replace("ANSWER ", "");
                    if (clientAnswer.equalsIgnoreCase(qa[1])) {
                        out.write("FEEDBACK Correct\n");
                        out.flush();
                        score++;
                    } else {
                        out.write("FEEDBACK Incorrect\n");
                        out.flush();
                    }
                }
                out.write("FINAL_SCORE " + score + "\n");
                out.flush();
            } catch (IOException e) {
                System.out.println("Error ");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Failed");
                }
            }
        }
    }
}
