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

public class StudentPanel {
    private JPanel panel;
    private JLabel lblUserImage;
    private JButton btnLogOut;
    private JButton btnCalendar;
    private JButton btnCursuri;
    private JButton btnNote;
    private JButton btnGrupuri;
    private JToolBar menuBar;
    private JPanel panelTop;
    private JPanel panelOutput;
    private JButton btnStudentName;
    private CardLayout cardLayout;
    private JButton btnDownloadCalendar;

    public StudentPanel(User user) {

        btnStudentName.setText(user.getNume() + " " + user.getPrenume());
        btnStudentName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sql = "SELECT u.id_user, u.CNP, u.nume, u.prenume, s.an_de_studiu, a.judet, a.localitate, a.strada, a.numar FROM utilizator u JOIN student s on s.id_student = u.id_user JOIN adresa a on a.id_adresa = u.id_adrasa WHERE u.id_user = ?";
                String stId = user.getId();

                try(Connection connection = DatabaseConnection.getConnection()){
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1, stId);

                    try(ResultSet resultSet = preparedStatement.executeQuery()){
                        if(resultSet.next()){
                            String id = resultSet.getString("id_user");
                            String CNP = resultSet.getString("CNP");
                            String nume = resultSet.getString("nume");
                            String prenume = resultSet.getString("prenume");
                            String anDeStudii = resultSet.getString("an_de_studiu");
                            String judet = resultSet.getString("judet");
                            String localitate = resultSet.getString("localitate");
                            String strada = resultSet.getString("strada");
                            String numar = resultSet.getString("numar");

                            String mesaj = String.format("ID user: %s\nCNP: %s\nNume: %s\nPrenume: %s\nAn de studiu: %s\nAdresa: judetul %s, localitatea %s, %s, numarul %s\n", id, CNP, nume, prenume, anDeStudii, judet, localitate, strada,  numar);

                            JOptionPane.showMessageDialog(panel, mesaj, "Informatii personale", JOptionPane.INFORMATION_MESSAGE);
                        }else{
                            JOptionPane.showMessageDialog(
                                    panel,
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

        JPanel calendarPanel = new JPanel();

        JPanel coursesPanel = new JPanel(new BorderLayout());

        JPanel notesPanel = new JPanel();


        panelOutput.add(calendarPanel, "Calendar");
        panelOutput.add(coursesPanel, "Cursuri");
        panelOutput.add(notesPanel, "Note");

        btnLogOut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });

        btnCursuri.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sql = "SELECT id_cursuri, nume FROM inrolare JOIN cursuri ON id_curs = id_cursuri WHERE id_student = ?";
                String stId = user.getId();

                DefaultListModel<String> cursuriModel = new DefaultListModel<>();

                try(Connection connection = DatabaseConnection.getConnection()){
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1, stId);

                    try(ResultSet resultSet = preparedStatement.executeQuery()){
                        while(resultSet.next()){
                            String courseName = "|" + resultSet.getString("id_cursuri") +"| " + resultSet.getString("nume");
                            cursuriModel.addElement(courseName);

                        }
                    }catch(SQLException ex){
                        ex.printStackTrace();
                    }
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                JList<String> cursuriList = new JList<>(cursuriModel);
                JScrollPane cursuriScroller = new JScrollPane(cursuriList);

                cursuriScroller.setPreferredSize(new Dimension(450, 200));

                JTextField searchField = new JTextField();
                searchField.setPreferredSize(new Dimension(450, 30));
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
                        if(filteredListModel.size() == 0){
                            filteredListModel.addElement("Nu s-au gasit cursuri cu acest nume!");
                        }

                        cursuriList.setModel(filteredListModel);

                    }
                });

                JPanel searchPanel = new JPanel();
                searchPanel.setLayout(new BorderLayout());
                searchPanel.add(searchField, BorderLayout.NORTH);
                searchPanel.add(cursuriScroller, BorderLayout.CENTER);

                coursesPanel.removeAll();

                coursesPanel.add(searchPanel, BorderLayout.NORTH);

                //buton pentru inscriere
                JButton btnInscriere = new JButton("Inscriere la curs");
                btnInscriere.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        windowInscriere(user);
                    }
                });



                //buton pentru renuntare la cursuri
                JButton btnRenuntare = new JButton("Renuntare la curs");
                btnRenuntare.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        windowRenuntare(user);
                    }
                });

                //buton pentru vizualizarea profesorilor
                JButton btnProf = new JButton("Profesori");
                btnProf.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        showAllUsers(3);
                    }
                });
                JPanel butonPanel = new JPanel(new GridLayout(1, 2, 10, 10));
                butonPanel.add(btnProf);
                butonPanel.add(btnInscriere);
                butonPanel.add(btnRenuntare);


                coursesPanel.add(butonPanel, BorderLayout.SOUTH);


                cardLayout.show(panelOutput, "Cursuri");
            }
        });
        btnNote.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sql = "SELECT c.nume,i.nota_curs, i.nota_seminar, i.nota_lab, i.nota_finala FROM inrolare i JOIN cursuri c on i.id_curs = c.id_cursuri WHERE i.id_student = ?";
                String stId = user.getId();

                String[] header = {"Nume", "Nota Curs", "Nota Seminar", "Nota Laborator", "Nota Finala"};
                DefaultTableModel tableModel = new DefaultTableModel(header, 0){
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };



                try(Connection connection = DatabaseConnection.getConnection()){
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1, stId);

                    try (ResultSet resultSet = preparedStatement.executeQuery()){
                        while(resultSet.next()){
                            String numeCurs = resultSet.getString("nume");
                            String notaCurs = resultSet.getString("nota_curs");
                            String notaSeminar = resultSet.getString("nota_seminar");
                            String notaLaborator = resultSet.getString("nota_lab");
                            String notaFinala = resultSet.getString("nota_finala");

                            tableModel.addRow(new Object[]{numeCurs, notaCurs, notaSeminar, notaLaborator, notaFinala});
                        }
                    }

                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }

                JTable tabel = new JTable(tableModel);

                JScrollPane noteScroll = new JScrollPane(tabel);

                noteScroll.setPreferredSize(new Dimension(430, 200));

                notesPanel.removeAll();
                notesPanel.add(noteScroll, BorderLayout.CENTER);
                cardLayout.show(panelOutput, "Note");

            }
        });
        btnCalendar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] zile = {"luni", "marti", "miercuri", "joi", "vineri"};

                String ziuaDeAzi = getZi();
                int index = 0;
                for(int i = 0; i < zile.length; i++){
                    if(zile[i].equals(ziuaDeAzi)){
                        index=i;
                        break;
                    }
                }

                //combo box pt alegerea zilei
                JComboBox<String> comboBoxZi = new JComboBox<>(zile);
                comboBoxZi.setSelectedIndex(index);

                JPanel ziPanel = new JPanel();
                ziPanel.add(new JLabel("Zi:"));
                ziPanel.add(comboBoxZi);


                String[] header = {"Curs", "Activitate", "Ora Inceput", "Ora Sfarsit"};
                DefaultTableModel tableModel = new DefaultTableModel(header, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

                JTable activitatiTabel = new JTable(tableModel);
                JScrollPane scrollPane = new JScrollPane(activitatiTabel);


                ActionListener updateTableListener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String zi = (String) comboBoxZi.getSelectedItem();
                        tableModel.setRowCount(0);

                        String sql = "SELECT c.nume, a.tip, cal.data_inceput, cal.data_sfarsit "+
                                "FROM activitate a " +
                                "JOIN calendar cal ON a.id_desfasurare = cal.id_desfasurare " +
                                "JOIN cursuri c ON a.id_curs = c.id_cursuri "+
                                "JOIN inrolare i ON c.id_cursuri = i.id_curs "+
                                "JOIN inrolare_activitati ia ON a.id_ora = ia.id_activitate " +
                                "WHERE i.id_student = ? "+
                                "AND cal.zi = ? " +
                                "AND ia.id_student = i.id_student AND a.tip IN ('curs', 'seminar', 'laborator') ORDER BY cal.data_inceput;";

                        try (Connection connection = DatabaseConnection.getConnection()) {

                            String stId = user.getId();

                            PreparedStatement preparedStatement = connection.prepareStatement(sql);
                            preparedStatement.setString(1, stId);
                            preparedStatement.setString(2, zi);

                            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                                while (resultSet.next()) {
                                    String cursName = resultSet.getString("nume");
                                    String activitate = resultSet.getString("tip");
                                    String oraInceput = resultSet.getString("data_inceput");
                                    String oraSfarsit = resultSet.getString("data_sfarsit");

                                    tableModel.addRow(new Object[]{cursName, activitate, oraInceput, oraSfarsit});
                                }
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }

                    }
                };


               comboBoxZi.addActionListener(updateTableListener);

               updateTableListener.actionPerformed(null);

                btnDownloadCalendar = new JButton("Descarcare calendar");
                btnDownloadCalendar.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser chooser = new JFileChooser();
                        chooser.setDialogTitle("Descarcare calendar");
                        chooser.setSelectedFile(new java.io.File("calendar.csv"));

                        int uSelect = chooser.showSaveDialog(panel);

                        if(uSelect == JFileChooser.APPROVE_OPTION){
                            java.io.File file = chooser.getSelectedFile();

                            try(FileWriter writer = new FileWriter(file)){
                                TableModel model = activitatiTabel.getModel();

                                for(int i = 0; i< model.getColumnCount(); i++){
                                    writer.write(model.getColumnName(i) + (i < model.getColumnCount() -1 ? ",": ""));

                                }
                                writer.write("\n");

                                for(int i=0; i<model.getRowCount(); i++){
                                    for(int j=0; j<model.getColumnCount(); j++){
                                        writer.write(model.getValueAt(i, j).toString() + (j<model.getColumnCount() -1 ? ",": ""));
                                    }
                                    writer.write("\n");
                                }
                                JOptionPane.showMessageDialog(panelOutput, "Calendar salvat cu succes!" + file.getAbsolutePath(), "Succes", JOptionPane.INFORMATION_MESSAGE);
                            } catch (IOException ex) {
                                JOptionPane.showMessageDialog(panelOutput, ex.getMessage(), "Eroare", JOptionPane.ERROR_MESSAGE);
                                throw new RuntimeException(ex);
                            }


                        }
                    }
                });

                JButton btnInscriere = new JButton("Inscriere la o activitate noua");
                btnInscriere.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        windowInscriereActivitate(user);
                    }
                });
                calendarPanel.removeAll();
                calendarPanel.setLayout(new BorderLayout());
                calendarPanel.add(ziPanel, BorderLayout.NORTH);
                calendarPanel.add(scrollPane, BorderLayout.CENTER);

                JPanel butonPanel = new JPanel(new GridLayout(1, 2, 10, 10));
                butonPanel.add(btnInscriere);
                butonPanel.add(btnDownloadCalendar);


                calendarPanel.add(butonPanel, BorderLayout.SOUTH);
                calendarPanel.revalidate();
                calendarPanel.repaint();

                cardLayout.show(panelOutput, "Calendar");
            }
        });

        btnGrupuri.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sql = "SELECT g.nume, c.nume AS curs, g.id_grup FROM grup_de_studi g JOIN inrolare_grup_de_studiu igs on g.id_grup = igs.id_grup JOIN student s ON igs.id_student = s.id_student JOIN cursuri c on c.id_cursuri = g.id_curs WHERE s.id_student = ?";
                String stId = user.getId();

                //buton pentru inscriere intr-un grup
                JButton btnInscriere = new JButton("Inscriere intr-un grup nou");
                btnInscriere.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        windowInscriereGrup(user);
                    }
                });

                //buton pentru creare grup nou
                JButton btnCreare = new JButton("Creare grup nou");
                btnCreare.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        windowCreareGrup();
                    }
                });

                JPanel grupuriPanel = new JPanel();
                grupuriPanel.setLayout(new BoxLayout(grupuriPanel, BoxLayout.Y_AXIS));

                try(Connection connection = DatabaseConnection.getConnection()){
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1, stId);

                    try(ResultSet resultSet = preparedStatement.executeQuery()){
                        boolean rez = false;
                        while(resultSet.next()){
                            rez=true;
                            String nume = resultSet.getString("nume");
                            String curs = resultSet.getString("curs");
                            String id = resultSet.getString("id_grup");


                            JButton groupButton = new JButton(nume);
                            groupButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    showGrupPanel(user, nume, curs, id);
                                }
                            });

                            grupuriPanel.add(groupButton);

                        }
                        if(!rez){
                            JLabel gr = new JLabel("Nu faci parte din grupuri de studiu!");
                            grupuriPanel.add(gr);
                        }
                    }
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }

                JScrollPane grupuriScroll = new JScrollPane(grupuriPanel);

                JPanel grupPanel = new JPanel();
                grupPanel.add(grupuriScroll, BorderLayout.CENTER);

                JPanel buttonPanel = new JPanel();
                buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
                buttonPanel.add(btnInscriere);
                buttonPanel.add(btnCreare);

                grupPanel.add(buttonPanel, BorderLayout.SOUTH);
                panelOutput.add(grupPanel, "Grupuri");
                cardLayout.show(panelOutput, "Grupuri");
            }
        });


    }


    private void showGrupPanel(User user, String nume, String curs, String id){
        JPanel grupPanel = new JPanel(new BorderLayout());

        //partea stanga pentru afisarea informatiilor
        JPanel leftPanel = new JPanel(new BorderLayout());

        //informatii despre grup
        JLabel info = new JLabel("<html><b>Nume grup:</b> " + nume + "<br/><b>Nume curs:</b> " + curs + "</html>");
        info.setVerticalAlignment(SwingConstants.TOP);
        info.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        leftPanel.add(info, BorderLayout.NORTH);

        //label participanti
        JPanel participantiPanel = new JPanel();
        participantiPanel.setLayout(new BoxLayout(participantiPanel, BoxLayout.Y_AXIS));

        JLabel partLabel = new JLabel("Participanti:");
        partLabel.setHorizontalAlignment(SwingConstants.CENTER);
        participantiPanel.add(partLabel);

        DefaultListModel<String> studenti = new DefaultListModel<>();
        JList<String> participanti = new JList<>(studenti);
        participantiPanel.add(new JScrollPane(participanti));

        //parasire grup
        JButton btnParasire = new JButton("Parasire grup");
        btnParasire.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        panel,
                        "Esti sigur ca vrei sa parasesti grupul?",
                        "Confirmare Parasire Grup",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE

                );

                if (confirm == JOptionPane.YES_OPTION) {
                renuntareGrup(user.getId(), id);}
            }
        });

        //buton pentru adaugare activitate
        JButton btnAdaugare = new JButton("Adaugare activitate");
        btnAdaugare.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                windowAdaugareActivitate(user, id);
            }
        });

        //buton pentru inscriere la activitate
        JButton btnInscriere = new JButton("Inscriere la activitati");
        btnInscriere.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                windowInscriereActivitateGrup(user, id);
            }
        });

        //buton pentru afisarea sugestiilor de participanti
        JButton btnSugestii = new JButton("Sugestii de participanti");
        btnSugestii.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                popUpSugestii(id);
            }
        });

        //buton pentru adaugare profesor la activitate
        JButton btnProfesor = new JButton("Adaugare profesor la activitate");
        btnProfesor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                windowAdaugareProfesor();
            }
        });

        participantiPanel.add(Box.createVerticalStrut(10));

        participantiPanel.add(btnSugestii);
        participantiPanel.add(btnAdaugare);
        participantiPanel.add(btnInscriere);
        participantiPanel.add(btnProfesor);
        participantiPanel.add(btnParasire);


        leftPanel.add(participantiPanel, BorderLayout.CENTER);

        //accesare date
        try(Connection connection = DatabaseConnection.getConnection()){
            String sql = "SELECT u.nume, u.prenume FROM utilizator u JOIN student s ON u.id_user = s.id_student JOIN inrolare_grup_de_studiu igs ON s.id_student = igs.id_student JOIN grup_de_studi g ON igs.id_grup = g.id_grup WHERE g.id_grup = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, id);

            try(ResultSet resultSet = preparedStatement.executeQuery()){
                while(resultSet.next()){
                    String numeStudent = resultSet.getString("nume") + " " + resultSet.getString("prenume");
                    studenti.addElement(numeStudent);
                }
            }



        } catch (SQLException e) {
            throw new RuntimeException(e);
        }



        //partea dreapta: chat
        JPanel rightPanel = new JPanel(new BorderLayout());

        JTextArea chat = new JTextArea();
        chat.setEditable(false);
        rightPanel.add(new JScrollPane(chat), BorderLayout.CENTER);

        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT m.text, m.time, u.nume, u.prenume FROM mesaj m " +
                    "JOIN utilizator u ON m.id_student = u.id_user WHERE m.id_grup = ? ORDER BY m.time ASC";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String numePrenume = resultSet.getString("nume") + " " + resultSet.getString("prenume");
                    String mesaj = resultSet.getString("text");
                    Timestamp timestamp = resultSet.getTimestamp("time");

                    chat.append(String.format("[%s] %s: %s\n", timestamp, numePrenume, mesaj));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT a.id_activitate, a.nume, a.termen_inscriere, a.nr_min_participanti, " +
                    "       (SELECT COUNT(*) FROM inrolare_act_grup ia WHERE ia.id_activitate = a.id_activitate) AS nr_participanti " +
                    "FROM activitate_grup a WHERE a.id_grup = ? AND canceled=0";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String activitateNume = resultSet.getString("nume");
                    Timestamp termenInscriere = resultSet.getTimestamp("termen_inscriere");
                    int nrMinParticipanti = resultSet.getInt("nr_min_participanti");
                    int nrParticipanti = resultSet.getInt("nr_participanti");
                    String activitateId = resultSet.getString("id_activitate");

                    Timestamp currentDate = new Timestamp(System.currentTimeMillis());

                    if (termenInscriere != null && termenInscriere.before(currentDate)) {
                        if (nrParticipanti < nrMinParticipanti) {
                            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                            String sql2 = "INSERT INTO mesaj (text, id_grup, time, id_student) VALUES (?, ?, ?, 0)";
                            try (PreparedStatement preparedStatement2 = connection.prepareStatement(sql2)) {
                                String mesaj =String.format(
                                        "Activitatea \"%s\" a fost anulata (nu a fost atins numarul minim de persoane: %d/%d).",
                                        activitateNume, nrParticipanti, nrMinParticipanti);
                                preparedStatement2.setString(1, mesaj);
                                preparedStatement2.setString(2, id);
                                preparedStatement2.setTimestamp(3, timestamp);
                                preparedStatement2.executeUpdate();


                            }


                            String sqlCancel = "UPDATE activitate_grup SET canceled = 1 WHERE id_activitate = ?";
                            try (PreparedStatement preparedStatementCancel = connection.prepareStatement(sqlCancel)) {
                                preparedStatementCancel.setInt(1, Integer.parseInt(activitateId));
                                preparedStatementCancel.executeUpdate();
                                System.out.println("Activitatea cu id-ul " + activitateId + " a fost marcat anulat.");
                            } catch (SQLException e) {
                                JOptionPane.showMessageDialog(null, "Eroare anulare activitate.");
                                e.printStackTrace();
                            }

                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        JPanel mesajPanel = new JPanel(new BorderLayout());
        JTextField mesajField = new JTextField();
        JButton send = new JButton("Trimite");

        send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String mesaj = mesajField.getText().trim();
                if(!mesaj.isEmpty()){

                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    String sql = "INSERT INTO mesaj (text, id_grup, time, id_student) VALUES (?, ?, ?, ?)";
                    try(Connection connection = DatabaseConnection.getConnection()){
                       PreparedStatement preparedStatement = connection.prepareStatement(sql);
                       preparedStatement.setString(1, mesaj);
                       preparedStatement.setString(2, id);
                       preparedStatement.setTimestamp(3, timestamp);
                       String stId = user.getId();
                       preparedStatement.setString(4, stId);

                       preparedStatement.executeUpdate();

                        chat.append(String.format("[%s] %s: %s\n", timestamp, user.getNume() + " " + user.getPrenume(), mesaj));
                        mesajField.setText("");
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });

        mesajPanel.add(mesajField, BorderLayout.CENTER);
        mesajPanel.add(send, BorderLayout.EAST);
        rightPanel.add(mesajPanel, BorderLayout.SOUTH);

        JSplitPane splitPane =  new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(200);
        grupPanel.add(splitPane, BorderLayout.CENTER);

        panelOutput.add(grupPanel, "Grup");
        cardLayout.show(panelOutput, "Grup");

    }

    private String getZi(){
        LocalDate astazi = LocalDate.now();
        String zi = astazi.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("ro"));
        return zi;
    }
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                panel,
                "Esti sigur ca vrei sa te deconectezi?",
                "Confirmare Deconectare",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE

        );

        if (confirm == JOptionPane.YES_OPTION) {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(panel);
            if (parentFrame != null) {
                parentFrame.dispose();
            }
            new LoginPage(null);
        }
    }



    public JPanel getPanel() {
        return panel;
    }

    public void setPanel1(JPanel panel1) {
        this.panel = panel1;
    }
    public void windowInscriere(User user){
        JFrame inscriereFrame = new JFrame("Inscriere");
        inscriereFrame.setSize(300, 150);
        inscriereFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        inscriereFrame.setLayout(new GridLayout(3, 1));

        JLabel lblCurs = new JLabel("ID-ul cursului: ");
        JTextField tfCurs = new JTextField();

        JButton btnConfirmare = new JButton("Inscriere");
        btnConfirmare.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tCurs = tfCurs.getText();
                if(!tCurs.isEmpty()){
                    try {
                        int idCurs = Integer.parseInt(tCurs);
                        int idStudent = Integer.parseInt(user.getId());
                        inscrieStudent(idStudent, idCurs);
                        JOptionPane.showMessageDialog(inscriereFrame, "Inscriere cu succes!");
                    }catch(NumberFormatException ex){
                        JOptionPane.showMessageDialog(inscriereFrame, "ID curs invalida!");
                    }

                }

            }
        });
        inscriereFrame.add(lblCurs);
        inscriereFrame.add(tfCurs);
        inscriereFrame.add(btnConfirmare);

        inscriereFrame.setLocationRelativeTo(null);
        inscriereFrame.setVisible(true);
    }

    public void inscrieStudent(int idStudent, int idCurs){
        try(Connection connection = DatabaseConnection.getConnection()){
            String sql = "SELECT p.id_profesor, COUNT(s.id_student) AS student_count FROM profesor p JOIN prof_cursuri pc ON pc.id_profesor = p.id_profesor JOIN cursuri c ON pc.id_curs = c.id_cursuri JOIN inrolare i ON i.id_curs = c.id_cursuri JOIN student s ON i.id_student = s.id_student WHERE pc.id_curs = ? GROUP BY p.id_profesor ORDER BY  student_count ASC LIMIT 1";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, idCurs);

            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()){
                int profId = resultSet.getInt("id_profesor");
                String sqlInr = "INSERT INTO inrolare (id_student, id_curs, id_prof) VALUES (?, ?, ?)";
                PreparedStatement preparedStatement1 = connection.prepareStatement(sqlInr);
                preparedStatement1.setInt(1, idStudent);
                preparedStatement1.setInt(2, idCurs);
                preparedStatement1.setInt(3, profId);

                preparedStatement1.executeUpdate();
                System.out.println("Student inrolat cu succes.");
            }else{
                System.out.println("Nu s-a gasit profesor.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void windowRenuntare(User user){
        JFrame renuntareFrame = new JFrame("Renuntare");
        renuntareFrame.setSize(300, 150);
        renuntareFrame.getDefaultCloseOperation();
        renuntareFrame.setLayout(new GridLayout(3, 1));

        JLabel lblCurs = new JLabel("ID-ul cursului: ");
        JTextField tfCurs = new JTextField();

        JButton btnConfirmare = new JButton("Renuntare");
        btnConfirmare.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tCurs = tfCurs.getText();
                if(!tCurs.isEmpty()){
                    try {
                        int idCurs = Integer.parseInt(tCurs);
                        int idStudent = Integer.parseInt(user.getId());
                        renuntareCurs(idStudent, idCurs);
                        JOptionPane.showMessageDialog(renuntareFrame, "Renuntare cu succes!");
                    }catch(NumberFormatException ex){
                        JOptionPane.showMessageDialog(renuntareFrame, "ID curs invalida!");
                    }

                }

            }
        });
        renuntareFrame.add(lblCurs);
        renuntareFrame.add(tfCurs);
        renuntareFrame.add(btnConfirmare);

        renuntareFrame.setLocationRelativeTo(null);
        renuntareFrame.setVisible(true);
    }

    public void renuntareCurs(int idStudent, int idCurs){
        try(Connection connection = DatabaseConnection.getConnection()){

            String sql = "SELECT * FROM inrolare WHERE id_student = ? AND id_curs = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, idStudent);
            preparedStatement.setInt(2, idCurs);

            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()){
                String sqlDel = "DELETE FROM inrolare WHERE id_student = ? AND id_curs = ?";
                PreparedStatement preparedStatement1 = connection.prepareStatement(sqlDel);
                preparedStatement1.setInt(1, idStudent);
                preparedStatement1.setInt(2, idCurs);

                preparedStatement1.executeUpdate();
                System.out.println("Renuntare cu succes");
            }else{
                System.out.println("Studentul nu este inrolat la acest curs");
            }



        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void windowInscriereActivitate(User user){
        JFrame inscriereFrame = new JFrame("Inscriere");
        inscriereFrame.setSize(300, 150);
        inscriereFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        inscriereFrame.setLayout(new GridLayout(3, 1));

        JLabel lblCurs = new JLabel("ID-ul cursului: ");
        JTextField tfCurs = new JTextField();

        JLabel lblTip = new JLabel("Tipul activitatii (curs/laborator/seminar): ");
        JTextField tfTip = new JTextField();

        JButton btnConfirmare = new JButton("Inscriere");
        btnConfirmare.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tCurs = tfCurs.getText();
                String tTip = tfTip.getText();
                if(!tCurs.isEmpty() && !tTip.isEmpty()){
                    try {
                        int idCurs = Integer.parseInt(tCurs);
                        int idStudent = Integer.parseInt(user.getId());
                        inscrieStudent(idStudent, idCurs, tTip);

                    }catch(NumberFormatException ex){
                        JOptionPane.showMessageDialog(inscriereFrame, "ID curs invalida!");
                    }

                }

            }
        });
        inscriereFrame.add(lblCurs);
        inscriereFrame.add(tfCurs);
        inscriereFrame.add(lblTip);
        inscriereFrame.add(tfTip);

        inscriereFrame.add(btnConfirmare);

        inscriereFrame.setLocationRelativeTo(null);
        inscriereFrame.setVisible(true);
    }

    public void inscrieStudent(int idStudent, int idCurs, String tip){
        try(Connection connection = DatabaseConnection.getConnection()){
            if(!studentInscris(idStudent, idCurs, connection)){
                JOptionPane.showMessageDialog(null, "Nu esti inscris la cursul ales!");
                return;
            }
            String sql = "SELECT a.id_ora, a.nr_max_part, cal.zi, cal.data_inceput, cal.data_sfarsit FROM activitate a JOIN calendar cal ON cal.id_desfasurare = a.id_desfasurare WHERE a.id_curs = ? AND a.tip = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, idCurs);
            preparedStatement.setString(2, tip);
            ResultSet resultSet = preparedStatement.executeQuery();

            boolean loc = false;
            boolean activitate = false;

            while(resultSet.next()) {
                int idActivitate = resultSet.getInt("id_ora");
                String inceput = resultSet.getString("data_inceput");
                String sfarsit = resultSet.getString("data_sfarsit");
                String zi =  resultSet.getString("zi");
                int nrMax = resultSet.getInt("nr_max_part");

                String sqlCheck = "SELECT COUNT(*) FROM inrolare_activitati WHERE id_student = ? AND id_activitate = ?";
                PreparedStatement pr = connection.prepareStatement(sqlCheck);
                pr.setInt(1, idStudent);
                pr.setInt(2, idActivitate);

                ResultSet rs = pr.executeQuery();

                if(rs.next() && rs.getInt(1)>0){
                    JOptionPane.showMessageDialog(null, "Esti deja inscris la aceasta activitate!");
                    return;
                }


                if(eDejaOcupat(idStudent, zi, inceput, sfarsit, connection)){
                    continue;
                }

                if(eDejaPlin(idActivitate, nrMax, connection)){
                    continue;
                }

                loc = true;
                activitate = true;

                String sqlInr = "INSERT INTO inrolare_activitati (id_student, id_activitate) VALUES (?, ?)";
                PreparedStatement preparedStatement1 = connection.prepareStatement(sqlInr);
                preparedStatement1.setInt(1, idStudent);
                preparedStatement1.setInt(2, idActivitate);

                preparedStatement1.executeUpdate();
                JOptionPane.showMessageDialog(null, "Inscriere cu succes!");
                System.out.println("Student inrolat cu succes.");
            }
            if(!activitate){
                JOptionPane.showMessageDialog(null, "Nu exista activitati disponibile!");
                return;
            }else if(!loc){
                JOptionPane.showMessageDialog(null, "Activitatea nu mai are locuri disponibile!");
                return;
           }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    public boolean studentInscris(int idStudent, int idCurs, Connection connection) throws SQLException {

            String sql = "SELECT COUNT(*) FROM inrolare WHERE id_student = ? AND id_curs = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, idStudent);
            preparedStatement.setInt(2, idCurs);

            ResultSet resultSet = preparedStatement.executeQuery();


            return resultSet.next() && resultSet.getInt(1) > 0;

    }

    public boolean eDejaOcupat(int idStudent, String zi, String start, String end, Connection connection) throws SQLException {
        String sql1 = "SELECT COUNT(*) FROM calendar cal JOIN activitate a ON a.id_desfasurare = cal.id_desfasurare JOIN inrolare_activitati ia ON a.id_ora = ia.id_activitate JOIN student s ON ia.id_student = s.id_student WHERE s.id_student = ? AND cal.zi = ? AND cal.data_inceput = ? AND cal.data_sfarsit = ?";

        PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
        preparedStatement1.setInt(1, idStudent);
        preparedStatement1.setString(2, zi);
        preparedStatement1.setString(3, start);
        preparedStatement1.setString(4, end);

        ResultSet resultSet1 = preparedStatement1.executeQuery();

        if(resultSet1.next() && resultSet1.getInt(1) > 0){
            return true;
        }
        return false;
    }

    public boolean eDejaPlin(int idActivitate, int nrMax, Connection connection) throws SQLException {
        String sql = "SELECT COUNT(*) AS nr_inscrisi FROM inrolare_activitati WHERE id_activitate = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, idActivitate);

        ResultSet resultSet = preparedStatement.executeQuery();

        return resultSet.next() && resultSet.getInt("nr_inscrisi") >= nrMax;
    }

    public void windowInscriereGrup(User user){
        JFrame inscriereFrame = new JFrame("Inscriere");
        inscriereFrame.setSize(300, 150);
        inscriereFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        inscriereFrame.setLayout(new GridLayout(3, 1));

        JLabel lblGrup = new JLabel("ID-ul grupului: ");
        JTextField tfGrup = new JTextField();


        JButton btnConfirmare = new JButton("Inscriere");
        btnConfirmare.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tGrup = tfGrup.getText();

                if(!tGrup.isEmpty() ){
                    try {
                        int idGrup = Integer.parseInt(tGrup);
                        int idStudent = Integer.parseInt(user.getId());
                        inscrieStudentGrup(idStudent, idGrup);

                    }catch(NumberFormatException ex){
                        JOptionPane.showMessageDialog(inscriereFrame, "ID grup invalid!");
                    }

                }

            }
        });
        inscriereFrame.add(lblGrup);
        inscriereFrame.add(tfGrup);


        inscriereFrame.add(btnConfirmare);

        inscriereFrame.setLocationRelativeTo(null);
        inscriereFrame.setVisible(true);
    }

    public void inscrieStudentGrup(int idStudent, int idGrup){
        try(Connection connection = DatabaseConnection.getConnection()){

            String sql1 = "SELECT id_curs FROM grup_de_studi WHERE id_grup = ?";
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql1);
            preparedStatement2.setInt(1, idGrup);

            ResultSet resultSet1 = preparedStatement2.executeQuery();

            if (resultSet1.next()) {
                int idCurs = resultSet1.getInt("id_curs");
                if(!studentInscris(idStudent, idCurs, connection)){
                    JOptionPane.showMessageDialog(null, "Nu esti inscris la cursul grupului!");
                    return;
                }


                String sqlInr = "INSERT INTO inrolare_grup_de_studiu (id_student, id_grup) VALUES (?, ?)";
                PreparedStatement preparedStatement1 = connection.prepareStatement(sqlInr);
                preparedStatement1.setInt(1, idStudent);
                preparedStatement1.setInt(2, idGrup);

                preparedStatement1.executeUpdate();
                JOptionPane.showMessageDialog(null, "Inscriere cu succes!");
                System.out.println("Student inrolat cu succes.");
            } else {
                JOptionPane.showMessageDialog(null, "Grupul nu exista!");
                return;
            }





        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }
    public void renuntareGrup(String idStudent, String idGrup){
        try(Connection connection = DatabaseConnection.getConnection()){




                String sqlDel = "DELETE FROM inrolare_grup_de_studiu WHERE id_student = ? AND id_grup = ?";
                PreparedStatement preparedStatement1 = connection.prepareStatement(sqlDel);
                preparedStatement1.setString(1, idStudent);
                preparedStatement1.setString(2, idGrup);

                preparedStatement1.executeUpdate();
                System.out.println("Renuntare cu succes");



        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void windowAdaugareActivitate(User user, String idGrup){
        JFrame adaugareFrame = new JFrame("Adaugare");
        adaugareFrame.setSize(474, 450);
        adaugareFrame.getDefaultCloseOperation();
        adaugareFrame.setLayout(new BoxLayout(adaugareFrame.getContentPane(), BoxLayout.Y_AXIS));

        //nume
        JLabel lblNume = new JLabel("Nume activitate:");
        JTextField tfNume = new JTextField();

        //numar minim participanti
        JLabel lblPart = new JLabel("Numar minim participanti:");
        JTextField tfPart = new JTextField();

        //pana la ce data se poate inscrie
        JLabel lblInscr = new JLabel("Termen inscriere:");
        JTextField tfInscr = new JTextField();

        //data desfasurare
        JLabel lblData = new JLabel("Data activitate:");
        JTextField tfData = new JTextField();

        //ora inceput
        JLabel lblInceput = new JLabel("Ora inceput:");
        JTextField tfInceput = new JTextField();

        //ora sfarsit
        JLabel lblSfarsit = new JLabel("Ora sfarsit:");
        JTextField tfSfarsit = new JTextField();



        JButton btnConfirmare = new JButton("Adaugare");
        btnConfirmare.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tNume = tfNume.getText();
                String tPart = tfPart.getText();
                String tInscr = tfInscr.getText();
                String tData = tfData.getText();
                String tInceput = tfInceput.getText();
                String tSfarsit = tfSfarsit.getText();

                if(!tNume.isEmpty() && !tPart.isEmpty() && !tInscr.isEmpty() && !tData.isEmpty() && !tInceput.isEmpty() && !tSfarsit.isEmpty() ){
                    try {
                        Connection connection = DatabaseConnection.getConnection();
                        String sql = "INSERT INTO activitate_grup (data_desfasurare, termen_inscriere, nr_min_participanti, nume, ora_inceput, ora_sfarsit, id_grup) VALUES (?, ?, ?, ?, ?, ?, ?)";
                        PreparedStatement preparedStatement = connection.prepareStatement(sql);

                        preparedStatement.setString(1, tData);
                        preparedStatement.setString(2, tInscr);
                        preparedStatement.setString(3, tPart);
                        preparedStatement.setString(4, tNume);
                        preparedStatement.setString(5, tInceput);
                        preparedStatement.setString(6, tSfarsit);
                        preparedStatement.setString(7, idGrup);

                        preparedStatement.executeUpdate();
                        JOptionPane.showMessageDialog(null, "Activitate adaugata cu succes!");

                    }catch(NumberFormatException ex){
                        JOptionPane.showMessageDialog(adaugareFrame, "Date invalide");
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }

                }

            }
        });
        adaugareFrame.add(lblNume);
        adaugareFrame.add(tfNume);

        adaugareFrame.add(lblPart);
        adaugareFrame.add(tfPart);

        adaugareFrame.add(lblData);
        adaugareFrame.add(tfData);


        adaugareFrame.add(lblInceput);
        adaugareFrame.add(tfInceput);

        adaugareFrame.add(lblSfarsit);
        adaugareFrame.add(tfSfarsit);

        adaugareFrame.add(lblInscr);
        adaugareFrame.add(tfInscr);

        adaugareFrame.add(btnConfirmare);

        adaugareFrame.setLocationRelativeTo(null);
        adaugareFrame.setVisible(true);
    }

    public void windowInscriereActivitateGrup(User user, String idGrup){
        JFrame inscriereFrame = new JFrame("Adaugare");
        inscriereFrame.setSize(474, 450);
        inscriereFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        inscriereFrame.setLayout(new BoxLayout(inscriereFrame.getContentPane(), BoxLayout.Y_AXIS));

        try(Connection connection = DatabaseConnection.getConnection()){
            String sql = "select id_activitate, nume from activitate_grup where id_grup = ? AND termen_inscriere >= now()";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, idGrup);

            try(ResultSet resultSet = preparedStatement.executeQuery()){
                boolean rez = false;
                while(resultSet.next()){
                    rez=true;
                    String nume = resultSet.getString("nume");
                    String id = resultSet.getString("id_activitate");


                    JButton actButton = new JButton(nume + " - id: " + id);
                    actButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                                inscriereActivitateGrup(user.getId(), id, inscriereFrame);
                        }
                    });

                    inscriereFrame.add(actButton);

                }
                if(!rez){
                    JLabel gr = new JLabel("Nu exista activitati!");
                    inscriereFrame.add(gr);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        inscriereFrame.setVisible(true);

    }

    public void inscriereActivitateGrup(String idStudent, String idActivitate, JFrame inscriereFrame)  {
        try{
            Connection connection=DatabaseConnection.getConnection();

            String sql = "INSERT INTO inrolare_act_grup VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, idActivitate);
            preparedStatement.setString(2, idStudent);

            preparedStatement.executeUpdate();
            JOptionPane.showMessageDialog(null, "Inscriere reusita!");


        } catch (SQLException e) {
            System.out.println("Inscriere nereusita");
            JOptionPane.showMessageDialog(null, "Inscriere nereusita");

        }

    }

    public void popUpSugestii(String idGrup) {
        String sql = "SELECT DISTINCT u.nume, u.prenume, c.nume "  +
        "FROM student s " +
                "JOIN utilizator u ON s.id_student = u.id_user " +
        "JOIN inrolare i ON s.id_student = i.id_student " +
        "JOIN cursuri c ON i.id_curs = c.id_cursuri " +
        "JOIN grup_de_studi gs ON c.id_cursuri = gs.id_curs "+
        "WHERE gs.id_grup = ? "+
        "AND s.id_student NOT IN ( " +
                "SELECT DISTINCT igs.id_student " +
                "FROM inrolare_grup_de_studiu igs "+
                "WHERE igs.id_grup = ?)";


        DefaultListModel<String> studenti = new DefaultListModel<>();
        JList<String> sugestiiList = new JList<>(studenti);


        try(Connection connection = DatabaseConnection.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, idGrup);
            preparedStatement.setString(2, idGrup);

            try(ResultSet resultSet = preparedStatement.executeQuery()){
                while(resultSet.next()){
                    String numeStudent = resultSet.getString("nume") + " " + resultSet.getString("prenume");
                    studenti.addElement(numeStudent);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        JScrollPane scrollPane = new JScrollPane(sugestiiList);
        JPanel sugestii = new JPanel(new BorderLayout());

        sugestii.add(scrollPane, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(null, sugestii, "Sugestii studenti",JOptionPane.PLAIN_MESSAGE );

    }

    public void windowAdaugareProfesor(){
        JFrame adaugareFrame = new JFrame("Adaugare");
        adaugareFrame.setSize(200, 200);
        adaugareFrame.getDefaultCloseOperation();
        adaugareFrame.setLayout(new BoxLayout(adaugareFrame.getContentPane(), BoxLayout.Y_AXIS));

        //id activitate
        JLabel lblActivitate = new JLabel("ID activitate:");
        JTextField tfActivitate = new JTextField();

        //id profesor
        JLabel lblProfesor = new JLabel("ID Profesor");
        JTextField tfProfesor = new JTextField();

        JButton btnConfirmare = new JButton("Adaugare");
        btnConfirmare.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tActivitate = tfActivitate.getText();
                String tProfesor = tfActivitate.getText();


                if(!tActivitate.isEmpty() && !tProfesor.isEmpty()){
                    try {
                        Connection connection = DatabaseConnection.getConnection();
                        String sql = "UPDATE activitate_grup SET id_prof = ? WHERE id_activitate = ?;";
                        PreparedStatement preparedStatement = connection.prepareStatement(sql);

                        preparedStatement.setString(1, tProfesor);
                        preparedStatement.setString(2, tActivitate);


                        preparedStatement.executeUpdate();
                        JOptionPane.showMessageDialog(null, "Profesor adaugat cu succes!");

                    }catch(NumberFormatException ex){
                        JOptionPane.showMessageDialog(adaugareFrame, "Date invalide");
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }

                }

            }
        });
        adaugareFrame.add(lblActivitate);
        adaugareFrame.add(tfActivitate);

        adaugareFrame.add(lblProfesor);
        adaugareFrame.add(tfProfesor);

        adaugareFrame.add(btnConfirmare);

        adaugareFrame.setLocationRelativeTo(null);
        adaugareFrame.setVisible(true);
    }

    public void windowCreareGrup(){
        JFrame adaugareFrame = new JFrame("Adaugare");
        adaugareFrame.setSize(200, 200);
        adaugareFrame.getDefaultCloseOperation();
        adaugareFrame.setLayout(new BoxLayout(adaugareFrame.getContentPane(), BoxLayout.Y_AXIS));

        //id grup
        JLabel lblGrup= new JLabel("ID grup:");
        JTextField tfGrup = new JTextField();

        //nume grup
        JLabel lblNume = new JLabel("Nume:");
        JTextField tfNume = new JTextField();

        //id curs
        JLabel lblCurs = new JLabel("ID Curs");
        JTextField tfCurs = new JTextField();

        JButton btnConfirmare = new JButton("Adaugare");
        btnConfirmare.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tGrup = tfGrup.getText();
                String tNume = tfNume.getText();
                String tCurs = tfCurs.getText();

                if(!tGrup.isEmpty() && !tCurs.isEmpty() && !tNume.isEmpty()){
                    try {
                        Connection connection = DatabaseConnection.getConnection();
                        String sql = "insert into grup_de_studi (id_grup, nume, id_curs) values (?, ?, ?);";
                        PreparedStatement preparedStatement = connection.prepareStatement(sql);

                        preparedStatement.setString(1, tGrup);
                        preparedStatement.setString(2, tNume);
                        preparedStatement.setString(3, tCurs);


                        preparedStatement.executeUpdate();
                        JOptionPane.showMessageDialog(null, "Grup adaugat cu succes!");

                    }catch(NumberFormatException ex){
                        JOptionPane.showMessageDialog(adaugareFrame, "Date invalide");
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }

                }

            }
        });
        adaugareFrame.add(lblGrup);
        adaugareFrame.add(tfGrup);

        adaugareFrame.add(lblNume);
        adaugareFrame.add(tfNume);

        adaugareFrame.add(lblCurs);
        adaugareFrame.add(tfCurs);

        adaugareFrame.add(btnConfirmare);

        adaugareFrame.setLocationRelativeTo(null);
        adaugareFrame.setVisible(true);
    }
    private void showAllUsers(int roleId) {
        String sql = "SELECT id_user, nume, prenume FROM utilizator WHERE id_rol = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, roleId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                StringBuilder usersInfo = new StringBuilder();

                usersInfo.append("Profesori:\n");


                while (resultSet.next()) {
                    usersInfo.append("ID: ").append(resultSet.getString("id_user"))
                            .append(" Nume: ").append(resultSet.getString("nume"))
                            .append(" Prenume: ").append(resultSet.getString("prenume"))
                            .append("\n");
                }

                JOptionPane.showMessageDialog(panel, usersInfo.toString(), "Lista utilizatori", JOptionPane.INFORMATION_MESSAGE);

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Eroare la afisarea utilizatorilor!", "Eroare", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panel, "Eroare la conectarea la baza de date!", "Eroare", JOptionPane.ERROR_MESSAGE);
        }
    }

}


