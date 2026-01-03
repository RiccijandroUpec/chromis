package uk.chromis.pos.invoice.utils;

/**
 * Constantes para facturación electrónica Ecuador
 */
public class InvoiceConstants {
    
    // Tipos de documento
    public static final String DOCUMENT_TYPE_INVOICE = "01";
    public static final String DOCUMENT_TYPE_NOTE_CREDIT = "04";
    public static final String DOCUMENT_TYPE_NOTE_DEBIT = "05";
    public static final String DOCUMENT_TYPE_LIQUIDATION = "03";
    
    // Códigos de impuestos
    public static final String TAX_CODE_VAT = "2"; // IVA
    public static final String TAX_CODE_ICE = "3"; // ICE
    public static final String TAX_CODE_IRBPNR = "5"; // IRBPNR
    
    // Porcentajes de IVA
    public static final String VAT_RATE_ZERO = "0"; // 0%
    public static final String VAT_RATE_FIVE = "2"; // 5%
    public static final String VAT_RATE_TWELVE = "3"; // 12%
    
    // Códigos de forma de pago
    public static final String PAYMENT_CASH = "01"; // Efectivo
    public static final String PAYMENT_CHECK = "19"; // Cheque
    public static final String PAYMENT_DEBIT_CARD = "16"; // Tarjeta débito
    public static final String PAYMENT_CREDIT_CARD = "17"; // Tarjeta crédito
    public static final String PAYMENT_TRANSFER = "20"; // Transferencia bancaria
    public static final String PAYMENT_WALLET = "21"; // Wallet/Billetera digital
    public static final String PAYMENT_PREPAID = "03"; // Prepagado
    
    // Tipos de identificación
    public static final String IDENTIFICATION_TYPE_CEDULA = "C"; // Cédula de identidad
    public static final String IDENTIFICATION_TYPE_RUC = "R"; // RUC
    public static final String IDENTIFICATION_TYPE_PASSPORT = "P"; // Pasaporte
    public static final String IDENTIFICATION_TYPE_FOREIGN = "O"; // Extranjero
    
    // Estados de factura
    public static final String INVOICE_STATUS_DRAFT = "DRAFT";
    public static final String INVOICE_STATUS_GENERATED = "GENERATED";
    public static final String INVOICE_STATUS_SIGNED = "SIGNED";
    public static final String INVOICE_STATUS_SENT = "SENT_TO_SRI";
    public static final String INVOICE_STATUS_AUTHORIZED = "AUTHORIZED";
    public static final String INVOICE_STATUS_REJECTED = "REJECTED";
    public static final String INVOICE_STATUS_CANCELLED = "CANCELLED";
    
    // Ambientes SRI
    public static final String ENVIRONMENT_TEST = "test";
    public static final String ENVIRONMENT_PRODUCTION = "production";
    
    // URLs SRI
    public static final String SRI_URL_TEST = "https://celcert.sri.gob.ec/comprobantes-electronicos-ws/RecepcionComprobantes";
    public static final String SRI_URL_PRODUCTION = "https://celcer.sri.gob.ec/comprobantes-electronicos-ws/RecepcionComprobantes";
    
    // Monedas
    public static final String CURRENCY_USD = "USD";
    public static final String CURRENCY_UVT = "UVT"; // Unidad de Valor Tributario
    
    // Obligaciones
    public static final String ACCOUNTING_YES = "SI";
    public static final String ACCOUNTING_NO = "NO";
    
    // Longitudes esperadas
    public static final int ACCESS_KEY_LENGTH = 49;
    public static final int RUC_LENGTH = 13;
    public static final int CEDULA_LENGTH = 10;
}
