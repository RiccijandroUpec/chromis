package uk.chromis.pos.invoice.services;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Servicio ligero para enviar correos electrónicos por SMTP mediante Sockets nativos.
 * No requiere la librería externa javax.mail.
 * Soporta SMTP seguro (SSL/TLS en puerto 465) y autenticación LOGIN.
 */
public class EmailSenderService {
    
    private String smtpHost;
    private int smtpPort;
    private String smtpUser;
    private String smtpPassword;
    private boolean useSSL;
    
    public EmailSenderService() {
        // Valores por defecto (se pueden sobreescribir desde properties)
        this.smtpHost = "smtp.gmail.com";
        this.smtpPort = 465;
        this.smtpUser = "";
        this.smtpPassword = "";
        this.useSSL = true;
        
        loadConfiguration();
    }
    
    /**
     * Carga la configuración del archivo properties si existe
     */
    private void loadConfiguration() {
        loadConfigurationForUser("default");
    }
    
    /**
     * Carga la configuración del archivo properties para un usuario específico si existe,
     * o cae de regreso a la configuración por defecto/global.
     */
    public void loadConfigurationForUser(String userId) {
        try {
            java.util.Properties props = new java.util.Properties();
            java.io.File f = new java.io.File("chromisposconfig.properties");
            if (f.exists()) {
                props.load(new java.io.FileInputStream(f));
                
                // Host
                String userHost = props.getProperty("invoice.mail.host." + userId);
                this.smtpHost = (userHost != null && !userHost.isEmpty()) ? userHost : props.getProperty("invoice.mail.host", "smtp.gmail.com");
                
                // Port
                String userPort = props.getProperty("invoice.mail.port." + userId);
                String defaultPort = props.getProperty("invoice.mail.port", "465");
                this.smtpPort = Integer.parseInt((userPort != null && !userPort.isEmpty()) ? userPort : defaultPort);
                
                // SSL
                String userSSL = props.getProperty("invoice.mail.ssl." + userId);
                this.useSSL = Boolean.parseBoolean((userSSL != null && !userSSL.isEmpty()) ? userSSL : props.getProperty("invoice.mail.ssl", "true"));
                
                // User
                String userUser = props.getProperty("invoice.mail.user." + userId);
                this.smtpUser = (userUser != null && !userUser.isEmpty()) ? userUser : props.getProperty("invoice.mail.user", "");
                
                // Password
                String userPass = props.getProperty("invoice.mail.password." + userId);
                String pass = (userPass != null && !userPass.isEmpty()) ? userPass : props.getProperty("invoice.mail.password", "");
                
                if (pass != null && !pass.isEmpty()) {
                    try {
                        this.smtpPassword = uk.chromis.pos.invoice.utils.CipherUtil.decrypt(pass);
                    } catch (Exception ex) {
                        this.smtpPassword = pass; // Si falla la desencripción, usar texto plano
                    }
                } else {
                    this.smtpPassword = "";
                }
            }
        } catch (Exception e) {
            System.err.println("Advertencia: No se pudo cargar configuración de correo para usuario " + userId + ": " + e.getMessage());
        }
    }
    
