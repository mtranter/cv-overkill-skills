import {bindable, bindingMode} from 'aurelia-framework';

const concat = (xs,y) => xs.concat(y)
const flatMap = (f,xs) => xs.map(f).reduce(concat, [])

export class SkillsFilterCustomElement {
  @bindable({ defaultBindingMode: bindingMode.twoWay }) skills;
  selectedFilters = [];
  allSkills = [];
  tags = [];

  attached() {
    this.allSkills = this.skills
    this.tags = flatMap(s => s.tags, this.skills).filter((value, index, self) => self.indexOf(value) === index)
  }
  toggleTag(tag) {
    let index = this.tags.indexOf(tag)
    if(index === -1) {
      this.tags.push(tag)
    }else {
      this.tags.splice(index, -1)
    }
    this.refresh();
  }
  refresh() {
    this.skills = this.allSkills.filter(s => s.tags.some(t => this.tags.indexOf(t) > -1));
  }

}
