import { ref } from 'vue'

/**
 * 高德地图 POI 搜索 composable
 * 提供 AutoComplete（输入提示）和 PlaceSearch（详情查询）能力
 * 独立加载 AMap 脚本与插件，不依赖 AmapView.vue
 */
export function useAmapPoiSearch() {
  const suggestions = ref([])
  const searching = ref(false)
  const loaded = ref(false)

  let readyPromise = null

  /** 确保 AMap 及所需插件已加载 */
  const ensureReady = () => {
    if (readyPromise) return readyPromise
    readyPromise = new Promise((resolve) => {
      const key = import.meta.env.VITE_AMAP_KEY
      const securityCode = import.meta.env.VITE_AMAP_SECURITY_JS_CODE
      if (!key) { resolve(false); return }

      window._AMapSecurityConfig = { securityJsCode: securityCode }

      const waitLoad = () => {
        if (window.AMap) {
          window.AMap.plugin(['AMap.AutoComplete', 'AMap.PlaceSearch'], () => {
            loaded.value = true
            resolve(true)
          })
          return
        }
        const script = document.createElement('script')
        script.src = `https://webapi.amap.com/maps?v=2.0&key=${key}&callback=amapPoiOnLoad`
        window.amapPoiOnLoad = waitLoad
        script.onerror = () => resolve(false)
        document.head.appendChild(script)
      }
      // 如果脚本已在 AmapView 加载中，等其就绪
      if (window.AMap) {
        waitLoad()
      } else {
        window.amapPoiOnLoad = waitLoad
        // 可能 AmapView 也会加载同个脚本，不要重复创建 script
        if (!document.querySelector('script[src*="webapi.amap.com/maps"]')) {
          const script = document.createElement('script')
          script.src = `https://webapi.amap.com/maps?v=2.0&key=${key}&callback=amapPoiOnLoad`
          script.onerror = () => resolve(false)
          document.head.appendChild(script)
        }
      }
    })
    return readyPromise
  }

  /**
   * 搜索输入提示
   * @param {string} keyword 搜索关键词
   * @param {string} city 限定城市，默认 '南昌'
   */
  const search = async (keyword, city = '南昌') => {
    if (!keyword || keyword.trim().length < 1) {
      suggestions.value = []
      return
    }
    const ok = await ensureReady()
    if (!ok) { suggestions.value = []; return }

    searching.value = true
    try {
      const auto = new window.AMap.AutoComplete({ city, citylimit: true })
      auto.search(keyword, (status, result) => {
        if (status === 'complete' && result.info === 'OK') {
          suggestions.value = (result.tips || []).slice(0, 8).map(tip => ({
            name: tip.name,
            address: tip.district || '',
            lat: tip.location ? tip.location.lat : null,
            lng: tip.location ? tip.location.lng : null
          }))
        } else {
          suggestions.value = []
        }
        searching.value = false
      })
    } catch {
      suggestions.value = []
      searching.value = false
    }
  }

  /**
   * 根据名称获取 POI 精确坐标
   * @param {string} name POI 名称
   * @param {string} city 城市
   */
  const getPoiLocation = (name, city = '南昌') => {
    return new Promise((resolve) => {
      ensureReady().then(ok => {
        if (!ok) { resolve(null); return }
        const place = new window.AMap.PlaceSearch({ city, citylimit: true })
        place.search(name, (s, r) => {
          if (s === 'complete' && r.poiList?.pois?.length > 0) {
            const poi = r.poiList.pois[0]
            resolve({ name: poi.name, lat: poi.location.lat, lng: poi.location.lng, address: poi.pname + poi.cityname + poi.adname })
          } else {
            resolve(null)
          }
        })
      })
    })
  }

  const clear = () => {
    suggestions.value = []
  }

  return { suggestions, searching, loaded, search, getPoiLocation, clear }
}