    /**
     * Envía un correo con archivos adjuntos de forma asíncrona
     */
    public void sendEmailAsync(final String toEmail, final String subject, final String bodyText, final File... attachments) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sendEmail(toEmail, subject, bodyText, attachments);
                    System.out.println("✓ Correo enviado exitosamente a " + toEmail);
                } catch (Exception e) {
                    System.err.println("✗ Error enviando correo: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    /**
     * Envía un correo electrónico de forma síncrona
     */
    public void sendEmail(String toEmail, String subject, String bodyText, File... attachments) throws Exception {
        if (smtpUser == null || smtpUser.isEmpty()) {
            System.out.println("Servicio de correo SMTP no configurado. Envío omitido.");
            return;
        }
        
        Socket socket;
        if (useSSL) {
            SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = sf.createSocket(smtpHost, smtpPort);
        } else {
            socket = new Socket(smtpHost, smtpPort);
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {
            
            readResponse(reader, "220");
            
            sendCmd(writer, reader, "EHLO " + smtpHost, "250");
            
            // Autenticación LOGIN
            sendCmd(writer, reader, "AUTH LOGIN", "334");
            sendCmd(writer, reader, Base64.getEncoder().encodeToString(smtpUser.getBytes(StandardCharsets.UTF_8)), "334");
            sendCmd(writer, reader, Base64.getEncoder().encodeToString(smtpPassword.getBytes(StandardCharsets.UTF_8)), "235");
            
            // Remitente y Destinatario
            sendCmd(writer, reader, "MAIL FROM:<" + smtpUser + ">", "250");
            sendCmd(writer, reader, "RCPT TO:<" + toEmail + ">", "250");
            
            // Iniciar DATA
            sendCmd(writer, reader, "DATA", "354");
            
            // Generar boundary único para multipart
            String boundary = "====Boundary_Chromis_POS_Ecuador_123456789====";
            
            // Escribir cabeceras del correo
            writer.println("From: " + smtpUser);
            writer.println("To: " + toEmail);
            writer.println("Subject: " + subject);
            writer.println("MIME-Version: 1.0");
            writer.println("Content-Type: multipart/mixed; boundary=\"" + boundary + "\"");
            writer.println();
            
            // Escribir cuerpo del correo
            writer.println("--" + boundary);
            writer.println("Content-Type: text/html; charset=utf-8");
            writer.println("Content-Transfer-Encoding: 7bit");
            writer.println();
            writer.println(bodyText);
            writer.println();
            
            // Escribir adjuntos si existen
            if (attachments != null) {
                for (File file : attachments) {
                    if (file == null || !file.exists()) continue;
                    
                    writer.println("--" + boundary);
                    String mimeType = "application/xml";
                    if (file.getName().endsWith(".html")) {
                        mimeType = "text/html";
                    } else if (file.getName().endsWith(".pdf")) {
                        mimeType = "application/pdf";
                    }
                    writer.println("Content-Type: " + mimeType + "; name=\"" + file.getName() + "\"");
                    writer.println("Content-Transfer-Encoding: base64");
                    writer.println("Content-Disposition: attachment; filename=\"" + file.getName() + "\"");
                    writer.println();
                    
                    // Escribir archivo codificado en Base64
                    try (FileInputStream fis = new FileInputStream(file)) {
                        byte[] buffer = new byte[3072]; // múltiplo de 3 para base64
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            byte[] chunk = new byte[bytesRead];
                            System.arraycopy(buffer, 0, chunk, 0, bytesRead);
                            writer.println(Base64.getEncoder().encodeToString(chunk));
                        }
                    }
                    writer.println();
                }
            }
            
            // Fin del correo
            writer.println("--" + boundary + "--");
            writer.println(".");
            readResponse(reader, "250");
            
            // Salir
            sendCmd(writer, reader, "QUIT", "221");
        } finally {
            socket.close();
        }
    }
    
    private void sendCmd(PrintWriter writer, BufferedReader reader, String cmd, String expectedCode) throws Exception {
        writer.println(cmd);
        readResponse(reader, expectedCode);
    }
    
    private void readResponse(BufferedReader reader, String expectedCode) throws Exception {
        String line = reader.readLine();
        if (line == null) {
            throw new IOException("Conexión cerrada por el servidor SMTP.");
        }
        System.out.println("SMTP: " + line);
        
        // SMTP puede enviar múltiples líneas de bienvenida o respuesta (e.g. 220-linea1 \n 220 linea2)
        while (line.startsWith(expectedCode + "-")) {
            line = reader.readLine();
            System.out.println("SMTP: " + line);
        }
        
        if (!line.startsWith(expectedCode)) {
            throw new IOException("Error en protocolo SMTP. Se esperaba " + expectedCode + " pero se recibió: " + line);
        }
    }
    
    // Getters y Setters
    public String getSmtpHost() { return smtpHost; }
    public void setSmtpHost(String smtpHost) { this.smtpHost = smtpHost; }
    public int getSmtpPort() { return smtpPort; }
    public void setSmtpPort(int smtpPort) { this.smtpPort = smtpPort; }
    public String getSmtpUser() { return smtpUser; }
    public void setSmtpUser(String smtpUser) { this.smtpUser = smtpUser; }
    public String getSmtpPassword() { return smtpPassword; }
    public void setSmtpPassword(String smtpPassword) { this.smtpPassword = smtpPassword; }
    public boolean isUseSSL() { return useSSL; }
    public void setUseSSL(boolean useSSL) { this.useSSL = useSSL; }
}
