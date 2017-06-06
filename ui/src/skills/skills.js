import {SkillsService} from './skills-service'
import {inject} from 'aurelia-framework'

@inject(SkillsService)
export class Skills {
  skills = []
  constructor(svc) {
    this.svc = svc
  }
  activate() {
    this.svc.getSkills()
      .then(skills => this.skills = skills);
  }
}
