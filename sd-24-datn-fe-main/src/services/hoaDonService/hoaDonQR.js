/**
 * QR Code functionality for HoaDon
 */

import { getHoaDonByMa } from '@/services/api/APIHoaDon/HoaDonAPI.js';
import QRCode from 'qrcode';

/**
 * Generate QR code for invoice
 * @param {string} maHoaDon - Invoice code
 * @returns {Promise<string>} Base64 QR code image
 */
export const generateInvoiceQR = async (maHoaDon) => {
    try {
        // Create QR code content with just the invoice code
        const qrContent = maHoaDon;
        
        // Generate QR code as base64 image
        const qrCodeDataURL = await QRCode.toDataURL(qrContent, {
            width: 300,
            margin: 2,
            color: {
                dark: '#000000',
                light: '#FFFFFF'
            }
        });
        
        return qrCodeDataURL;
    } catch (error) {
        console.error('Error generating QR code:', error);
        throw error;
    }
};

/**
 * Download QR code as image file
 * @param {string} maHoaDon - Invoice code
 * @param {string} fileName - Optional custom file name
 */
export const downloadInvoiceQR = async (maHoaDon, fileName = null) => {
    try {
        // Generate QR code
        const qrCodeDataURL = await generateInvoiceQR(maHoaDon);
        
        // Create download link
        const link = document.createElement('a');
        link.href = qrCodeDataURL;
        link.download = fileName || `QR_HoaDon_${maHoaDon}.png`;
        
        // Trigger download
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        
        return true;
    } catch (error) {
        console.error('Error downloading QR code:', error);
        throw error;
    }
};

/**
 * Verify invoice exists by code (for QR validation)
 * @param {string} maHoaDon - Invoice code
 * @returns {Promise<Object>} Invoice data if exists
 */
export const verifyInvoiceByCode = async (maHoaDon) => {
    try {
        const invoiceData = await getHoaDonByMa(maHoaDon);
        return invoiceData;
    } catch (error) {
        console.error('Error verifying invoice:', error);
        throw error;
    }
};

/**
 * Generate QR code with invoice validation
 * @param {string} maHoaDon - Invoice code
 * @returns {Promise<Object>} QR code data and invoice info
 */
export const generateValidatedQR = async (maHoaDon) => {
    try {
        // First verify invoice exists
        const invoiceData = await verifyInvoiceByCode(maHoaDon);
        
        // Generate QR code
        const qrCodeDataURL = await generateInvoiceQR(maHoaDon);
        
        return {
            qrCode: qrCodeDataURL,
            invoice: invoiceData,
            success: true
        };
    } catch (error) {
        console.error('Error generating validated QR:', error);
        return {
            qrCode: null,
            invoice: null,
            success: false,
            error: error.message
        };
    }
};

export default {
    generateInvoiceQR,
    downloadInvoiceQR,
    verifyInvoiceByCode,
    generateValidatedQR
};
