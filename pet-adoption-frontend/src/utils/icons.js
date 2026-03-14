import * as ElementPlusIconsVue from '@element-plus/icons-vue'

export function registerIcons(app, iconNames) {
  iconNames.forEach(name => {
    const component = ElementPlusIconsVue[name]
    if (component) {
      app.component(name, component)
    }
  })
}