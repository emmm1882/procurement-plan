<template>
  <div id="app">
    <Header />
    <div class="main-layout">
      <Sidebar />
      <div class="main-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script>
import Header from './layout/Header.vue'
import Sidebar from './layout/Sidebar.vue'

export default {
  name: 'App',
  components: { Header, Sidebar }
}
</script>

<style>
#app {
  height: 100vh;
  background: #f5f7fa;
}
.main-layout {
  display: flex;
  height: calc(100vh - 56px);
}
.main-content {
  flex: 1;
  padding: 24px;
  background: #f5f7fa;
  overflow: auto;
}
</style>