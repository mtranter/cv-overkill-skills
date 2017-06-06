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
}
