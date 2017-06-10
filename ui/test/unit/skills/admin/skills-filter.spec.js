import {StageComponent} from 'aurelia-testing';
import {bootstrap} from 'aurelia-bootstrapper';
import {Container} from 'aurelia-dependency-injection';
import {HttpClient} from 'aurelia-fetch-client';

describe('IndexComponent', () => {
  let component;
  let viewModel;
  let svc;

  beforeEach(() => {
    component = StageComponent
      .withResources('skills/skills-filter')
      .inView('<skills-filter skills.bind="skills"></skills-filter>')
      .boundTo({skills:[{tags:['dotnet','language']}, {tags:['java','language']}]})

      component.bootstrap(aurelia => {
        aurelia.use.standardConfiguration();
      });
  });

  it('should render list of tags', done => {
    component.manuallyHandleLifecycle().create(bootstrap)
    .then(() => component.bind())
    .then(() => {
      const checkboxTag = document.querySelector('label');
      expect(checkboxTag).toBe(null);
    })
    .then(() => component.attached())
    .then(() => {
      const checkboxTags = [].slice.call(document.querySelectorAll('label'));
      expect(checkboxTags.map(t => t.innerText.trim())).toEqual(['dotnet','language','java']);
    })
    .then(done)
    .catch(e => { console.log(e.message); throw e;  })
  });

  afterEach(() => {
    component.dispose();
  });
});
