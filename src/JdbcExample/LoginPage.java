package JdbcExample;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginPage extends JDialog {
    private JTextField tfId;
    private JPasswordField tfParola;
    private JButton okButton;
    private JButton cancelButton;
    private JPanel loginPanel;

    public User authenticatedUser;

    public LoginPage(JFrame parent) {
        super(parent);
        setTitle("Login");
        setContentPane(loginPanel);
        setMinimumSize(new Dimension(450, 474));
        setModal(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        tfId.setText("");
        tfParola.setText("");
        authenticatedUser = null;

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String id = tfId.getText();
                String parola = new String(tfParola.getPassword());

                authenticatedUser = authenticateUser(id, parola);

                if (authenticatedUser != null) {
                    JOptionPane.showMessageDialog(LoginPage.this, "Logare cu succes! Bine ai venit, " + authenticatedUser.getNume() + " " + authenticatedUser.getPrenume() + "!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    openDashboard();
                } else {
                    JOptionPane.showMessageDialog(LoginPage.this, "Parola sau Id-ul utilizator nu este corect.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        setVisible(true);
    }

    private void openDashboard() {
        JFrame frame = new JFrame("Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 474);
        frame.setLocationRelativeTo(null);
        if(authenticatedUser != null){
            if(authenticatedUser.getId_rol().equals("4")) {
            frame.setTitle("Student Panel");
            frame.add(new StudentPanel(authenticatedUser).getPanel());
        }
            else if(authenticatedUser.getId_rol().equals("3")){
                frame.setTitle("Profesor Panel");
                frame.add(new ProfesorPanel(authenticatedUser).getPanel());
            }
            else if(authenticatedUser.getId_rol().equals("1") || authenticatedUser.getId_rol().equals("2")){
                frame.setTitle("Administrator Panel");
                frame.add(new AdminPanel(authenticatedUser).getPanel());
            }

        frame.setVisible(true);

        }
    }
    private User authenticateUser(String id, String password) {
        User user = null;
        String sql = "SELECT id_user, nume, prenume, id_rol FROM utilizator WHERE id_user=? AND parola=?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);
            statement.setString(2, password);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                user = new User(
                        resultSet.getString("id_user"),
                        resultSet.getString("nume"),
                        resultSet.getString("prenume"),
                        resultSet.getString("id_rol")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

}
