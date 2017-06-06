export function configure(config){
  let container = config.container;
  container.registerInstance('plugin.admin.route',  { route: ['skills'],  name: 'edit-skills',  moduleId: 'skills/admin/edit', title: 'Skills', nav: true,
    settings:{
      icon: 'fa-address-card'
    }
 });

  container.registerInstance('plugin.widget.homepage.component', {
    title: 'Skills & Techs',
    href:'#skills',
    name:'skills',
    viewModel: 'skills/skills',
    view:'skills/skills.html'
  });
}
