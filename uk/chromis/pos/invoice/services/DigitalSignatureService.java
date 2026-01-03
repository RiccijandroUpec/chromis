package uk.chromis.pos.invoice.services;

import uk.chromis.pos.invoice.models.ElectronicInvoice;
import uk.chromis.pos.invoice.models.InvoiceStatus;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;

/**
 * Servicio para firmar digitalmente facturas
 * Requiere certificado digital válido en Ecuador (PKCS#7)
 * Utiliza la API estándar de Java para firma digital
 */
public class DigitalSignatureService {
    
    private String certificatePath;
    private String certificatePassword;
    private KeyStore keyStore;
    
    public DigitalSignatureService(String certificatePath, String certificatePassword) {
        this.certificatePath = certificatePath;
        this.certificatePassword = certificatePassword;
        this.keyStore = null;
    }
    
    /**
     * Carga el certificado digital desde archivo PFX/PKCS12
     */
    public void loadCertificate() throws Exception {
        if (certificatePath == null || certificatePath.isEmpty()) {
            throw new IllegalArgumentException("Certificate path cannot be null");
        }
        
        File certFile = new File(certificatePath);
        if (!certFile.exists()) {
            throw new FileNotFoundException("Certificate file not found: " + certificatePath);
        }
        
        try (FileInputStream fis = new FileInputStream(certFile)) {
            // Cargar KeyStore PKCS12 (formato de certificados Ecuador)
            keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(fis, certificatePassword != null ? certificatePassword.toCharArray() : new char[0]);
        }
    }
    
    /**
     * Firma digitalmente el XML de la factura
     */
    public void signInvoice(ElectronicInvoice invoice) throws Exception {
        if (invoice.getXmlContent() == null || invoice.getXmlContent().isEmpty()) {
            throw new IllegalArgumentException("XML content must be generated first");
        }
        
        if (keyStore == null) {
            loadCertificate();
        }
        
        // Obtener alias (generalmente el primero)
        String alias = keyStore.aliases().nextElement();
        
        // Obtener la clave privada
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, 
            certificatePassword != null ? certificatePassword.toCharArray() : new char[0]);
        
        if (privateKey == null) {
            throw new IllegalStateException("Private key not found in certificate");
        }
        
        // Obtener el certificado
        Certificate certificate = keyStore.getCertificate(alias);
        if (certificate == null) {
            throw new IllegalStateException("Certificate not found");
        }
        
        // Generar firma digital
        String signature = generateSignature(invoice.getXmlContent(), privateKey);
        
        // Crear XML firmado (estructura simplificada)
        String signedXml = createSignedXmlContent(invoice.getXmlContent(), signature, certificate);
        
        invoice.setSignedXmlContent(signedXml);
        invoice.setStatus(InvoiceStatus.SIGNED);
    }
    
    /**
     * Genera la firma digital del contenido XML
     */
    private String generateSignature(String xmlContent, PrivateKey privateKey) throws Exception {
        // Usar SHA256 para firmar
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(xmlContent.getBytes("UTF-8"));
        
        byte[] signatureBytes = signature.sign();
        
        // Codificar a Base64
        return java.util.Base64.getEncoder().encodeToString(signatureBytes);
    }
    
    /**
     * Crea el XML firmado agregando la firma digital
     */
    private String createSignedXmlContent(String xmlContent, String signature, Certificate certificate) throws Exception {
        // Extraer el certificado codificado en Base64
        byte[] certBytes = certificate.getEncoded();
        String certBase64 = java.util.Base64.getEncoder().encodeToString(certBytes);
        
        // Estructura básica de XML con firma (nota: SRI requiere formato específico)
        StringBuilder signedXml = new StringBuilder();
        signedXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        signedXml.append("<factura>\n");
        signedXml.append("  <contenido>\n");
        signedXml.append(xmlContent);
        signedXml.append("  </contenido>\n");
        signedXml.append("  <firma>\n");
        signedXml.append("    <signatureValue>").append(signature).append("</signatureValue>\n");
        signedXml.append("    <certificate>").append(certBase64).append("</certificate>\n");
        signedXml.append("  </firma>\n");
        signedXml.append("</factura>\n");
        
        return signedXml.toString();
    }
    
    /**
     * Valida la firma digital de un documento
     */
    public boolean validateSignature(ElectronicInvoice invoice) throws Exception {
        if (invoice.getSignedXmlContent() == null || invoice.getSignedXmlContent().isEmpty()) {
            return false;
        }
        
        if (keyStore == null) {
            loadCertificate();
        }
        
        // Obtener alias y certificado
        String alias = keyStore.aliases().nextElement();
        Certificate certificate = keyStore.getCertificate(alias);
        
        if (certificate == null) {
            return false;
        }
        
        // Extraer contenido y firma del XML
        String originalContent = extractOriginalContent(invoice.getSignedXmlContent());
        String signatureValue = extractSignatureValue(invoice.getSignedXmlContent());
        
        // Decodificar firma de Base64
        byte[] signatureBytes = java.util.Base64.getDecoder().decode(signatureValue);
        
        // Verificar firma
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(certificate.getPublicKey());
        signature.update(originalContent.getBytes("UTF-8"));
        
        return signature.verify(signatureBytes);
    }
    
    /**
     * Extrae el contenido original del XML firmado
     */
    private String extractOriginalContent(String signedXml) {
        int startIdx = signedXml.indexOf("<contenido>") + "<contenido>".length();
        int endIdx = signedXml.indexOf("</contenido>");
        return signedXml.substring(startIdx, endIdx);
    }
    
    /**
     * Extrae el valor de firma del XML firmado
     */
    private String extractSignatureValue(String signedXml) {
        int startIdx = signedXml.indexOf("<signatureValue>") + "<signatureValue>".length();
        int endIdx = signedXml.indexOf("</signatureValue>");
        return signedXml.substring(startIdx, endIdx);
    }
    
    /**
     * Obtiene la ruta del certificado
     */
    public String getCertificatePath() {
        return certificatePath;
    }
    
    /**
     * Establece la ruta del certificado
     */
    public void setCertificatePath(String certificatePath) {
        this.certificatePath = certificatePath;
    }
    
    /**
     * Obtiene la contraseña del certificado
     */
    public String getCertificatePassword() {
        return certificatePassword;
    }
    
    /**
     * Establece la contraseña del certificado
     */
    public void setCertificatePassword(String certificatePassword) {
        this.certificatePassword = certificatePassword;
    }
    
    /**
     * Verifica si hay un certificado cargado
     */
    public boolean isCertificateLoaded() {
        return keyStore != null;
    }
}
