import { useConfirm } from './useConfirm.js';

/**
 * Hỏi xác nhận trước khi bật/tắt trạng thái của một bản ghi danh mục
 * (màu sắc, kích cỡ, chất liệu, thương hiệu, ...).
 *
 * Các màn quản lý thuộc tính đều gạt công tắc là gọi API ngay, rất dễ bấm nhầm khi cuộn
 * bảng mà không có cách nào biết để hoàn tác. Gom câu chữ vào một chỗ để bảy màn thuộc
 * tính nói cùng một ngôn ngữ thay vì mỗi nơi tự chế một kiểu.
 *
 * @example
 * const { confirmToggleStatus } = useStatusToggleConfirm();
 * if (!(await confirmToggleStatus('màu sắc', color.name, color.status === 'active'))) return;
 */
export function useStatusToggleConfirm() {
  const { confirm } = useConfirm();

  /**
   * @param {string} loaiBanGhi Tên loại bản ghi viết thường, ví dụ 'màu sắc', 'kích cỡ'.
   * @param {string} tenBanGhi  Tên cụ thể để người dùng đối chiếu.
   * @param {boolean} dangHoatDong true = đang bật, thao tác sắp tới là tắt.
   * @returns {Promise<boolean>} true nếu người dùng đồng ý.
   */
  const confirmToggleStatus = async (loaiBanGhi, tenBanGhi, dangHoatDong, event = null) => {
    const dongY = await confirm({
      title: dangHoatDong ? `Vô hiệu hóa ${loaiBanGhi}?` : `Kích hoạt lại ${loaiBanGhi}?`,
      message: dangHoatDong
        ? `${capitalize(loaiBanGhi)} này sẽ không còn được chọn khi tạo sản phẩm mới. Các sản phẩm đang dùng nó vẫn giữ nguyên.`
        : `${capitalize(loaiBanGhi)} này sẽ được dùng lại khi tạo sản phẩm mới.`,
      details: tenBanGhi ? [`${capitalize(loaiBanGhi)}: ${tenBanGhi}`] : [],
      confirmText: dangHoatDong ? 'Ngừng hoạt động' : 'Kích hoạt',
      tone: dangHoatDong ? 'warning' : 'info'
    });

    // Công tắc là <input type="checkbox"> bind bằng :checked. Trình duyệt đã tự lật ô
    // checkbox ngay khi người dùng bấm, mà dữ liệu phía Vue thì không đổi -> Vue không
    // re-render và công tắc bị kẹt ở trạng thái sai khi người dùng bấm "Hủy bỏ".
    // Trả ô checkbox về đúng trạng thái dữ liệu thật.
    if (!dongY && event?.target) {
      event.target.checked = dangHoatDong;
    }

    return dongY;
  };

  return { confirmToggleStatus };
}

const capitalize = (text) => (text ? text.charAt(0).toUpperCase() + text.slice(1) : text);
