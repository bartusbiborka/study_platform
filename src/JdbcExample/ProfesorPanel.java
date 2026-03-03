package JdbcExample;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Locale;

public class ProfesorPanel {
    private JButton btnUsername;
    private JButton btnLogOut;
    private JButton btnCalendar;
    private JButton btnCatalog;
    private JToolBar menuBar;
    private JPanel panelOutput;
    private JPanel panel;
    private CardLayout cardLayout;
    private JButton btnDownloadCalendar;

    public ProfesorPanel (User user){

        btnUsername.setText(user.getNume() + " " + user.getPrenume());
        btnUsername.addActionListener(new ActionListener() {
               @Override
               public void actionPerformed(ActionEvent e) {
                   String sql = "SELECT u.id_user, u.CNP, u.nume, u.prenume, p.departament, a.judet, a.localitate, a.strada, a.numar FROM utilizator u JOIN profesor p on p.id_profesor = u.id_user JOIN adresa a on a.id_adresa = u.id_adrasa WHERE u.id_user = ?";
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
                               String departament = resultSet.getString("departament");
                               String judet = resultSet.getString("judet");
                               String localitate = resultSet.getString("localitate");
                               String strada = resultSet.getString("strada");
                               String numar = resultSet.getString("numar");

                               String mesaj = String.format("ID user: %s\nCNP: %s\nNume: %s\nPrenume: %s\nDepartament: %s\nAdresa: judetul %s, localitatea %s, %s, numarul %s\n", id, CNP, nume, prenume, departament, judet, localitate, strada,  numar);

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

        btnLogOut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });

        cardLayout = new CardLayout();
        panelOutput.setLayout(cardLayout);

        JPanel calendarPanel = new JPanel();
        JPanel catalogPanel = new JPanel();


        panelOutput.add(calendarPanel, "Calendar");
        panelOutput.add(catalogPanel, "Catalog");

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

                        String sql = "SELECT c.nume, a.tip, cal.data_inceput, cal.data_sfarsit " +
                                "          FROM activitate a " +
                                "  JOIN calendar cal ON a.id_desfasurare = cal.id_desfasurare " +
                                "   JOIN cursuri c ON a.id_curs = c.id_cursuri " +
                                "  JOIN prof_cursuri pc ON c.id_cursuri = pc.id_curs " +
                                " JOIN profesor p ON pc.id_profesor = p.id_profesor " +
                                " WHERE p.id_profesor = ? " +
                                "  AND cal.zi = ? AND a.tip IN ('curs', 'seminar', 'laborator') ORDER BY cal.data_inceput";

                        try (Connection connection = DatabaseConnection.getConnection()) {

                            String profId = user.getId();

                            PreparedStatement preparedStatement = connection.prepareStatement(sql);
                            preparedStatement.setString(1, profId);
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

                JButton btnAdaugare = new JButton("Adaugare activitate");
                btnAdaugare.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        windowAdaugaActivitate(user);
                    }
                });

