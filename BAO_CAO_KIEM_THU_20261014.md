# BÁO CÁO KIỂM THỬ LUỒNG BÁN HÀNG CƠ BẢN

- Ngày kiểm thử: 14/10/2026
- Nhánh kiểm thử: `main`
- Phạm vi: quản lý thuộc tính/sản phẩm/biến thể/ảnh/tồn kho và bán hàng tại quầy
- Môi trường: SQL Server, Spring Boot (cổng 8080), FE quản trị (cổng 5173)

## Kết quả ca kiểm thử

| Mã | Nội dung | Dữ liệu/kết quả thực tế | Trạng thái |
|---|---|---|---|
| TC01 | Đăng nhập quản trị | Đăng nhập được và vào trang quản trị | Đạt |
| TC02 | Tải danh sách thuộc tính | Hiển thị 12 thương hiệu hiện có | Đạt |
| TC03 | Kiểm tra bắt buộc tên thuộc tính | Bỏ trống tên, hệ thống báo `Vui lòng nhập tên thương hiệu` | Đạt |
| TC04 | Thêm thuộc tính | Tạo `TEST QA 20261014`, mã `TH013` thành công | Đạt |
| TC05 | Xem sản phẩm, biến thể và ảnh | SP030 hiển thị ảnh; CTSP090, màu Trắng, size L, tồn 40 | Đạt |
| TC06 | Tạo hóa đơn chờ | Tạo HD000031 thành công | Đạt |
| TC07 | Thêm sản phẩm vào hóa đơn | Thêm 1 CTSP090, đơn giá 539.000 đ | Đạt |
| TC08 | Áp dụng voucher | Tự chọn WELCOME10, giảm 53.900 đ; tốt hơn mức giảm 50.000 đ của AOTHETHAO50 | Đạt |
| TC09 | Tiền khách đưa không đủ | Nhập 100.000 đ, hệ thống chặn thanh toán và báo còn thiếu | Đạt |
| TC10 | Tính tiền thừa | Nhập 500.000 đ, tổng trả 485.100 đ, tiền thừa 14.900 đ | Đạt |
| TC11 | Thanh toán tiền mặt | HD000031 chuyển sang hoàn thành | Đạt |
| TC12 | Trừ tồn khi thanh toán | Tồn CTSP090: 40 → 39; đã bán: 0 → 1 | Đạt |
| TC13 | In hóa đơn | Mở được mẫu in và gọi thao tác in | Đạt một phần: chưa xác minh máy in/PDF vật lý |
| TC14 | Hoàn hàng | HD000031 chuyển sang `Đã hoàn hàng` | Đạt |
| TC15 | Hoàn tồn | Tồn CTSP090: 39 → 40; đã bán: 1 → 0 | Đạt |
| TC16 | Hủy hóa đơn chờ | Tạo rồi hủy HD000032; tồn kho không thay đổi | Đạt |
| TC17 | Lịch sử trạng thái hóa đơn | Ban đầu hiển thị sai; sau migration hiển thị đúng `Đã hoàn thành` và `Đã hoàn hàng` | Đã sửa và kiểm tra lại |
| TC18 | Truy cập FE bằng 127.0.0.1 | Bị CORS do backend chỉ cho phép localhost | Đã sửa mã nguồn; cần khởi động lại backend để nạp cấu hình mới |

## Lỗi đã sửa

### 1. Sai trạng thái trong lịch sử hóa đơn

Nguyên nhân: `lich_su_hoa_don.deleted` có kiểu `bit`, trong khi backend dùng cột này để lưu trạng thái hóa đơn từ 0 đến 10. SQL Server ép các giá trị lớn hơn 0 thành 1.

Đã thực hiện:

- Đổi kiểu cột thành `smallint` trong file tạo database.
- Tạo migration riêng `migration_20261014_sua_trang_thai_lich_su.sql`.
- Khôi phục trạng thái các bản ghi cũ dựa trên nội dung hành động.
- Chạy migration trên database kiểm thử và kiểm tra lại giao diện thành công.

### 2. CORS khi dùng địa chỉ 127.0.0.1

Đã bổ sung `http://127.0.0.1:5173` và `http://127.0.0.1:3000` vào cấu hình CORS chung và các controller có cấu hình CORS riêng.

## Kiểm tra kỹ thuật

- Backend: Maven test thành công, 1 test, 0 lỗi.
- FE quản trị: build thành công.
- FE khách hàng: build thành công.
- Cảnh báo phụ thuộc npm vẫn còn; chưa tự động nâng phiên bản vì có nguy cơ làm thay đổi hành vi hiện tại.

## Dữ liệu kiểm thử còn lại

- Thương hiệu: `TEST QA 20261014` / `TH013`.
- Hóa đơn HD000031: trạng thái `Đã hoàn hàng`, được giữ làm bằng chứng kiểm thử.
- HD000032: đã hủy.

## Kết luận

Luồng bán hàng tại quầy cơ bản đã chạy xuyên suốt từ tạo hóa đơn, thêm sản phẩm, voucher, kiểm tra tiền khách đưa, thanh toán, trừ tồn, hoàn hàng và hoàn tồn. Chưa nên chuyển sang phát triển hóa đơn/online cho đến khi khởi động lại backend, xác minh lại CORS, test in trên thiết bị/PDF thực tế và chạy lại một vòng smoke test ngắn.
