// Country Service - API để lấy danh sách quốc gia
export const countryService = {
  /**
   * Lấy danh sách tất cả quốc gia từ REST Countries API
   * @returns {Promise<Array>} Danh sách quốc gia đã được sắp xếp
   */
  async getAllCountries() {
    try {
      const response = await fetch('https://restcountries.com/v3.1/all?fields=name,cca2,flag');
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const countries = await response.json();
      
      // Chuyển đổi và sắp xếp dữ liệu
      return countries
        .map(country => ({
          code: country.cca2, // Mã quốc gia 2 ký tự (VN, US, JP...)
          name: country.name.common, // Tên quốc gia
          flag: country.flag || '', // Emoji cờ quốc gia
          label: `${country.flag || ''} ${country.name.common}`.trim() // Hiển thị với cờ
        }))
        .sort((a, b) => a.name.localeCompare(b.name, 'vi', { sensitivity: 'base' }));
    } catch (error) {
      console.error('Error fetching countries:', error);
      
      // Fallback với một số quốc gia phổ biến
      return this.getFallbackCountries();
    }
  },

  /**
   * Danh sách quốc gia dự phòng khi API không khả dụng
   * @returns {Array} Danh sách quốc gia cơ bản
   */
  getFallbackCountries() {
    return [
      { code: 'VN', name: 'Vietnam', flag: '🇻🇳', label: '🇻🇳 Vietnam' },
      { code: 'US', name: 'United States', flag: '🇺🇸', label: '🇺🇸 United States' },
      { code: 'CN', name: 'China', flag: '🇨🇳', label: '🇨🇳 China' },
      { code: 'JP', name: 'Japan', flag: '🇯🇵', label: '🇯🇵 Japan' },
      { code: 'KR', name: 'South Korea', flag: '🇰🇷', label: '🇰🇷 South Korea' },
      { code: 'DE', name: 'Germany', flag: '🇩🇪', label: '🇩🇪 Germany' },
      { code: 'IT', name: 'Italy', flag: '🇮🇹', label: '🇮🇹 Italy' },
      { code: 'FR', name: 'France', flag: '🇫🇷', label: '🇫🇷 France' },
      { code: 'GB', name: 'United Kingdom', flag: '🇬🇧', label: '🇬🇧 United Kingdom' },
      { code: 'TH', name: 'Thailand', flag: '🇹🇭', label: '🇹🇭 Thailand' }
    ].sort((a, b) => a.name.localeCompare(b.name, 'vi', { sensitivity: 'base' }));
  },

  /**
   * Tìm quốc gia theo mã hoặc tên
   * @param {Array} countries - Danh sách quốc gia
   * @param {string} searchValue - Giá trị tìm kiếm
   * @returns {Object|null} Quốc gia tìm được hoặc null
   */
  findCountry(countries, searchValue) {
    if (!searchValue || !countries.length) return null;
    
    return countries.find(country => 
      country.code.toLowerCase() === searchValue.toLowerCase() ||
      country.name.toLowerCase() === searchValue.toLowerCase()
    );
  }
};
