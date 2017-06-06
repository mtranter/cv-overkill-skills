import {HttpClient} from 'aurelia-fetch-client'
import {inject} from 'aurelia-framework'

@inject(HttpClient)
export class SkillsService{
  constructor(http){
    this.http = http;
  }
  getSkills() {
    return this.http.fetch('http://127.0.0.1:8080/')
      .then(d => d.json());
  }
}
