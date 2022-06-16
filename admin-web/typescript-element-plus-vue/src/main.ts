import { createApp } from 'vue';
import App from './App.vue';
import { router } from '/@/router/index';
import { store } from '/@/store/index';
import './theme/index.scss';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import * as icons from '@element-plus/icons';
import lodash from 'lodash';
import smartEnumPlugin from '/@/plugins/smart-enums-plugin';
import constantsInfo from "/@/constants/index";
import privilegePlugin from '/@/plugins/privilege-plugin';
import locale from 'element-plus/lib/locale/lang/zh-cn'
import JsonViewer from 'vue-json-viewer';
import { inserted } from '/@/directives/privilege';

let vueApp = createApp(App);
let app = vueApp
  .use(router)
  .use(store)
  .use(JsonViewer)
  .use(ElementPlus, { locale })
  .use(smartEnumPlugin, constantsInfo)
  .use(privilegePlugin);
app.directive('privilege', {
  mounted(el, binding) {
    inserted(el, binding);
  },
});
// 注册图标组件
Object.keys(icons).forEach((key) => {
  app.component(key, icons[key]);
});
app.config.globalProperties.$icons = icons;
app.config.globalProperties.$lodash = lodash;
app.mount('#app');