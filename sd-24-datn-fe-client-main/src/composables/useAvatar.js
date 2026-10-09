import { ref, computed } from 'vue'

// Global avatar state
const currentAvatar = ref('')

// Avatar styles from DiceBear
export const avatarStyles = [
  { name: 'avataaars', label: 'Avataaars' },
  { name: 'big-smile', label: 'Big Smile' },
  { name: 'bottts', label: 'Bottts' },
  { name: 'croodles', label: 'Croodles' },
  { name: 'fun-emoji', label: 'Fun Emoji' },
  { name: 'icons', label: 'Icons' },
  { name: 'initials', label: 'Initials' },
  { name: 'lorelei', label: 'Lorelei' },
  { name: 'micah', label: 'Micah' },
  { name: 'miniavs', label: 'Miniavs' },
  { name: 'notionists', label: 'Notionists' },
  { name: 'open-peeps', label: 'Open Peeps' },
  { name: 'personas', label: 'Personas' },
  { name: 'pixel-art', label: 'Pixel Art' },
  { name: 'shapes', label: 'Shapes' },
  { name: 'thumbs', label: 'Thumbs' }
]

// Avatar seeds for variety
export const avatarSeeds = [
  'Felix', 'Aneka', 'Kimberly', 'John', 'Sarah', 'Mike', 'Emma', 'David',
  'Lisa', 'Tom', 'Anna', 'Chris', 'Maria', 'Alex', 'Sophie', 'Ryan',
  'Jessica', 'Daniel', 'Amy', 'Kevin', 'Rachel', 'Brian', 'Nicole', 'Jason',
  'Michelle', 'Andrew', 'Laura', 'Steven', 'Jennifer', 'Matthew', 'Ashley', 'Joshua'
]

export function useAvatar() {
  // Generate DiceBear avatar URL
  const getAvatarUrl = (style, seed) => {
    return `https://api.dicebear.com/9.x/${style}/svg?seed=${seed}`
  }

  // Get current avatar URL with fallback
  const getCurrentAvatarUrl = () => {
    const saved = localStorage.getItem('userAvatar')
    if (saved) {
      currentAvatar.value = saved
      return saved
    }
    
    // Default avatar
    const defaultUrl = getAvatarUrl('avataaars', 'default')
    currentAvatar.value = defaultUrl
    return defaultUrl
  }

  // Save avatar to localStorage
  const saveAvatar = (avatarUrl) => {
    localStorage.setItem('userAvatar', avatarUrl)
    currentAvatar.value = avatarUrl
    
    // Emit global event for other components
    window.dispatchEvent(new CustomEvent('avatarUpdated', { 
      detail: { avatarUrl } 
    }))
  }

  // Generate random avatar
  const generateRandomAvatar = () => {
    const randomStyle = avatarStyles[Math.floor(Math.random() * avatarStyles.length)]
    const randomSeed = avatarSeeds[Math.floor(Math.random() * avatarSeeds.length)]
    return getAvatarUrl(randomStyle.name, randomSeed)
  }

  // Computed property for reactive avatar URL
  const avatarUrl = computed(() => {
    return currentAvatar.value || getCurrentAvatarUrl()
  })

  // Initialize avatar on first use
  const initializeAvatar = () => {
    getCurrentAvatarUrl()
  }

  return {
    // State
    currentAvatar,
    avatarUrl,
    
    // Methods
    getAvatarUrl,
    getCurrentAvatarUrl,
    saveAvatar,
    generateRandomAvatar,
    initializeAvatar,
    
    // Data
    avatarStyles,
    avatarSeeds
  }
}
