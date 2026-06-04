/*
**    Chromis POS - Respaldo y Restauración de Base de Datos
**
**    Autor: Riccijandro | github.com/riccijandro
**    Contacto: richardrodriguez271@gmail.com
*/

package uk.chromis.pos.forms;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class BackupRestoreDialog extends JDialog {
    
    private JTextArea txtLog;
    private JButton btnBackup, btnRestore, btnSchedule, btnClose;
    private JLabel lblLastBackup, lblDbInfo;
    private JCheckBox chkSchedule;
    private JSpinner spinnerHour;
    private Timer backupTimer;
    private String backupDir;
    
    public BackupRestoreDialog(JFrame parent) {
        super(parent, "💾 Respaldo y Restauración de Base de Datos", true);
        backupDir = "backups";
        initComponents();
        pack();
        setLocationRelativeTo(parent);
        setMinimumSize(new Dimension(600, 500));
        loadBackupInfo();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Panel superior - Info de BD
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        
        lblDbInfo = new JLabel("📀 Base de Datos: cargando...");
        lblDbInfo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        topPanel.add(lblDbInfo, gbc);
        
        gbc.gridy = 1;
        lblLastBackup = new JLabel("Último respaldo: No hay respaldos");
        lblLastBackup.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        topPanel.add(lblLastBackup, gbc);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Panel central - Log
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "📋 Registro de Actividad"));
        
        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setFont(new Font("Consolas", Font.PLAIN, 11));
        txtLog.setBackground(new Color(30, 30, 30));
        txtLog.setForeground(new Color(0, 255, 0));
        JScrollPane scroll = new JScrollPane(txtLog);
        scroll.setPreferredSize(new Dimension(550, 250));
        centerPanel.add(scroll);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // Panel inferior - Botones
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        btnBackup = new JButton("💾 Hacer Respaldo Ahora");
        btnBackup.setBackground(new Color(79, 70, 229));
        btnBackup.setForeground(Color.WHITE);
        btnBackup.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBackup.addActionListener(e -> doBackup());
        gbc.gridx = 0; gbc.gridy = 0;
        bottomPanel.add(btnBackup, gbc);
        
        btnRestore = new JButton("📂 Restaurar Respaldo");
        btnRestore.setBackground(new Color(245, 158, 11));
        btnRestore.setForeground(Color.WHITE);
        btnRestore.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRestore.addActionListener(e -> doRestore());
        gbc.gridx = 1;
        bottomPanel.add(btnRestore, gbc);
        
        // Panel de programación
        JPanel schedulePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        schedulePanel.setBorder(BorderFactory.createTitledBorder("🕐 Respaldo Automático"));
        
        chkSchedule = new JCheckBox("Activar respaldo automático diario a las:");
        spinnerHour = new JSpinner(new SpinnerNumberModel(2, 0, 23, 1));
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinnerHour, "HH:00");
        spinnerHour.setEditor(editor);
        
        btnSchedule = new JButton("Guardar Programación");
        btnSchedule.addActionListener(e -> toggleSchedule());
        
        schedulePanel.add(chkSchedule);
        schedulePanel.add(spinnerHour);
        schedulePanel.add(btnSchedule);
        
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        bottomPanel.add(schedulePanel, gbc);
        
        btnClose = new JButton("Cerrar");
        btnClose.addActionListener(e -> dispose());
        gbc.gridy = 2;
        bottomPanel.add(btnClose, gbc);
        
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Cargar configuración de programación
        loadScheduleConfig();
    }
    
    private void loadBackupInfo() {
        try {
            Properties props = new Properties();
            File configFile = new File("chromisposconfig.properties");
            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    props.load(fis);
                }
                String dbName = props.getProperty("database.name", "no configurada");
                String dbHost = props.getProperty("database.server", "localhost");
                lblDbInfo.setText("📀 Base de Datos: " + dbName + " @" + dbHost);
            }
        } catch (IOException e) {
            lblDbInfo.setText("📀 Base de Datos: Error al leer configuración");
        }
        
        // Buscar último backup
        File backupFolder = new File(backupDir);
        if (backupFolder.exists()) {
            File[] backups = backupFolder.listFiles((dir, name) -> name.endsWith(".sql"));
            if (backups != null && backups.length > 0) {
                File last = backups[backups.length - 1];
                lblLastBackup.setText("Último respaldo: " + last.getName() + 
                    " (" + new Date(last.lastModified()) + ")");
            }
        }
    }
    
    private void loadScheduleConfig() {
        try {
            Properties props = new Properties();
            File configFile = new File("chromisposconfig.properties");
            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    props.load(fis);
                }
                String schedule = props.getProperty("backup.schedule.enabled", "false");
                String hour = props.getProperty("backup.schedule.hour", "2");
                chkSchedule.setSelected("true".equals(schedule));
                spinnerHour.setValue(Integer.parseInt(hour));
            }
        } catch (IOException e) {
            // Usar valores por defecto
        }
    }
    
    private void doBackup() {
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() {
                try {
                    publish("🔄 Iniciando respaldo de base de datos...\n");
                    
                    Properties props = new Properties();
                    try (FileInputStream fis = new FileInputStream("chromisposconfig.properties")) {
                        props.load(fis);
                    }
                    
                    String dbHost = props.getProperty("database.server", "localhost");
                    String dbPort = props.getProperty("database.port", "3306");
                    String dbName = props.getProperty("database.name", "chromispos_ec");
                    String dbUser = props.getProperty("database.user", "root");
                    String dbPass = props.getProperty("database.password", "");
                    
                    // Crear directorio de backups si no existe
                    Files.createDirectories(Paths.get(backupDir));
                    
                    // Nombre del archivo
                    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String backupFile = backupDir + "/chromispos_backup_" + timestamp + ".sql";
                    
                    publish("📁 Archivo: " + backupFile + "\n");
                    
                    // Construir comando mysqldump
                    ProcessBuilder pb = new ProcessBuilder(
                        "mysqldump",
                        "-h" + dbHost,
                        "-P" + dbPort,
                        "-u" + dbUser,
                        "-p" + dbPass,
                        "--routines",
                        "--triggers",
                        "--add-drop-database",
                        "--databases", dbName,
                        "--result-file=" + backupFile
                    );
                    
                    publish("⚙ Ejecutando mysqldump...\n");
                    
                    Process process = pb.start();
                    int exitCode = process.waitFor();
                    
                    if (exitCode == 0) {
                        File file = new File(backupFile);
                        String size = String.format("%.2f MB", file.length() / (1024.0 * 1024.0));
                        publish("✅ Respaldo completado exitosamente!\n");
                        publish("📦 Tamaño: " + size + "\n");
                        publish("📍 Ubicación: " + backupFile + "\n");
                        lblLastBackup.setText("Último respaldo: " + file.getName());
                    } else {
                        publish("❌ Error al ejecutar mysqldump (código: " + exitCode + ")\n");
                        publish("💡 Asegúrese de que mysqldump esté instalado y en el PATH\n");
                    }
                    
                } catch (Exception ex) {
                    publish("❌ Error: " + ex.getMessage() + "\n");
                    ex.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void process(java.util.List<String> chunks) {
                for (String text : chunks) {
                    txtLog.append(text);
                    txtLog.setCaretPosition(txtLog.getDocument().getLength());
                }
            }
        };
        worker.execute();
    }
    
    private void doRestore() {
        JFileChooser chooser = new JFileChooser(backupDir);
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Archivos SQL (.sql)", "sql"));
        chooser.setDialogTitle("Seleccionar archivo de respaldo para restaurar");
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            
            int confirm = JOptionPane.showConfirmDialog(this,
                "⚠ ¿Está seguro de restaurar este respaldo?\n\n" +
                "Archivo: " + file.getName() + "\n" +
                "Tamaño: " + String.format("%.2f MB", file.length() / (1024.0 * 1024.0)) + "\n\n" +
                "¡Se sobrescribirán TODOS los datos actuales!",
                "Confirmar Restauración", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                restoreFromFile(file);
            }
        }
    }
    
    private void restoreFromFile(File file) {
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() {
                try {
                    publish("🔄 Iniciando restauración desde: " + file.getName() + "\n");
                    
                    Properties props = new Properties();
                    try (FileInputStream fis = new FileInputStream("chromisposconfig.properties")) {
                        props.load(fis);
                    }
                    
                    String dbHost = props.getProperty("database.server", "localhost");
                    String dbPort = props.getProperty("database.port", "3306");
                    String dbName = props.getProperty("database.name", "chromispos_ec");
                    String dbUser = props.getProperty("database.user", "root");
                    String dbPass = props.getProperty("database.password", "");
                    
                    publish("⚙ Ejecutando restauración...\n");
                    
                    ProcessBuilder pb = new ProcessBuilder(
                        "mysql",
                        "-h" + dbHost,
                        "-P" + dbPort,
                        "-u" + dbUser,
                        "-p" + dbPass,
                        dbName
                    );
                    
                    pb.redirectInput(file);
                    Process process = pb.start();
                    
                    BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()));
                    
                    int exitCode = process.waitFor();
                    
                    if (exitCode == 0) {
                        publish("✅ Restauración completada exitosamente!\n");
                    } else {
                        publish("❌ Error durante la restauración\n");
                        String line;
                        while ((line = errorReader.readLine()) != null) {
                            publish("   " + line + "\n");
                        }
                    }
                    
                } catch (Exception ex) {
                    publish("❌ Error: " + ex.getMessage() + "\n");
                }
                return null;
            }
            
            @Override
            protected void process(java.util.List<String> chunks) {
                for (String text : chunks) {
                    txtLog.append(text);
                    txtLog.setCaretPosition(txtLog.getDocument().getLength());
                }
            }
        };
        worker.execute();
    }
    
    private void toggleSchedule() {
        try {
            Properties props = new Properties();
            File configFile = new File("chromisposconfig.properties");
            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    props.load(fis);
                }
            }
            
            props.setProperty("backup.schedule.enabled", 
                chkSchedule.isSelected() ? "true" : "false");
            props.setProperty("backup.schedule.hour", 
                spinnerHour.getValue().toString());
            
            try (FileOutputStream fos = new FileOutputStream(configFile)) {
                props.store(fos, "ChromisPOS Ecuador Configuration");
            }
            
            // Iniciar/detener timer
            if (chkSchedule.isSelected()) {
                int hour = (int) spinnerHour.getValue();
                startScheduledBackup(hour);
                JOptionPane.showMessageDialog(this,
                    "✅ Respaldo automático activado para las " + hour + ":00 horas",
                    "Programación Guardada", JOptionPane.INFORMATION_MESSAGE);
            } else {
                if (backupTimer != null) {
                    backupTimer.stop();
                }
                JOptionPane.showMessageDialog(this,
                    "⏹ Respaldo automático desactivado",
                    "Programación Guardada", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "❌ Error al guardar configuración: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void startScheduledBackup(int hour) {
        if (backupTimer != null) {
            backupTimer.stop();
        }
        
        // Timer que revisa cada hora si debe hacer backup
        backupTimer = new Timer(3600000, e -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            int currentHour = cal.get(java.util.Calendar.HOUR_OF_DAY);
            if (currentHour == hour) {
                doBackup();
            }
        });
        backupTimer.start();
    }
}