                JButton btnExamen = new JButton("Calendarul examenelor");
                btnExamen.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        popUpExamen(user.getId());
                    }
                });

                JButton btnGrup = new JButton("Activitati cu grupuri de studiu");
                btnGrup.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        popUpGrupuri(user.getId());
                    }
                });

                JPanel butonPanel = new JPanel();
                butonPanel.setLayout(new BoxLayout(butonPanel, BoxLayout.Y_AXIS));
                butonPanel.add(Box.createVerticalStrut(10));

                calendarPanel.removeAll();
                calendarPanel.setLayout(new BorderLayout());
                calendarPanel.add(ziPanel, BorderLayout.NORTH);
                calendarPanel.add(scrollPane, BorderLayout.CENTER);

                butonPanel.add(btnAdaugare);
                butonPanel.add(btnExamen);
                butonPanel.add(btnGrup);
                butonPanel.add(btnDownloadCalendar);

                calendarPanel.add(butonPanel, BorderLayout.SOUTH);
                calendarPanel.revalidate();
                calendarPanel.repaint();

                cardLayout.show(panelOutput, "Calendar");
            }
        });

        btnCatalog.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sql = "\n" +
                        "select c.id_cursuri, c.nume from cursuri c join prof_cursuri pc on pc.id_curs = c.id_cursuri  where pc.id_profesor = ?";
                String profId = user.getId();

                JPanel cursuriPanel = new JPanel();
                cursuriPanel.setLayout(new BoxLayout(cursuriPanel, BoxLayout.Y_AXIS));

                try(Connection connection = DatabaseConnection.getConnection()){
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1, profId);

                    try(ResultSet resultSet = preparedStatement.executeQuery()){
                        boolean rez = false;
                        while(resultSet.next()){
                            rez=true;
                            String nume = resultSet.getString("nume");
                            String id = resultSet.getString("id_cursuri");

                            JButton cursButton = new JButton(nume);
                            cursButton.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    showCoursePanel(user, id);
                                }
                            });

                            cursuriPanel.add(cursButton);

                        }
                        if(!rez){
                            JLabel gr = new JLabel("Nu predai niciun curs!");
                            cursuriPanel.add(gr);
                        }
                    }
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }

                JScrollPane cursuriScroll = new JScrollPane(cursuriPanel);

                JPanel cursPanel = new JPanel();
                cursPanel.add(cursuriScroll, BorderLayout.CENTER);
                panelOutput.add(cursPanel, "Cursuri");

                cardLayout.show(panelOutput, "Cursuri");


            }
        });


    }

    public JPanel getPanel() {
        return panel;
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
    private String getZi(){
        LocalDate astazi = LocalDate.now();
        String zi = astazi.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("ro"));
        return zi;
    }

    public void windowAdaugaActivitate(User user){
        JFrame adaugareFrame = new JFrame("Adaugare");
        adaugareFrame.setSize(474, 450);
        adaugareFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        adaugareFrame.setLayout(new BoxLayout(adaugareFrame.getContentPane(), BoxLayout.Y_AXIS));

        //tip
        JLabel lblTip = new JLabel("Tip activitate (curs/seminar/laborator/examen/colocviu):");
        JTextField tfTip = new JTextField();

        //id curs
        JLabel lblIdCurs = new JLabel(" ID Curs:");
        JTextField tfIdCurs = new JTextField();

        //numar maxim participanti
        JLabel lblPart = new JLabel("Numar maxim participanti:");
        JTextField tfPart = new JTextField();

        //zi
        JLabel lblZi = new JLabel("Zi (luni/marti/miercuri/joi/vineri):");
        JTextField tfZi = new JTextField();

        //data desfasurare
        JLabel lblData = new JLabel("Data activitate (optional):");
        JTextField tfData = new JTextField();

        //ora inceput
        JLabel lblInceput = new JLabel("Ora inceput:");
        JTextField tfInceput = new JTextField();

        JButton btnConfirmare = new JButton("Adaugare");
        btnConfirmare.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tTip = tfTip.getText();
                String tIdCurs = tfIdCurs.getText().trim();
                String tPart = tfPart.getText();
                String tZi = tfZi.getText();
                String tData = tfData.getText().trim();


                if (tData.isEmpty()) {
                    tData = null;
                }else{
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate inputDate = LocalDate.parse(tData, formatter);

                        if (inputDate.isBefore(LocalDate.now())) {
                            JOptionPane.showMessageDialog(adaugareFrame, "Activitatea trebuie sa fie in viitor!", "Eroare", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                    } catch (DateTimeParseException ex) {
                        JOptionPane.showMessageDialog(adaugareFrame, "Formatul datei este incorect! Folositi formatul: yyyy-MM-dd", "Eroare", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                if(!tIdCurs.isEmpty()){
                    try{
                        String profId = user.getId();
                        String sql = "SELECT COUNT(*) FROM prof_cursuri WHERE id_profesor = ? AND id_curs = ?";

                        try(Connection connection = DatabaseConnection.getConnection()){
                            PreparedStatement preparedStatement = connection.prepareStatement(sql);
                            preparedStatement.setString(1, profId);
                            preparedStatement.setString(2, tIdCurs);

                            ResultSet resultSet = preparedStatement.executeQuery();

                            if(resultSet.next()){
                                if(resultSet.getInt(1) == 0){
                                    JOptionPane.showMessageDialog(adaugareFrame, "Nu predati acest curs!", "Eroare", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                            }

                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    } catch (RuntimeException ex) {
                        throw new RuntimeException(ex);
                    }

                }

                String tInceput = tfInceput.getText();


                if(!tTip.isEmpty() && !tIdCurs.isEmpty() && !tPart.isEmpty() && !tZi.isEmpty() && !tInceput.isEmpty()){
                    try {
                        Connection connection = DatabaseConnection.getConnection();
                        String sql = "{CALL InsertActivitateCalendar(?, ?, ?, ?, ?, ?)}";
                        PreparedStatement preparedStatement = connection.prepareStatement(sql);


                        preparedStatement.setString(1, tZi);
                        preparedStatement.setString(2, tInceput);
                        preparedStatement.setString(3, tData);
                        preparedStatement.setString(4, tIdCurs);
                        preparedStatement.setString(5, tTip);
                        preparedStatement.setString(6, tPart);

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
        adaugareFrame.add(lblTip);
        adaugareFrame.add(tfTip);

        adaugareFrame.add(lblIdCurs);
        adaugareFrame.add(tfIdCurs);

        adaugareFrame.add(lblPart);
        adaugareFrame.add(tfPart);

        adaugareFrame.add(lblZi);
        adaugareFrame.add(tfZi);

        adaugareFrame.add(lblInceput);
        adaugareFrame.add(tfInceput);

        adaugareFrame.add(lblData);
        adaugareFrame.add(tfData);

        adaugareFrame.add(btnConfirmare);

        adaugareFrame.setLocationRelativeTo(null);
        adaugareFrame.setVisible(true);
    }

    public void popUpExamen(String profId) {
        String sql = "SELECT c.nume, a.tip, cal.data_inceput, cal.data_sfarsit, cal.data " +
                "                                FROM activitate a " +
                "                                JOIN calendar cal ON a.id_desfasurare = cal.id_desfasurare " +
                "                                JOIN cursuri c ON a.id_curs = c.id_cursuri" +
                "                                JOIN prof_cursuri pc ON c.id_cursuri = pc.id_curs" +
                "                                JOIN profesor p ON pc.id_profesor = p.id_profesor" +
                "                                WHERE p.id_profesor = ? " +
                "                                AND a.tip IN ('examen', 'colocviu')" +
                " AND DATE(cal.data) > CURRENT_DATE " +
                "                                ORDER BY cal.data_inceput";


        DefaultListModel<String> examen = new DefaultListModel<>();
        JList<String> examenList = new JList<>(examen);


        try(Connection connection = DatabaseConnection.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, profId);

            try(ResultSet resultSet = preparedStatement.executeQuery()){
                while(resultSet.next()){
                    String tExamen = resultSet.getString("data") + " " + resultSet.getString("data_inceput") + "-" + resultSet.getString("data_sfarsit") + ": " + resultSet.getString("tip") + " " + resultSet.getString("nume") ;

                    examen.addElement(tExamen);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        JScrollPane scrollPane = new JScrollPane(examenList);
        JPanel sugestii = new JPanel(new BorderLayout());

        sugestii.add(scrollPane, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(null, sugestii, "Examene",JOptionPane.PLAIN_MESSAGE );

    }

    public void popUpGrupuri(String profId) {
        String sql ="select g.nume as grup, ag.nume, ag.data_desfasurare, ag.ora_inceput, ag.ora_sfarsit from activitate_grup ag JOIN grup_de_studi g ON ag.id_grup = g.id_grup WHERE ag.id_prof = ? AND DATE(ag.data_desfasurare) >= current_date()";


        DefaultListModel<String> grupuri = new DefaultListModel<>();
        JList<String> grupList = new JList<>(grupuri);


        try(Connection connection = DatabaseConnection.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, profId);

            try(ResultSet resultSet = preparedStatement.executeQuery()){
                while(resultSet.next()){
                    String tGrupuri = resultSet.getString("data_desfasurare") + " " + resultSet.getString("ora_inceput") + "-" + resultSet.getString("ora_sfarsit") + ": " + resultSet.getString("grup") + " " + resultSet.getString("nume") ;

                    grupuri.addElement(tGrupuri);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        JScrollPane scrollPane = new JScrollPane(grupList);
        JPanel activitati = new JPanel(new BorderLayout());

        activitati.add(scrollPane, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(null, activitati, "Activitati grupuri",JOptionPane.PLAIN_MESSAGE );

    }


    public void showCoursePanel(User user, String cursId) {
        JPanel coursePanel = new JPanel(new BorderLayout());
        String[] header = {"ID student", "Nume", "Prenume", "Nota curs", "Nota seminar", "Nota laborator", "Nota finala"};
        DefaultTableModel tableModel = new DefaultTableModel(header, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        loadTableData(tableModel, user.getId(), cursId);
        JPanel butonPanel = new JPanel();
        butonPanel.setLayout(new BoxLayout(butonPanel, BoxLayout.Y_AXIS));
        butonPanel.add(Box.createVerticalStrut(10));

        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        coursePanel.add(scrollPane, BorderLayout.CENTER);
        coursePanel.add(butonPanel, BorderLayout.SOUTH);

        JButton btnNote = new JButton("Notare student");
        btnNote.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                windowNotareStudent(cursId, user.getId(), table, tableModel);
                loadTableData(tableModel, user.getId(), cursId);
            }
        });


        JButton btnDownloadCatalog = new JButton("Descarcare catalog");
        btnDownloadCatalog.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Descarcare calendar");
                chooser.setSelectedFile(new java.io.File("catalog.csv"));

                int uSelect = chooser.showSaveDialog(panel);

                if (uSelect == JFileChooser.APPROVE_OPTION) {
                    java.io.File file = chooser.getSelectedFile();

                    try (FileWriter writer = new FileWriter(file)) {
                        TableModel model = table.getModel();

                        for (int i = 0; i < model.getColumnCount(); i++) {
                            writer.write(model.getColumnName(i) + (i < model.getColumnCount() - 1 ? "," : ""));
                        }
                        writer.write("\n");

                        for (int i = 0; i < model.getRowCount(); i++) {
                            for (int j = 0; j < model.getColumnCount(); j++) {
                                Object value = model.getValueAt(i, j);

                                writer.write((value != null ? value.toString() : "") + (j < model.getColumnCount() - 1 ? "," : ""));
                            }
                            writer.write("\n");
                        }

                        JOptionPane.showMessageDialog(panelOutput, "Catalog salvat cu succes! " + file.getAbsolutePath(), "Succes", JOptionPane.INFORMATION_MESSAGE);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(panelOutput, ex.getMessage(), "Eroare", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            }
        });


        JButton btnPondere = new JButton("Gestionare ponderi note");
        btnPondere.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                windowPondere(cursId, user.getId(), table, tableModel);
            }
        });



        butonPanel.add(btnNote);
        butonPanel.add(btnPondere);
        butonPanel.add(btnDownloadCatalog);

        panelOutput.add(coursePanel, "Curs");
        cardLayout.show(panelOutput, "Curs");

        panelOutput.revalidate();
        panelOutput.repaint();
    }

    public void windowNotareStudent(String cursId, String profId, JTable table, DefaultTableModel tableModel) {
        JFrame adaugareFrame = new JFrame("Notare student");
        adaugareFrame.setSize(300, 300);
        adaugareFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        adaugareFrame.setLayout(new BoxLayout(adaugareFrame.getContentPane(), BoxLayout.Y_AXIS));

        //id student
        JLabel lblIdStudent = new JLabel("ID Student");
        JTextField tfIdStudent = new JTextField();


        //nota curs
        JLabel lblCurs = new JLabel("Nota curs:");
        JTextField tfCurs = new JTextField();

        //nota seminar
        JLabel lblSeminar = new JLabel("Nota seminar:");
        JTextField tfSeminar = new JTextField();

        //nota laborator
        JLabel lblLaborator = new JLabel("Nota laborator");
        JTextField tfLaborator = new JTextField();

        JButton btnConfirmare = new JButton("Notare");
        btnConfirmare.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tIdStudent = tfIdStudent.getText().trim();
                String tCurs = tfCurs.getText().trim();
                String tSeminar = tfSeminar.getText().trim();
                String tLaborator = tfLaborator.getText().trim();

                if (tCurs.isEmpty()) {
                    tCurs = null;
                }

                if (tSeminar.isEmpty()) {
                    tSeminar = null;
                }

                if (tLaborator.isEmpty()) {
                    tLaborator = null;
                }

                if (!tIdStudent.isEmpty()) {
                    try {
                        Connection connection = DatabaseConnection.getConnection();
                        String sql = "update inrolare set nota_curs = ?, nota_seminar = ?, nota_lab = ? where id_curs= ? and id_prof = ? and id_student = ?";
                        PreparedStatement preparedStatement = connection.prepareStatement(sql);

                        preparedStatement.setString(1, tCurs);
                        preparedStatement.setString(2, tSeminar);
                        preparedStatement.setString(3, tLaborator);
                        preparedStatement.setString(4, cursId);
                        preparedStatement.setString(5, profId);
                        preparedStatement.setString(6, tIdStudent);

                        int rowsAffected = preparedStatement.executeUpdate();
                        if (rowsAffected > 0) {
                            JOptionPane.showMessageDialog(null, "Note adaugate cu succes!");

                            loadTableData(tableModel, profId, cursId);

                            table.revalidate();
                            table.repaint();
                        } else {
                            JOptionPane.showMessageDialog(null, "Nu s-a putut actualiza nota. Verificați datele introduse.");
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(adaugareFrame, "Date invalide");
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });


        adaugareFrame.add(lblIdStudent);
        adaugareFrame.add(tfIdStudent);

        adaugareFrame.add(lblCurs);
        adaugareFrame.add(tfCurs);

        adaugareFrame.add(lblSeminar);
        adaugareFrame.add(tfSeminar);

        adaugareFrame.add(lblLaborator);
        adaugareFrame.add(tfLaborator);

        adaugareFrame.add(btnConfirmare);

        adaugareFrame.setLocationRelativeTo(null);
        adaugareFrame.setVisible(true);
    }

    private void loadTableData(DefaultTableModel tableModel, String profId, String cursId) {
        tableModel.setRowCount(0);

        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "select u.id_user, u.nume, u.prenume, i.nota_curs, i.nota_seminar, i.nota_lab, i.nota_finala from inrolare i join utilizator u on u.id_user = i.id_student where i.id_prof = ? and i.id_curs = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, profId);
            preparedStatement.setString(2, cursId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String idStudent = resultSet.getString("id_user");
                    String nume = resultSet.getString("nume");
                    String prenume = resultSet.getString("prenume");
                    String notaCurs = resultSet.getString("nota_curs");
                    String notaSeminar = resultSet.getString("nota_seminar");
                    String notaLab = resultSet.getString("nota_lab");
                    String notaFinala = resultSet.getString("nota_finala");

                    tableModel.addRow(new Object[]{idStudent, nume, prenume, notaCurs, notaSeminar, notaLab, notaFinala});
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    public void windowPondere(String cursId, String profId, JTable table, DefaultTableModel tableModel) {

        JFrame adaugareFrame = new JFrame("Pondere note");
        adaugareFrame.setSize(300, 300);
        adaugareFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        adaugareFrame.setLayout(new BoxLayout(adaugareFrame.getContentPane(), BoxLayout.Y_AXIS));

        //nota curs
        JLabel lblCurs = new JLabel("Pondere nota curs:");
        JTextField tfCurs = new JTextField();

        //nota seminar
        JLabel lblSeminar = new JLabel("Pondere nota seminar:");
        JTextField tfSeminar = new JTextField();

        //nota laborator
        JLabel lblLaborator = new JLabel("Pondere nota laborator");
        JTextField tfLaborator = new JTextField();

        JButton btnConfirmare = new JButton("Notare");
        btnConfirmare.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tCurs = tfCurs.getText().trim();
                String tSeminar = tfSeminar.getText().trim();
                String tLaborator = tfLaborator.getText().trim();

                int pondereCurs = tCurs.isEmpty() ? 0 : Integer.parseInt(tCurs);
                int pondereSeminar = tSeminar.isEmpty() ? 0 : Integer.parseInt(tSeminar);
                int pondereLab = tLaborator.isEmpty() ? 0 : Integer.parseInt(tLaborator);


                    try {
                        Connection connection = DatabaseConnection.getConnection();
                        String sql = "{call calculare_note(?, ?, ?, ?)}";
                        PreparedStatement preparedStatement = connection.prepareStatement(sql);

                        preparedStatement.setString(1, cursId);

                        preparedStatement.setInt(2, pondereCurs);
                        preparedStatement.setInt(3, pondereSeminar);
                        preparedStatement.setInt(4, pondereLab);

                        int rowsAffected = preparedStatement.executeUpdate();
                        if (rowsAffected > 0) {
                            JOptionPane.showMessageDialog(null, "Gestionare ponderi cu succes!");

                            loadTableData(tableModel, profId, cursId);

                            table.revalidate();
                            table.repaint();
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(adaugareFrame, "Date invalide");
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }

            }
        });

        adaugareFrame.add(lblCurs);
        adaugareFrame.add(tfCurs);

        adaugareFrame.add(lblSeminar);
        adaugareFrame.add(tfSeminar);

        adaugareFrame.add(lblLaborator);
        adaugareFrame.add(tfLaborator);

        adaugareFrame.add(btnConfirmare);

        adaugareFrame.setLocationRelativeTo(null);
        adaugareFrame.setVisible(true);
    }


    }
