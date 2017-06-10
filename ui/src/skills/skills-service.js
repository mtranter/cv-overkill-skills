import {HttpClient} from 'aurelia-fetch-client'
import {inject} from 'aurelia-framework'

@inject(HttpClient)
export class SkillsService{
  constructor(http){
    this.http = http;
  }
  getSkills() {
    return this.http.fetch('http://api.marktranter.com/skills')
      .then(d => d.json());
  }
  addSkill(skill) {
    return this.http.fetch(`http://api.marktranter.com/skills/`, {method:'POST', body: JSON.stringify(skill)})
  }
  addTag(skill, tag) {
    return this.http.fetch(`http://api.marktranter.com/skills/${encodeURIComponent(skill.name)}/tags`, {method:'POST', body: JSON.stringify(tag)})
  }
  deleteTag(skill, tag) {
    return this.http.fetch(`http://api.marktranter.com/skills/${encodeURIComponent(skill.name)}/tags/${tag}`, {method:'DELETE'})
  }
  updateSkill(skill) {
    return this.http.fetch(`http://api.marktranter.com/skills/${encodeURIComponent(skill.name)}`, {method:'PATCH', body: JSON.stringify({skillLevel: skill.skillLevel})})
  }
  deleteSkill(skill) {
    return this.http.fetch(`http://api.marktranter.com/skills/${encodeURIComponent(skill.name)}`, {method:'DELETE'})
  }
}
