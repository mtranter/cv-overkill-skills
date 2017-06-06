
export function configure(aurelia) {
  aurelia.use
    .standardConfiguration()
    .plugin('skills/plugin.js');

  aurelia.use.developmentLogging();

  aurelia.start().then(() => {
    aurelia.setRoot('skills/skills.js', document.getElementById('root'))
  });
}
