/**
 * 图片 CDN 工具
 * 默认使用 Trae text-to-image 作为图片 CDN，可通过 .env 中的 VITE_IMAGE_CDN_BASE 切换为自有 CDN。
 */

const CDN_BASE = import.meta.env.VITE_IMAGE_CDN_BASE || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image'

const ALLOWED_SIZES = [
  'square_hd',
  'square',
  'portrait_4_3',
  'portrait_16_9',
  'landscape_4_3',
  'landscape_16_9',
]

/**
 * 构建 CDN 图片 URL
 * @param {string} prompt 图片提示词
 * @param {string} imageSize 图片尺寸
 * @returns {string}
 */
export function buildCdnUrl(prompt, imageSize = 'square') {
  const size = ALLOWED_SIZES.includes(imageSize) ? imageSize : 'square'
  return `${CDN_BASE}?prompt=${encodeURIComponent(prompt)}&image_size=${size}`
}

const USER_AVATAR_PROMPTS = [
  'A friendly young Chinese woman with shoulder-length black hair, soft smile, professional headshot, clean light gray background, realistic photo',
  'A friendly young Chinese man with short black hair, soft smile, professional headshot, clean light gray background, realistic photo',
  'A friendly middle-aged Chinese woman with warm smile, professional headshot, clean light gray background, realistic photo',
  'A friendly middle-aged Chinese man wearing glasses, professional headshot, clean light gray background, realistic photo',
]

/**
 * 获取用户默认头像
 * @param {string} seed 用于确定头像样式的种子（如 userId / phone）
 * @param {string} imageSize
 * @returns {string}
 */
export function getAvatarUrl(seed = '', imageSize = 'square') {
  const index = Math.abs(hashCode(String(seed))) % USER_AVATAR_PROMPTS.length
  return buildCdnUrl(USER_AVATAR_PROMPTS[index], imageSize)
}

/**
 * 获取司机默认头像
 */
export function getDriverAvatarUrl(seed = '', imageSize = 'square') {
  const prompt = `A professional Chinese taxi driver wearing a blue uniform and cap, friendly smile, clean studio background, portrait photo, realistic, seed ${seed}`
  return buildCdnUrl(prompt, imageSize)
}

/**
 * 获取管理员默认头像
 */
export function getAdminAvatarUrl(seed = '', imageSize = 'square') {
  const prompt = `A professional Chinese business administrator in formal attire, confident smile, clean studio background, portrait photo, realistic, seed ${seed}`
  return buildCdnUrl(prompt, imageSize)
}

/**
 * 获取 AI 助手头像
 */
export function getAiAvatarUrl(imageSize = 'square') {
  const prompt = 'A cute friendly white robot avatar with blue glowing eyes, minimal blue gradient background, 3D render, square'
  return buildCdnUrl(prompt, imageSize)
}

/**
 * 获取活动 Banner
 */
export function getCampaignBannerUrl(title = '', imageSize = 'landscape_16_9') {
  const prompt = `A vibrant modern Chinese travel promotion banner for "${title}", cityscape and nature elements, blue purple gradient, cinematic wide view, clean space for text, realistic`
  return buildCdnUrl(prompt, imageSize)
}

function hashCode(str) {
  let h = 0
  for (let i = 0; i < str.length; i++) {
    h = Math.imul(31, h) + str.charCodeAt(i)
  }
  return h
}
