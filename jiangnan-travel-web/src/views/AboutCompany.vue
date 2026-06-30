<template>
  <div class="about-page app-page">
    <!-- 顶部渐变色背景 -->
    <div class="hero-section">
      <div class="hero-content">
        <div class="company-logo">🚗</div>
        <h1 class="company-name">江南出行智慧服务平台</h1>
        <p class="version">v1.0.0</p>
      </div>
    </div>

    <!-- 公司成就数据 -->
    <div class="achievements app-section">
      <div class="achievement-grid">
        <div class="achievement-item app-card">
          <span class="achievement-number">100万+</span>
          <span class="achievement-label">注册用户</span>
        </div>
        <div class="achievement-item app-card">
          <span class="achievement-number">11座</span>
          <span class="achievement-label">覆盖城市</span>
        </div>
        <div class="achievement-item app-card">
          <span class="achievement-number">500万+</span>
          <span class="achievement-label">服务订单</span>
        </div>
        <div class="achievement-item app-card">
          <span class="achievement-number">98.5%</span>
          <span class="achievement-label">好评率</span>
        </div>
      </div>
    </div>

    <div class="about-section app-card">
      <h3 class="app-section-title">公司愿景</h3>
      <p>江南出行智慧服务平台致力于打造中国领先的一站式智慧出行解决方案，通过数字化技术与AI智能调度，为用户提供安全、便捷、高效的出行服务体验。我们始终坚持以用户为中心，不断创新服务模式，让每一次出行都成为美好的旅程。</p>
    </div>

    <div class="about-section app-card">
      <h3 class="app-section-title">服务理念</h3>
      <p>我们秉承"智慧出行，服务至上"的理念，深度融合人工智能、大数据分析等前沿技术，实现智能派单、动态定价、实时路况预测等功能，大幅提升出行效率。同时，我们严格把控司机资质审核，建立完善的安全保障体系，让每一位乘客都能安心出行。</p>
    </div>

    <div class="about-section app-card">
      <h3 class="app-section-title">覆盖城市</h3>
      <p>目前已覆盖南昌、九江、赣州、上饶、宜春、吉安、抚州、景德镇、萍乡、新余、鹰潭等江西省内主要城市，并持续拓展服务网络。依托高德地图的精准导航与定位能力，我们在每个城市都建立了完善的运力调度体系，确保用户随时随地都能享受到优质的出行服务。</p>
    </div>

    <div class="about-section app-card">
      <h3 class="app-section-title">技术特色</h3>
      <p>平台深度融合DeepSeek AI大模型，提供智能行程规划、实时路况分析、个性化出行推荐等创新功能。结合高德地图LBS服务与WebSocket实时通信技术，实现订单状态实时推送、行程轨迹追踪、智能客服等全方位服务能力，为用户打造真正智能化的出行体验。</p>
    </div>

    <div class="contact-section app-card">
      <h3 class="app-section-title">联系我们</h3>
      <div class="contact-item">
        <span class="contact-icon">📞</span>
        <div class="contact-info">
          <span class="contact-label">客服电话</span>
          <span class="contact-value">400-888-9999</span>
        </div>
      </div>
      <div class="contact-item">
        <span class="contact-icon">📧</span>
        <div class="contact-info">
          <span class="contact-label">服务邮箱</span>
          <span class="contact-value">service@jiangnan-travel.com</span>
        </div>
      </div>
      <div class="contact-item">
        <span class="contact-icon">🕐</span>
        <div class="contact-info">
          <span class="contact-label">工作时间</span>
          <span class="contact-value">7×24小时全天候服务</span>
        </div>
      </div>
    </div>

    <el-button class="back-btn" @click="$router.back()">返回</el-button>
  </div>
</template>

<script setup>
</script>

<style scoped>
.about-page {
  padding-bottom: 40px;
}
.hero-section {
  background: linear-gradient(135deg, #1a6b4a 0%, #2D8A6E 30%, #3a9b8c 60%, #2c6b9e 100%);
  margin: calc(-1 * var(--spacing-md)) calc(-1 * var(--spacing-md)) 20px;
  padding: 40px 20px 36px;
  text-align: center;
  color: #fff;
}
.hero-content {
  max-width: 320px;
  margin: 0 auto;
}
.company-logo {
  font-size: 4rem;
  margin-bottom: 12px;
  filter: drop-shadow(0 2px 8px rgba(0,0,0,0.15));
}
.company-name {
  font-size: 1.6rem;
  font-weight: 700;
  margin-bottom: 6px;
  letter-spacing: 1px;
}
.version {
  font-size: 0.85rem;
  opacity: 0.8;
}
.achievement-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.achievement-item {
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.achievement-number {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-primary-dark);
}
.achievement-label {
  font-size: 0.8rem;
  color: var(--color-text-muted);
}
.about-section {
  padding: 16px 18px;
}
.about-section p {
  font-size: 0.9rem;
  line-height: 1.7;
  color: var(--color-text-secondary);
  text-indent: 2em;
}
.contact-section {
  padding: 16px 18px;
}
.contact-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--color-border-light);
}
.contact-item:last-child {
  border-bottom: none;
}
.contact-icon {
  font-size: 1.3rem;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg-secondary);
  border-radius: 50%;
  flex-shrink: 0;
}
.contact-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.contact-label {
  font-size: 0.8rem;
  color: var(--color-text-muted);
}
.contact-value {
  font-size: 0.95rem;
  color: var(--color-text);
  font-weight: 500;
}
.back-btn {
  width: 100%;
}
</style>
