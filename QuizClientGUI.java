import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

public class QuizClientGUI {
    private JFrame frame;
    private JTextArea questionArea;
    private JTextField answerField;
    private JButton submitButton;
    private JLabel feedbackLabel;
    private JLabel scoreLabel;

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    private int score = 0;

    public QuizClientGUI(String serverIP, int port) {
        // GUI 초기화
        frame = new JFrame("Quiz Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setLayout(new BorderLayout());

        questionArea = new JTextArea("success");
        questionArea.setEditable(false);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        frame.add(new JScrollPane(questionArea), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        answerField = new JTextField();
        submitButton = new JButton("Submit");
        feedbackLabel = new JLabel(" ");
        bottomPanel.add(answerField, BorderLayout.CENTER);
        bottomPanel.add(submitButton, BorderLayout.EAST);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        scoreLabel = new JLabel("Score: ");
        frame.add(scoreLabel, BorderLayout.NORTH);

        frame.setVisible(true);

        // 서버 연결 및 통신
        try {
            socket = new Socket(serverIP, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // 서버와 통신을 별도 스레드로 처리
            new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        if (response.startsWith("QUESTION")) {
                            update_Question(response.substring(9));
                        } else if (response.startsWith("FEEDBACK")) {
                            update_Feedback(response.substring(9));
                        } else if (response.startsWith("FINAL_SCORE")) {
                            update_FinalScore(response.substring(12));
                            break;
                        }
                    }
                } catch (IOException e) {
                    Error("failed");
                }
            }).start();

        } catch (IOException e) {
            Error("Unable to connect");
        }

        // 버튼 클릭 이벤트 처리
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                send_Answer();}
        });
    }

    // 질문 업데이트
    private void update_Question(String question) {
        SwingUtilities.invokeLater(() -> {  //javax.swing.SwingUtilities사용
            questionArea.setText(question);
            feedbackLabel.setText(" ");
            answerField.setText("");
        });
    }

    // 피드백 업데이트
    private void update_Feedback(String feedback) {
        SwingUtilities.invokeLater(() -> feedbackLabel.setText(feedback));
        if (feedback.equalsIgnoreCase("Correct!")) {
            score++;
            SwingUtilities.invokeLater(() -> scoreLabel.setText("Score: " + score));
        }
    }

    // 최종 점수 업데이트
    private void update_FinalScore(String finalScore) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(frame, "Your Final Score: " + finalScore, "Game Over", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0); // 게임 종료
        });
    }

    // 답변 전송
    private void send_Answer() {
        try {
            String answer = answerField.getText().trim();
            if (!answer.isEmpty()) {
                out.write("ANSWER " + answer + "\n");
                out.flush();
            }
        } catch (IOException e) {
            Error("Failed");
        }
    }

    // 에러 메시지 표시
    private void Error(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new QuizClientGUI("localhost", 4321));
    }
}
