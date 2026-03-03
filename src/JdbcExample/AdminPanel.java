package JdbcExample;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Locale;

public class AdminPanel {
    private JPanel panelTop;
    private JLabel lblUserImage;
    private JButton Deconectare;
    private JButton btnAdminName;
    private JButton btnUser;
    private JPanel panelOutput;
    private CardLayout cardLayout;
    private JPanel pan;
    private JToolBar menuBar;
    private JButton btnCursuri;

    public AdminPanel(User user) {

        btnAdminName.setText(user.getNume() + " " + user.getPrenume());

        btnAdminName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sql = "SELECT u.id_user, u.CNP, u.nume, u.prenume, u.id_rol, a.judet, a.localitate, a.strada, a.numar FROM utilizator u JOIN adresa a on a.id_adresa = u.id_adrasa WHERE u.id_user = ?";
                String stId = user.getId();

                try (Connection connection = DatabaseConnection.getConnection()) {
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1, stId);

                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            String id = resultSet.getString("id_user");
                            String CNP = resultSet.getString("CNP");
                            String nume = resultSet.getString("nume");
                            String prenume = resultSet.getString("prenume");
                            String judet = resultSet.getString("judet");
                            String localitate = resultSet.getString("localitate");
                            String strada = resultSet.getString("strada");
                            String numar = resultSet.getString("numar");

                            String mesaj = String.format("ID user: %s\nCNP: %s\nNume: %s\nPrenume: %s\nAdresa: judetul %s, localitatea %s, %s, numarul %s\n", id, CNP, nume, prenume, judet, localitate, strada, numar);

                            JOptionPane.showMessageDialog(pan, mesaj, "Informatii personale", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(
                                    pan,
                                    "Nu s-au gasit informatii personale.",
                                    "Eroare",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        cardLayout = new CardLayout();
        panelOutput.setLayout(cardLayout);

        JPanel userPanel = new JPanel();
        JPanel cursuriPanel = new JPanel();

        panelOutput.add(userPanel, "Utilizatori");
        panelOutput.add(cursuriPanel, "Cursuri");


        Deconectare.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }

            private void logout() {
                int confirm = JOptionPane.showConfirmDialog(
                        pan,
                        "Esti sigur ca vrei sa te deconectezi?",
                        "Confirmare Deconectare",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE

                );

                if (confirm == JOptionPane.YES_OPTION) {
                    JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(pan);
                    if (parentFrame != null) {
                        parentFrame.dispose();
                    }
                    new LoginPage(null);
                }
            }
        });

