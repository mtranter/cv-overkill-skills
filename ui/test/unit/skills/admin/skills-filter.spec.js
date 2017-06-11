import {StageComponent} from 'aurelia-testing';
import {bootstrap} from 'aurelia-bootstrapper';
import {Container} from 'aurelia-dependency-injection';
import {HttpClient} from 'aurelia-fetch-client';

describe('IndexComponent', () => {
  let component;
  let viewModel;
  let svc;
  let model = {skills:[]}

  beforeEach(() => {
    component = StageComponent
      .withResources('skills/skills-filter')
      .inView('<skills-filter skills.bind="skills"></skills-filter>')
      .boundTo(model)

      component.bootstrap(aurelia => {
        aurelia.use.standardConfiguration();
      });
  });

  it('should render list of tags', done => {
    component.create(bootstrap).then(() => {
      const checkboxTags = [].slice.call(document.querySelectorAll('label'));
      expect(checkboxTags.length).toBe(0);
    }).then(() => {
      model.skills = [{tags:['dotnet','language']}, {tags:['java','language']}];
      return Promise.delay(0, true);
    })
    .then(() => {
      const checkboxTags = [].slice.call(document.querySelectorAll('label'));
      expect(checkboxTags.map(t => t.innerText.trim())).toEqual(['dotnet','language','java']);
    })
    .then(() => {
      let dotnetLabel = document.querySelector('label');
      dotnetLabel.click();
      return Promise.delay(0);
    })
    .then(() => {
        let dotnetLabel = document.querySelector('label');
        expect(model.skills.length).toBe(1);
        expect(dotnetLabel.classList.contains('active')).toBe(true)
        dotnetLabel.click();
        return Promise.delay(0);
    })
    .then(() => {
      let dotnetLabel = document.querySelector('label');
      expect(model.skills.length).toBe(2)
      expect(dotnetLabel.classList.contains('active')).toBe(false)
    })
    .then(done)
    .catch(e => { console.log(e.message); console.log(e.stack); throw e;  })
  });

  afterEach(() => {
    component.dispose();
  });
});
