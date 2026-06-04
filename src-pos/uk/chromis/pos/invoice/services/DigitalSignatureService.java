package uk.chromis.pos.invoice.services;

import uk.chromis.pos.invoice.models.ElectronicInvoice;
import uk.chromis.pos.invoice.models.InvoiceStatus;

import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.xml.crypto.*;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.*;
import javax.xml.crypto.dsig.spec.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Servicio para firmar digitalmente facturas
 * Genera una firma electrónica estándar XAdES-BES (requerida por el SRI en Ecuador)
 * utilizando exclusivamente las APIs nativas de Java (JSR 105).
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
            throw new IllegalArgumentException("Ruta de certificado no especificada.");
        }
        
        File certFile = new File(certificatePath);
        if (!certFile.exists()) {
            throw new FileNotFoundException("Archivo de certificado no encontrado: " + certificatePath);
        }
        
        try (FileInputStream fis = new FileInputStream(certFile)) {
            keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(fis, certificatePassword != null ? certificatePassword.toCharArray() : new char[0]);
        }
    }
    
    /**
     * Firma digitalmente el XML de la factura en formato XAdES-BES
     */
    public void signInvoice(ElectronicInvoice invoice) throws Exception {
        if (invoice.getXmlContent() == null || invoice.getXmlContent().isEmpty()) {
            throw new IllegalArgumentException("El contenido XML debe ser generado primero.");
        }
        
        if (keyStore == null) {
            loadCertificate();
        }
        
        // Obtener alias y clave privada
        String alias = keyStore.aliases().nextElement();
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, 
            certificatePassword != null ? certificatePassword.toCharArray() : new char[0]);
        
        if (privateKey == null) {
            throw new IllegalStateException("Clave privada no encontrada en el certificado.");
        }
        
        // Obtener certificado X509
        X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
        if (certificate == null) {
            throw new IllegalStateException("Certificado no encontrado en el archivo cargado.");
        }
        
        // Parsear el XML a un Documento DOM
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(
            new ByteArrayInputStream(invoice.getXmlContent().getBytes("UTF-8"))
        );
        
        // Configurar el contexto de firma
        DOMSignContext dsc = new DOMSignContext(privateKey, doc.getDocumentElement());
        
        // Inicializar factoría de firmas XML de Java
        XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
        
        // Crear DigestMethod (SHA-1 es el estándar exigido por XAdES-BES en SRI)
        DigestMethod digestMethod = factory.newDigestMethod(DigestMethod.SHA1, null);
        
        // Transformaciones: Enveloped y Canonicalización
        Transform envelopedTransform = factory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);
        Transform c14nTransform = factory.newTransform("http://www.w3.org/TR/2001/REC-xml-c14n-20010315", (TransformParameterSpec) null);
        List<Transform> transformList = Arrays.asList(envelopedTransform, c14nTransform);
        
        // Crear identificadores únicos para la firma y propiedades XAdES
        String signatureId = "Signature-" + UUID.randomUUID().toString();
        String signedPropertiesId = "SignedProperties-" + UUID.randomUUID().toString();
        
        // 1. Referencia al propio documento completo (URI="")
        Reference docRef = factory.newReference("", digestMethod, transformList, null, null);
        
        // 2. Referencia a las propiedades XAdES (#SignedProperties)
        Reference xadesRef = factory.newReference(
            "#" + signedPropertiesId,
            digestMethod,
            Collections.emptyList(),
            "http://uri.etsi.org/01903#SignedProperties",
            null
        );
        
        // 3. Crear estructura DOM de las propiedades XAdES-BES
        String xadesNamespace = "http://uri.etsi.org/01903/v1.3.2#";
        Element qualifyingProperties = doc.createElementNS(xadesNamespace, "xades:QualifyingProperties");
        qualifyingProperties.setAttribute("Target", "#" + signatureId);
        
        Element signedProperties = doc.createElementNS(xadesNamespace, "xades:SignedProperties");
        signedProperties.setAttribute("Id", signedPropertiesId);
        qualifyingProperties.appendChild(signedProperties);
        
        // Registrar ID en el DOM para que el motor de firmas pueda ubicar la referencia
        signedProperties.setIdAttribute("Id", true);
        
        Element signedSignatureProperties = doc.createElementNS(xadesNamespace, "xades:SignedSignatureProperties");
        signedProperties.appendChild(signedSignatureProperties);
        
        // Tiempo de la firma
        Element signingTime = doc.createElementNS(xadesNamespace, "xades:SigningTime");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        signingTime.setTextContent(sdf.format(new Date()));
        signedSignatureProperties.appendChild(signingTime);
        
        // Certificado emisor
        Element signingCertificate = doc.createElementNS(xadesNamespace, "xades:SigningCertificate");
        signedSignatureProperties.appendChild(signingCertificate);
        
        Element cert = doc.createElementNS(xadesNamespace, "xades:Cert");
        signingCertificate.appendChild(cert);
        
        // CertDigest
        Element certDigest = doc.createElementNS(xadesNamespace, "xades:CertDigest");
        cert.appendChild(certDigest);
        
        Element digestMethodXades = doc.createElementNS("http://www.w3.org/2000/09/xmldsig#", "ds:DigestMethod");
        digestMethodXades.setAttribute("Algorithm", "http://www.w3.org/2000/09/xmldsig#sha1");
        certDigest.appendChild(digestMethodXades);
        
        // Calcular digest del certificado
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] certDer = certificate.getEncoded();
        byte[] certDigestVal = md.digest(certDer);
        String certDigestValBase64 = Base64.getEncoder().encodeToString(certDigestVal);
        
        Element digestValue = doc.createElementNS("http://www.w3.org/2000/09/xmldsig#", "ds:DigestValue");
        digestValue.setTextContent(certDigestValBase64);
        certDigest.appendChild(digestValue);
        
        // IssuerSerial
        Element issuerSerial = doc.createElementNS(xadesNamespace, "xades:IssuerSerial");
        cert.appendChild(issuerSerial);
        
        Element issuerName = doc.createElementNS("http://www.w3.org/2000/09/xmldsig#", "ds:X509IssuerName");
        issuerName.setTextContent(certificate.getIssuerDN().getName());
        issuerSerial.appendChild(issuerName);
        
        Element serialNumber = doc.createElementNS("http://www.w3.org/2000/09/xmldsig#", "ds:X509SerialNumber");
        serialNumber.setTextContent(certificate.getSerialNumber().toString());
        issuerSerial.appendChild(serialNumber);
        
        // Empaquetar QualifyingProperties en un ds:Object de la firma
        DOMStructure xadesObjectStructure = new DOMStructure(qualifyingProperties);
        XMLObject xadesObject = factory.newXMLObject(Collections.singletonList(xadesObjectStructure), null, null, null);
        
        // Configurar SignedInfo
        CanonicalizationMethod cm = factory.newCanonicalizationMethod(
            "http://www.w3.org/TR/2001/REC-xml-c14n-20010315",
            (C14NMethodParameterSpec) null
        );
        SignatureMethod sm = factory.newSignatureMethod(
            "http://www.w3.org/2000/09/xmldsig#rsa-sha1",
            null
        );
        
        List<Reference> references = Arrays.asList(docRef, xadesRef);
        SignedInfo si = factory.newSignedInfo(cm, sm, references);
        
        // Configurar KeyInfo (X509Data)
        KeyInfoFactory kif = factory.getKeyInfoFactory();
        X509Data x509Data = kif.newX509Data(Collections.singletonList(certificate));
        KeyInfo ki = kif.newKeyInfo(Collections.singletonList(x509Data));
        
        // Ensamblar Firma XML completa
        XMLSignature signature = factory.newXMLSignature(
            si,
            ki,
            Collections.singletonList(xadesObject),
            signatureId,
            null
        );
        
        // Firmar el documento (agrega el elemento <ds:Signature> al XML)
        signature.sign(dsc);
        
        // Serializar de DOM a String UTF-8 sin saltos innecesarios
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        
        invoice.setSignedXmlContent(writer.getBuffer().toString());
        invoice.setStatus(InvoiceStatus.SIGNED);
    }
    
    /**
     * Valida la firma digital de un documento XML firmado
     */
    public boolean validateSignature(ElectronicInvoice invoice) throws Exception {
        if (invoice.getSignedXmlContent() == null || invoice.getSignedXmlContent().isEmpty()) {
            return false;
        }
        
        // Parsear XML firmado
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(
            new ByteArrayInputStream(invoice.getSignedXmlContent().getBytes("UTF-8"))
        );
        
        // Buscar el nodo ds:Signature
        NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (nl.getLength() == 0) {
            return false;
        }
        
        // Contexto de validación
        DOMValidateContext valContext = new DOMValidateContext(
            new SimpleKeySelector(), 
            nl.item(0)
        );
        
        XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
        XMLSignature signature = factory.unmarshalXMLSignature(valContext);
        
        return signature.validate(valContext);
    }
    
    /**
     * Selector de clave simple para validación de firmas XML
     */
    private static class SimpleKeySelector extends KeySelector {
        @Override
        public KeySelectorResult select(KeyInfo keyInfo, KeySelector.Purpose purpose,
                                        AlgorithmMethod method, XMLCryptoContext context)
            throws KeySelectorException {
            
            for (Object info : keyInfo.getContent()) {
                if (info instanceof X509Data) {
                    X509Data x509Data = (X509Data) info;
                    for (Object o : x509Data.getContent()) {
                        if (o instanceof X509Certificate) {
                            final X509Certificate cert = (X509Certificate) o;
                            return new KeySelectorResult() {
                                @Override
                                public Key getKey() {
                                    return cert.getPublicKey();
                                }
                            };
                        }
                    }
                }
            }
            throw new KeySelectorException("No se encontró certificado X509 válido en KeyInfo.");
        }
    }
    
    public String getCertificatePath() {
        return certificatePath;
    }
    
    public void setCertificatePath(String certificatePath) {
        this.certificatePath = certificatePath;
    }
    
    public String getCertificatePassword() {
        return certificatePassword;
    }
    
    public void setCertificatePassword(String certificatePassword) {
        this.certificatePassword = certificatePassword;
    }
    
    public boolean isCertificateLoaded() {
        return keyStore != null;
    }
}