        btnUser.addActionListener(new ActionListener() {
            String currentUserRole = user.getId_rol();
            int currentRole = Integer.parseInt(currentUserRole);
            @Override
            public void actionPerformed(ActionEvent e) {
                JButton searchUsersButton = new JButton("Cauta utilizatori");
                searchUsersButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String searchTerm = JOptionPane.showInputDialog(pan, "Introduceti numele utilizatorului");
                        if (searchTerm != null && !searchTerm.isEmpty()) {
                            searchUsers(searchTerm);
                        }
                    }

                    private void searchUsers(String searchTerm) {
                        String sql = "SELECT * FROM utilizator WHERE nume LIKE ? OR prenume LIKE ?";
                        try (Connection connection = DatabaseConnection.getConnection()) {
                            PreparedStatement preparedStatement = connection.prepareStatement(sql);
                            preparedStatement.setString(1, "%" + searchTerm + "%");
                            preparedStatement.setString(2, "%" + searchTerm + "%");

                            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                                StringBuilder usersInfo = new StringBuilder("Utilizatori gasiti:\n");
                                while (resultSet.next()) {
                                    usersInfo.append("ID: ").append(resultSet.getString("id_user"))
                                            .append(" Nume: ").append(resultSet.getString("nume"))
                                            .append(" Prenume: ").append(resultSet.getString("prenume"))
                                            .append("\n");
                                }
                                JOptionPane.showMessageDialog(pan, usersInfo.toString(), "Rezultate cautare utilizatori", JOptionPane.INFORMATION_MESSAGE);
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(pan, "Eroare la cautarea utilizatorilor!", "Eroare", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });

                JButton insertUserButton = new JButton("Adauga utilizator");
                insertUserButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        String idUser = JOptionPane.showInputDialog(pan, "Introduceti id-ul utilizatorului");
                        String nume = JOptionPane.showInputDialog(pan, "Introduceti numele utilizatorului");
                        String prenume = JOptionPane.showInputDialog(pan, "Introduceti prenumele utilizatorului");
                        String CNP = JOptionPane.showInputDialog(pan, "Introduceti CNP-ul utilizatorului");
                        String telefon = JOptionPane.showInputDialog(pan, "Introduceti numarul de telefon");
                        String iban = JOptionPane.showInputDialog(pan, "Introduceti IBAN-ul utilizatorului");
                        String nrContract = JOptionPane.showInputDialog(pan, "Introduceti numarul contractului");
                        String idAdresa = JOptionPane.showInputDialog(pan, "Introduceti ID-ul adresei");
                        String idRol = JOptionPane.showInputDialog(pan, "Introduceti ID-ul rolului");

                        if (idUser != null && nume != null && prenume != null && CNP != null && telefon != null && iban != null && nrContract != null && idAdresa != null && idRol != null) {
                            int role = Integer.parseInt(idRol);

                            // Verificare permisiuni: Super Admin poate adăuga orice utilizator
                            if (currentRole == 1) { // Super Admin
                                // Permite orice tip de utilizator
                                insertUser(idUser, nume, prenume, CNP, telefon, iban, nrContract, Integer.parseInt(idAdresa), role);
                            } else if (currentRole == 2) { // Admin
                                // Admin poate adauga doar Profesori (3) sau Studenti (4)
                                if (role != 3 && role != 4) {
                                    JOptionPane.showMessageDialog(pan, "Un Admin poate adauga doar Profesori sau Studenti.", "Eroare", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                                insertUser(idUser, nume, prenume, CNP, telefon, iban, nrContract, Integer.parseInt(idAdresa), role);
                            } else {
                                JOptionPane.showMessageDialog(pan, "Nu aveti permisiunea sa adaugati utilizatori.", "Eroare", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            JOptionPane.showMessageDialog(pan, "Toate campurile sunt obligatorii!", "Eroare", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    private void insertUser(String idUser, String nume, String prenume, String CNP, String telefon, String iban, String nrContract, int idAdresa, int idRol) {
                        String sql = "INSERT INTO utilizator (id_user, nume, prenume, CNP, telefon, iban, nr_contract, id_adrasa, id_rol, parola) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                        try (Connection connection = DatabaseConnection.getConnection()) {
                            PreparedStatement preparedStatement = connection.prepareStatement(sql);
                            preparedStatement.setString(1, idUser);
                            preparedStatement.setString(2, nume);
                            preparedStatement.setString(3, prenume);
                            preparedStatement.setString(4, CNP);
                            preparedStatement.setString(5, telefon);
                            preparedStatement.setString(6, iban);
                            preparedStatement.setString(7, nrContract);
                            preparedStatement.setInt(8, idAdresa);
                            preparedStatement.setInt(9, idRol);
                            preparedStatement.setString(10, "12345");  // Optionally add logic for password generation

                            int rowsAffected = preparedStatement.executeUpdate();
                            if (rowsAffected > 0) {
                                JOptionPane.showMessageDialog(pan, "Utilizatorul a fost adaugat cu succes!", "Succes", JOptionPane.INFORMATION_MESSAGE);
                            }

                            if (idRol == 3) { // Profesor
                                insertProfesor(idUser);
                            } else if (idRol == 4) { // Student
                                insertStudent(idUser);
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(pan, "Eroare la adaugarea utilizatorului!", "Eroare", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    private void insertProfesor(String idUser) {

                        String sql = "INSERT INTO profesor (id_profesor) VALUES (?)";
                        try (Connection connection = DatabaseConnection.getConnection()) {
                            PreparedStatement preparedStatement = connection.prepareStatement(sql);
                            preparedStatement.setString(1, idUser);

                            int rowsAffected = preparedStatement.executeUpdate();
                            if (rowsAffected > 0) {
                                JOptionPane.showMessageDialog(pan, "Profesorul a fost adaugat cu succes!", "Succes", JOptionPane.INFORMATION_MESSAGE);
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(pan, "Eroare la adaugarea profesorului!", "Eroare", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    private void insertStudent(String idUser) {
                        String sql = "INSERT INTO student (id_student) VALUES (?)";

                        try (Connection connection = DatabaseConnection.getConnection()) {
                            PreparedStatement preparedStatement = connection.prepareStatement(sql);
                            preparedStatement.setString(1, idUser);

                            int rowsAffected = preparedStatement.executeUpdate();
                            if (rowsAffected > 0) {
                                JOptionPane.showMessageDialog(pan, "Studentul a fost adaugat cu succes!", "Succes", JOptionPane.INFORMATION_MESSAGE);
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(pan, "Eroare la adaugarea studentului!", "Eroare", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });

                JButton btnAdresa = new JButton("Introduce o adresa noua");
                btnAdresa.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String idAdresa = JOptionPane.showInputDialog(pan, "Introduceti id-ul adresei(obligatoriu)");
                        String judet = JOptionPane.showInputDialog(pan, "Introduceti judetul");
                        String localitate = JOptionPane.showInputDialog(pan, "Introduceti localitatea");
                        String strada = JOptionPane.showInputDialog(pan, "Introduceti strada");
                        String numar = JOptionPane.showInputDialog(pan, "Introduceti numarul");

                        if (idAdresa != null) {
                            insertAdresa(idAdresa, judet, localitate, strada, numar);
                        }
                    }

                    private void insertAdresa(String idAdresa, String judet, String localitate, String strada, String numar) {
                        String sql = "INSERT INTO adresa (id_adresa, judet, localitate, strada, numar) VALUES (?, ?, ?, ?, ?)";

                        try (Connection connection = DatabaseConnection.getConnection()) {
                            PreparedStatement preparedStatement = connection.prepareStatement(sql);

                            preparedStatement.setString(1, idAdresa);
                            preparedStatement.setString(2, judet.isEmpty() ? null : judet);
                            preparedStatement.setString(3, localitate.isEmpty() ? null : localitate);
                            preparedStatement.setString(4, strada.isEmpty() ? null : strada);
                            preparedStatement.setString(5, numar.isEmpty() ? null : numar);

                            int rowsAffected = preparedStatement.executeUpdate();
                            if (rowsAffected > 0) {
                                JOptionPane.showMessageDialog(pan, "Adresa a fost adaugata cu succes!", "Succes", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(pan, "Adresa nu a putut fi adaugata!", "Eroare", JOptionPane.WARNING_MESSAGE);
                            }

                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(pan, "Eroare la adaugarea adresei!", "Eroare", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                });

                JButton modifyUserButton = new JButton("Modifica utilizator");
                modifyUserButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String userId = JOptionPane.showInputDialog(pan, "Introduceti ID-ul utilizatorului de modificat");
                        if (userId != null && !userId.isEmpty()) {
                            modifyUser(userId);
                        }
                    }

                    private void modifyUser(String userId) {
                        String sql = "SELECT * FROM utilizator WHERE id_user = ?";

                        try (Connection connection = DatabaseConnection.getConnection()) {
                            PreparedStatement preparedStatement = connection.prepareStatement(sql);
                            preparedStatement.setString(1, userId);
                            ResultSet resultSet = preparedStatement.executeQuery();

                            if (resultSet.next()) {
                                int targetUserRole = resultSet.getInt("id_rol");

                                // Verificare permisiuni: Super Admin poate modifica orice utilizator
                                if (currentRole == 1) { // Super Admin
                                    // Permite modificarea oricarui utilizator
                                    String idAdresa = resultSet.getString("id_adrasa");
                                    String telefon = resultSet.getString("telefon");
                                    String iban = resultSet.getString("iban");

                                    // Permite utilizatorului sa selecteze campul pe care dorește sa-l modifice
                                    String[] options = {"ID Adresa", "Telefon", "IBAN"};
                                    int choice = JOptionPane.showOptionDialog(pan, "Alegeți câmpul pe care doriți să-l modificați:",
                                            "Selectați câmpul", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

                                    if (choice == JOptionPane.CLOSED_OPTION) {
                                        return; // Daca utilizatorul inchide dialogul fara selecție
                                    }

                                    // Permite utilizatorului sa modifice campul selectat
                                    String newValue = null;
                                    switch (choice) {
                                        case 0: // ID Adresa
                                            newValue = JOptionPane.showInputDialog(pan, "Introduceti noul ID Adresa:", idAdresa);
                                            break;
                                        case 1: // Telefon
                                            newValue = JOptionPane.showInputDialog(pan, "Introduceti noul telefon:", telefon);
                                            break;
                                        case 2: // IBAN
                                            newValue = JOptionPane.showInputDialog(pan, "Introduceti noul IBAN:", iban);
                                            break;
                                    }

                                    // Daca utilizatorul a introdus o valoare valida
                                    if (newValue != null && !newValue.isEmpty()) {
                                        String updateSql = null;
                                        switch (choice) {
                                            case 0:
                                                updateSql = "UPDATE utilizator SET id_adrasa = ? WHERE id_user = ?";
                                                break;
                                            case 1:
                                                updateSql = "UPDATE utilizator SET telefon = ? WHERE id_user = ?";
                                                break;
                                            case 2:
                                                updateSql = "UPDATE utilizator SET iban = ? WHERE id_user = ?";
                                                break;
                                        }

                                        if (updateSql != null) {
                                            PreparedStatement updateStmt = connection.prepareStatement(updateSql);
                                            updateStmt.setString(1, newValue);
                                            updateStmt.setString(2, userId);

                                            int rowsAffected = updateStmt.executeUpdate();
                                            if (rowsAffected > 0) {
                                                JOptionPane.showMessageDialog(pan, "Utilizatorul a fost modificat cu succes!", "Succes", JOptionPane.INFORMATION_MESSAGE);
                                            } else {
                                                JOptionPane.showMessageDialog(pan, "Nu s-au făcut modificări.", "Eroare", JOptionPane.ERROR_MESSAGE);
                                            }
                                        }
                                    } else {
                                        JOptionPane.showMessageDialog(pan, "Nu ați introdus o valoare validă!", "Eroare", JOptionPane.ERROR_MESSAGE);
                                    }
                                } else if (currentRole == 2) { // Admin
                                    // Admin poate modifica doar Profesori (3) sau Studenti (4)
                                    if (targetUserRole == 1) { // Nu poate modifica un Super Admin
                                        JOptionPane.showMessageDialog(pan, "Nu puteti modifica un Super Admin.", "Eroare", JOptionPane.ERROR_MESSAGE);
                                        return;
                                    }

                                    // Admin poate modifica doar Profesori (3) sau Studenti (4)
                                    if (targetUserRole != 3 && targetUserRole != 4) {
                                        JOptionPane.showMessageDialog(pan, "Un Admin poate modifica doar Profesori sau Studenti.", "Eroare", JOptionPane.ERROR_MESSAGE);
                                        return;
                                    }

                                    // Permite modificarea câmpurilor pentru profesori sau studenți
                                    String idAdresa = resultSet.getString("id_adrasa");
                                    String telefon = resultSet.getString("telefon");
                                    String iban = resultSet.getString("iban");

                                    // Permite utilizatorului să selecteze câmpul pe care dorește să-l modifice
                                    String[] options = {"ID Adresa", "Telefon", "IBAN"};
                                    int choice = JOptionPane.showOptionDialog(pan, "Alegeți câmpul pe care doriți să-l modificați:",
                                            "Selectați câmpul", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

                                    if (choice == JOptionPane.CLOSED_OPTION) {
                                        return; // Dacă utilizatorul închide dialogul fără selecție
                                    }

                                    // Permite utilizatorului să modifice câmpul selectat
                                    String newValue = null;
                                    switch (choice) {
                                        case 0: // ID Adresa
                                            newValue = JOptionPane.showInputDialog(pan, "Introduceti noul ID Adresa:", idAdresa);
                                            break;
                                        case 1: // Telefon
                                            newValue = JOptionPane.showInputDialog(pan, "Introduceti noul telefon:", telefon);
                                            break;
                                        case 2: // IBAN
                                            newValue = JOptionPane.showInputDialog(pan, "Introduceti noul IBAN:", iban);
                                            break;
                                    }

                                    // Dacă utilizatorul a introdus o valoare validă
                                    if (newValue != null && !newValue.isEmpty()) {
                                        String updateSql = null;
                                        switch (choice) {
                                            case 0:
                                                updateSql = "UPDATE utilizator SET id_adrasa = ? WHERE id_user = ?";
                                                break;
                                            case 1:
                                                updateSql = "UPDATE utilizator SET telefon = ? WHERE id_user = ?";
                                                break;
                                            case 2:
                                                updateSql = "UPDATE utilizator SET iban = ? WHERE id_user = ?";
                                                break;
                                        }

                                        if (updateSql != null) {
                                            PreparedStatement updateStmt = connection.prepareStatement(updateSql);
                                            updateStmt.setString(1, newValue);
                                            updateStmt.setString(2, userId);

                                            int rowsAffected = updateStmt.executeUpdate();
                                            if (rowsAffected > 0) {
                                                JOptionPane.showMessageDialog(pan, "Utilizatorul a fost modificat cu succes!", "Succes", JOptionPane.INFORMATION_MESSAGE);
                                            } else {
                                                JOptionPane.showMessageDialog(pan, "Nu s-au făcut modificări.", "Eroare", JOptionPane.ERROR_MESSAGE);
                                            }
                                        }
                                    } else {
                                        JOptionPane.showMessageDialog(pan, "Nu ați introdus o valoare validă!", "Eroare", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            } else {
                                JOptionPane.showMessageDialog(pan, "Utilizatorul nu a fost găsit!", "Eroare", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(pan, "Eroare la modificarea utilizatorului!", "Eroare", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });

                JButton deleteUserButton = new JButton("Sterge utilizator");
                deleteUserButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String userId = JOptionPane.showInputDialog(pan, "Introduceti ID-ul utilizatorului de sters");
                        if (userId != null && !userId.isEmpty()) {
                            deleteUser(userId);
                        }
                    }

                    private void deleteUser(String userId) {
                        int confirm = JOptionPane.showConfirmDialog(pan, "Esti sigur ca vrei sa stergi acest utilizator?",
                                "Confirmare stergere", JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            String sql = "SELECT id_rol FROM utilizator WHERE id_user = ?";

                            try (Connection connection = DatabaseConnection.getConnection()) {
                                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                                preparedStatement.setString(1, userId);
                                ResultSet resultSet = preparedStatement.executeQuery();

                                if (resultSet.next()) {
                                    int targetUserRole = resultSet.getInt("id_rol");

                                    if (currentRole == 1) {
                                        if (targetUserRole == 1) {
                                            JOptionPane.showMessageDialog(pan, "Nu puteti sterge un Super Admin.", "Eroare", JOptionPane.ERROR_MESSAGE);
                                            return;
                                        }


                                        String deleteSql = "DELETE FROM utilizator WHERE id_user = ?";
                                        PreparedStatement deleteStmt = connection.prepareStatement(deleteSql);
                                        deleteStmt.setString(1, userId);

                                        int rowsAffected = deleteStmt.executeUpdate();
                                        if (rowsAffected > 0) {
                                            JOptionPane.showMessageDialog(pan, "Utilizatorul a fost sters cu succes!", "Succes", JOptionPane.INFORMATION_MESSAGE);
                                        } else {
                                            JOptionPane.showMessageDialog(pan, "Nu s-a gasit utilizatorul de sters.", "Eroare", JOptionPane.ERROR_MESSAGE);
                                        }
                                    } else if (currentRole == 2) {
                                        if (targetUserRole == 1) {
                                            JOptionPane.showMessageDialog(pan, "Nu puteti sterge un Super Admin.", "Eroare", JOptionPane.ERROR_MESSAGE);
                                            return;
                                        }

                                        if (targetUserRole != 3 && targetUserRole != 4) {
                                            JOptionPane.showMessageDialog(pan, "Un Admin poate sterge doar Profesori sau Studenti.", "Eroare", JOptionPane.ERROR_MESSAGE);
                                            return;
                                        }

                                        String deleteSql = "DELETE FROM utilizator WHERE id_user = ?";
                                        PreparedStatement deleteStmt = connection.prepareStatement(deleteSql);
                                        deleteStmt.setString(1, userId);

                                        int rowsAffected = deleteStmt.executeUpdate();
                                        if (rowsAffected > 0) {
                                            JOptionPane.showMessageDialog(pan, "Utilizatorul a fost sters cu succes!", "Succes", JOptionPane.INFORMATION_MESSAGE);
                                        } else {
                                            JOptionPane.showMessageDialog(pan, "Nu s-a gasit utilizatorul de sters.", "Eroare", JOptionPane.ERROR_MESSAGE);
                                        }
                                    }
                                } else {
                                    JOptionPane.showMessageDialog(pan, "Utilizatorul nu a fost găsit!", "Eroare", JOptionPane.ERROR_MESSAGE);
                                }
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(pan, "Eroare la stergerea utilizatorului!", "Eroare", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                });

                JButton showAllStudentsButton = new JButton("Afiseaza toti studentii");
                showAllStudentsButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        showAllUsers(4);
                    }

                });

                JButton showAllProfessorsButton = new JButton("Afiseaza toti profesorii");
                showAllProfessorsButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        showAllUsers(3);
                    }
                });
                userPanel.removeAll();

                JPanel butonPanel = new JPanel(new GridLayout(6, 1, 10, 10));
                butonPanel.add(searchUsersButton);
                butonPanel.add(btnAdresa);
                butonPanel.add(insertUserButton);
                butonPanel.add(modifyUserButton);
                butonPanel.add(deleteUserButton);
                butonPanel.add(showAllProfessorsButton);
                butonPanel.add(showAllStudentsButton);

                userPanel.add(butonPanel, BorderLayout.CENTER);

                cardLayout.show(panelOutput, "Utilizatori");

                userPanel.revalidate();
                userPanel.repaint();

            }
        });

        btnCursuri.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sql = "SELECT c.id_cursuri, c.nume AS curs_nume, p.nume AS profesor_nume, p.prenume AS profesor_prenume " +
                        "FROM cursuri c " +
                        "JOIN prof_cursuri pc ON c.id_cursuri = pc.id_curs " +
                        "JOIN utilizator p ON pc.id_profesor = p.id_user";

                DefaultListModel<String> cursuriModel = new DefaultListModel<>();

                try (Connection connection = DatabaseConnection.getConnection()) {
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);

                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        while (resultSet.next()) {
                            String courseName = "|" + resultSet.getString("id_cursuri") + "| " + resultSet.getString("curs_nume");
                            String professorName = resultSet.getString("profesor_nume") + " " + resultSet.getString("profesor_prenume");
                            cursuriModel.addElement(courseName + " | Profesor: " + professorName);
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }

                JList<String> cursuriList = new JList<>(cursuriModel);
                JScrollPane cursuriScroller = new JScrollPane(cursuriList);
                cursuriScroller.setPreferredSize(new Dimension(430, 200));

                JTextField searchField = new JTextField();
                searchField.setPreferredSize(new Dimension(430, 30));
                searchField.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String search = searchField.getText();
                        DefaultListModel<String> filteredListModel = new DefaultListModel<>();

                        for (int i = 0; i < cursuriModel.getSize(); i++) {
                            String courseName = cursuriModel.getElementAt(i);
                            if (courseName.contains(search) || courseName.toLowerCase().contains(search)) {
                                filteredListModel.addElement(courseName);
                            }
                        }
                        if (filteredListModel.size() == 0) {
                            filteredListModel.addElement("Nu s-au gasit cursuri cu acest nume!");
                        }

                        cursuriList.setModel(filteredListModel);
                    }
                });


                cursuriScroller.setPreferredSize(new Dimension(430, 200));


                JPanel searchPanel = new JPanel();
                searchPanel.setLayout(new BorderLayout());

                searchPanel.add(searchField, BorderLayout.NORTH);
                searchPanel.add(cursuriScroller, BorderLayout.CENTER);


                cursuriPanel.removeAll();
                cursuriPanel.add(searchPanel, BorderLayout.NORTH);

                JButton btnAssignProf = new JButton("Asignare profesor la curs");
                btnAssignProf.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String courseIdInput = JOptionPane.showInputDialog(pan, "Introduceti ID-ul cursului");
                        String professorIdInput = JOptionPane.showInputDialog(pan, "Introduceti ID-ul profesorului");

                        if (courseIdInput != null && professorIdInput != null) {
                            try {
                                int courseId = Integer.parseInt(courseIdInput); // Parse course ID
                                int professorId = Integer.parseInt(professorIdInput); // Parse professor ID
                                assignProfessorToCourse(courseId, professorId);
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(pan, "ID-urile trebuie sa fie numere valide!", "Eroare", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }

                    private void assignProfessorToCourse(int courseId, int professorId) {
                        String assignProfessorSql = "INSERT INTO prof_cursuri (id_profesor, id_curs) VALUES (?, ?)";

                        try (Connection connection = DatabaseConnection.getConnection()) {

                            String checkCourseSql = "SELECT id_cursuri FROM cursuri WHERE id_cursuri = ?";
                            PreparedStatement courseStmt = connection.prepareStatement(checkCourseSql);
                            courseStmt.setInt(1, courseId);
                            ResultSet courseResultSet = courseStmt.executeQuery();

                            if (courseResultSet.next()) {

                                String checkProfessorSql = "SELECT id_profesor FROM profesor WHERE id_profesor = ?";
                                PreparedStatement professorStmt = connection.prepareStatement(checkProfessorSql);
                                professorStmt.setInt(1, professorId);
                                ResultSet professorResultSet = professorStmt.executeQuery();

                                if (professorResultSet.next()) {

                                    PreparedStatement assignStmt = connection.prepareStatement(assignProfessorSql);
                                    assignStmt.setInt(1, professorId);
                                    assignStmt.setInt(2, courseId);
                                    assignStmt.executeUpdate();

                                    JOptionPane.showMessageDialog(pan, "Profesorul a fost asignat cursului!", "Succes", JOptionPane.INFORMATION_MESSAGE);
                                } else {
                                    JOptionPane.showMessageDialog(pan, "Profesorul cu ID-ul specificat nu a fost gasit!", "Eroare", JOptionPane.ERROR_MESSAGE);
                                }
                            } else {
                                JOptionPane.showMessageDialog(pan, "Cursul cu ID-ul specificat nu a fost gasit!", "Eroare", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(pan, "Eroare la atribuirea profesorului!", "Eroare", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });


                JButton btnViewStudents = new JButton("Vezi studenti la curs");
                btnViewStudents.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                String courseId = JOptionPane.showInputDialog(pan, "Introduceti ID-ul cursului pentru a vedea studentii");
                if (courseId != null && !courseId.trim().isEmpty()) {
                     showStudentsInCourse(courseId);
                    }
                }

                    private void showStudentsInCourse(String courseId) {

                        String sql = "SELECT u.id_user, u.nume, u.prenume " +
                                "FROM utilizator u " +
                                "JOIN inrolare ic ON u.id_user = ic.id_student " +
                                "JOIN cursuri c ON ic.id_curs = c.id_cursuri " +
                                "WHERE c.id_cursuri = ?";

                        try (Connection connection = DatabaseConnection.getConnection()) {
                            PreparedStatement preparedStatement = connection.prepareStatement(sql);
                            preparedStatement.setString(1, courseId);  // Use the course ID here

                            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                                StringBuilder studentsInfo = new StringBuilder("Studenti inscrisi la curs:\n");

                                boolean foundStudents = false;
                                while (resultSet.next()) {
                                    foundStudents = true;
                                    studentsInfo.append("ID: ").append(resultSet.getString("id_user"))
                                            .append(" Nume: ").append(resultSet.getString("nume"))
                                            .append(" Prenume: ").append(resultSet.getString("prenume"))
                                            .append("\n");
                                }

                                if (!foundStudents) {
                                    studentsInfo.append("Nu sunt studenti inscrisi la acest curs.");
                                }

                                JOptionPane.showMessageDialog(pan, studentsInfo.toString(), "Studenti la curs", JOptionPane.INFORMATION_MESSAGE);
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(pan, "Eroare la afisarea studentilor!", "Eroare", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(pan, "Eroare la conectarea la baza de date!", "Eroare", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });


                JPanel butonPanel = new JPanel(new GridLayout(1, 2, 10, 10));
                butonPanel.add(btnAssignProf);
                butonPanel.add(btnViewStudents);


                cursuriPanel.add(butonPanel, BorderLayout.SOUTH);

                cardLayout.show(panelOutput, "Cursuri");

                cursuriPanel.revalidate();
                cursuriPanel.repaint();
            }
        });
    }

    private void windowRezultat(int courseId,String courseName, String[] professorNames) {
        JFrame rezultatFrame = new JFrame("Cursuri gasite");
        rezultatFrame.setSize(300, 150);
        rezultatFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        rezultatFrame.setLayout(new GridLayout(3, 1));

        JLabel lblCurs = new JLabel("Numele cursului: " + courseName);

        rezultatFrame.add(lblCurs);

        for(String s: professorNames) {
            JLabel lbProfessor = new JLabel("Numele profesorului: " + s);
            rezultatFrame.add(lbProfessor);

        }

        JButton btnViewStudent = new JButton("Vezi studenti");
        btnViewStudent.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sql = "SELECT u.id_user, u.nume, u.prenume " +
                        "FROM utilizator u " +
                        "JOIN inrolare ic ON u.id_user = ic.id_student  join cursuri c on c.id_cursuri = ic.id_curs" +
                        "WHERE c.id_cursuri = ?";

                try (Connection connection = DatabaseConnection.getConnection()) {
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setInt(1, courseId);

                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        StringBuilder studentsInfo = new StringBuilder("Studenti inscrisi la curs:\n");

                        boolean foundStudents = false;
                        while (resultSet.next()) {
                            foundStudents = true;
                            studentsInfo.append("ID: ").append(resultSet.getString("id_user"))
                                    .append(" Nume: ").append(resultSet.getString("nume"))
                                    .append(" Prenume: ").append(resultSet.getString("prenume"))
                                    .append("\n");
                        }

                        if (!foundStudents) {
                            studentsInfo.append("Nu sunt studenti inscrisi la acest curs.");
                        }

                        JOptionPane.showMessageDialog(pan, studentsInfo.toString(), "Studenti la curs", JOptionPane.INFORMATION_MESSAGE);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(pan, "Eroare la afisarea studentilor!", "Eroare", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(pan, "Eroare la conectarea la baza de date!", "Eroare", JOptionPane.ERROR_MESSAGE);
                }

                }

        });

        rezultatFrame.add(btnViewStudent);

        rezultatFrame.setLocationRelativeTo(null);
        rezultatFrame.setVisible(true);
    }

    private void showAllUsers(int roleId) {
        String sql = "SELECT id_user, nume, prenume, id_rol FROM utilizator WHERE id_rol = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, roleId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                StringBuilder usersInfo = new StringBuilder();

                if (roleId == 4) {
                    usersInfo.append("Studenti gasiti:\n");
                } else if (roleId == 3) {
                    usersInfo.append("Profesori gasiti:\n");
                }

                while (resultSet.next()) {
                    usersInfo.append("ID: ").append(resultSet.getString("id_user"))
                            .append(" Nume: ").append(resultSet.getString("nume"))
                            .append(" Prenume: ").append(resultSet.getString("prenume"))
                            .append(" Rol: ").append(resultSet.getInt("id_rol"))
                            .append("\n");
                }

                JOptionPane.showMessageDialog(pan, usersInfo.toString(), "Lista utilizatori", JOptionPane.INFORMATION_MESSAGE);

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(pan, "Eroare la afisarea utilizatorilor!", "Eroare", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(pan, "Eroare la conectarea la baza de date!", "Eroare", JOptionPane.ERROR_MESSAGE);
        }
    }

    public JPanel getPanel() {
        return pan;
    }
}

