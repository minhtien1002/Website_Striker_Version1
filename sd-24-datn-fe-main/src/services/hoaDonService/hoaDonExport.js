import { formatDate, formatCurrency, getStatusText } from './hoaDonFormatters.js';

/**
 * Service for exporting invoice data and generating reports
 */

// Print invoice as PDF - Download from backend
export const printInvoicePDF = async (invoice) => {
  try {
    // Import API function dynamically
    const { downloadInvoicePDF } = await import('../api/APIHoaDon/HoaDonAPI.js');
    
    // Download PDF from backend using invoice ID
    const pdfBlob = await downloadInvoicePDF(invoice.id);
    
    // Create download link
    const url = window.URL.createObjectURL(pdfBlob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `HoaDon_${invoice.maHoaDon}.pdf`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    
    console.log(`PDF downloaded for invoice: ${invoice.maHoaDon} (ID: ${invoice.id})`);
  } catch (error) {
    console.error('Error downloading PDF:', error);
    
    // Fallback to browser print if backend fails
    const printWindow = window.open('', '_blank');
    const orderTypeText = invoice.loaiDon === 'Online' ? 'Online' : 'Tại quầy';

    printWindow.document.write(`
      <!DOCTYPE html>
      <html>
      <head>
        <title>Hóa đơn ${invoice.maHoaDon}</title>
        <style>
          body { font-family: Arial, sans-serif; padding: 20px; }
          .header { text-align: center; margin-bottom: 30px; }
          .info { margin-bottom: 20px; }
          .label { font-weight: bold; }
          .total { font-size: 18px; font-weight: bold; color: #e74c3c; }
        </style>
      </head>
      <body>
        <div class="header">
          <h1>HÓA ĐƠN BÁN HÀNG</h1>
          <p>Mã hóa đơn: <span class="label">${invoice.maHoaDon}</span></p>
          <p>Ngày tạo: <span class="label">${formatDate(invoice.ngayTao)}</span></p>
        </div>
        
        <div class="info">
          <h3>Thông tin khách hàng</h3>
          <p>Tên: <span class="label">${invoice.khachHang?.tenKhachHang || 'Khách lẻ'}</span></p>
          <p>Số điện thoại: <span class="label">${invoice.khachHang?.soDienThoai || 'N/A'}</span></p>
          <p>Loại đơn: <span class="label">${orderTypeText}</span></p>
        </div>
        
        <div class="info">
          <h3>Thông tin thanh toán</h3>
          <p>Phí vận chuyển: <span class="label">${formatCurrency(invoice.phiVanChuyen)}</span></p>
          <p class="total">Tổng tiền: ${formatCurrency(invoice.tongTienThanhToan)}</p>
        </div>
      </body>
      </html>
    `);

    printWindow.document.close();
    printWindow.print();
  }
};

// Download QR code for invoice
export const downloadQRCode = async (invoice) => {
  try {
    // Import QR service dynamically to avoid circular dependencies
    const { downloadInvoiceQR } = await import('./hoaDonQR.js');
    
    // Download QR code using the new service
    await downloadInvoiceQR(invoice.maHoaDon);
    
    // Show success message (optional - you can add toast notification here)
    console.log(`QR code downloaded for invoice: ${invoice.maHoaDon}`);
  } catch (error) {
    console.error('Error downloading QR code:', error);
    
    // Fallback to simple QR code if the new service fails
    const qrData = {
      maHoaDon: invoice.maHoaDon,
      khachHang: invoice.khachHang?.tenKhachHang || 'Khách lẻ',
      tongTien: invoice.tongTienThanhToan,
      ngayTao: invoice.ngayTao
    };

    const qrContent = `Hóa đơn: ${qrData.maHoaDon}\nKhách hàng: ${qrData.khachHang}\nTổng tiền: ${formatCurrency(qrData.tongTien)}\nNgày: ${formatDate(qrData.ngayTao)}`;
    const qrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${encodeURIComponent(qrContent)}`;

    const link = document.createElement('a');
    link.href = qrUrl;
    link.download = `QR_${invoice.maHoaDon}.png`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
};

// Export invoices to Excel/CSV
export const exportToExcel = (filteredInvoices) => {
  // Create Excel data from filtered invoices
  const excelData = filteredInvoices.map((invoice, index) => ({
    'STT': index + 1,
    'Mã Hóa Đơn': invoice.maHoaDon,
    'Khách Hàng': invoice.khachHang?.tenKhachHang || 'Khách lẻ',
    'Số Điện Thoại': invoice.khachHang?.soDienThoai || 'N/A',
    'Loại Đơn': invoice.loaiDon === 'online' ? 'Online' : 'Trực tiếp',
    'Phí Vận Chuyển': invoice.phiVanChuyen,
    'Ngày Tạo': formatDate(invoice.ngayTao),
    'Tổng Tiền': invoice.tongTienThanhToan,
    'Trạng Thái': getStatusText(invoice.trangThai?.id)
  }));

  // Convert to CSV format
  const headers = Object.keys(excelData[0]).join(',');
  const csvContent = [headers, ...excelData.map(row => Object.values(row).join(','))].join('\n');

  // Create and download file
  const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = `Danh_sach_hoa_don_${new Date().toISOString().split('T')[0]}.csv`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);

  console.log('Exported invoices to Excel');
};

// Scan QR invoice - opens QR scanner modal
export const scanQRInvoice = (showQRScannerModal) => {
  if (showQRScannerModal && typeof showQRScannerModal === 'function') {
    showQRScannerModal();
  } else {
    console.warn('QR Scanner modal function not provided');
  }
};
