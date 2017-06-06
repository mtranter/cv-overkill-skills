
export class Edit {
  constructor(skillsSvc) {
    this.svc = skillsSvc;
    this.opts = {max:100, step:5}
  }
  attached() {
    this.skillsSvc.getSkills().then(s => this.skills = s);
  }
  updateSkill(skill) {
    this.skillsSvc.updateSkill(skill);
  }
  deleteSkill(skill){
    this.skillsSvc.deleteSkill(skill);
  }
}
