import {SkillsService} from './../skills-service'
import {inject} from 'aurelia-framework'

@inject(SkillsService)
export class Edit {
  newSkill = {name:'', skillLevel: 50}
  constructor(skillsSvc) {
    this.skillsSvc = skillsSvc;
    this.sliderOpts = { max:100, step:5 };
  }
  attached() {
    return this.skillsSvc.getSkills().then(s => this.skills = s);
  }
  addSkill(skill) {
    this.skillsSvc.addSkill(skill).then(_ => newSkill = {name:'', skillLevel: 50});
  }
  updateSkill(skill) {
    this.skillsSvc.updateSkill(skill);
  }
  deleteSkill(skill){
    this.skillsSvc.deleteSkill(skill);
  }
}
