<script setup>
import { ref } from 'vue'

const serviceStatus = ref('等待后端验收')

async function checkProductService() {
  serviceStatus.value = '检查中...'
  try {
    const response = await fetch('/api/product/health')
    const data = await response.json()
    serviceStatus.value = `${data.service}：${data.status}`
  } catch {
    serviceStatus.value = '后端暂未启动'
  }
}
</script>

<template>
  <main class="page">
    <p class="eyebrow">PINSHENGSHENG MALL</p>
    <h1>拼省省电商系统</h1>
    <p class="subtitle">从商品详情到订单支付，搭建可真实演示的电商业务闭环。</p>
    <button type="button" @click="checkProductService">检查商品服务</button>
    <p class="status">{{ serviceStatus }}</p>
  </main>
</template>

<style>
:root {
  font-family: Inter, "Microsoft YaHei", sans-serif;
  color: #172033;
  background: #f5f7fb;
}

body {
  margin: 0;
}

.page {
  box-sizing: border-box;
  min-height: 100vh;
  padding: 16vh 12vw;
  background: linear-gradient(135deg, #ffffff 0%, #eef4ff 100%);
}

.eyebrow {
  color: #5573d9;
  font-size: 0.8rem;
  font-weight: 700;
  letter-spacing: 0.16em;
}

h1 {
  margin: 1rem 0;
  font-size: clamp(2.5rem, 7vw, 5.5rem);
}

.subtitle,
.status {
  color: #5d6780;
  font-size: 1.1rem;
}

button {
  margin-top: 1.5rem;
  padding: 0.8rem 1.2rem;
  border: 0;
  border-radius: 0.6rem;
  color: #ffffff;
  background: #5573d9;
  cursor: pointer;
}
</style>
