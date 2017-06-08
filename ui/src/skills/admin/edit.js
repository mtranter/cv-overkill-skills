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
    return this.refresh();
  }
  addSkill(skill) {
    this.skillsSvc.addSkill(skill).then(_ => {
       this.newSkill = {name:'', skillLevel: 50};
       this.refresh();
     });
  }
  updateSkill(skill) {
    this.skillsSvc.updateSkill(skill).then(_ => this.refresh());
  }
  deleteSkill(skill){
    this.skillsSvc.deleteSkill(skill).then(_ => this.refresh());
  }
  refresh() {
    return this.skillsSvc.getSkills().then(s => this.skills = s);
  }
}
